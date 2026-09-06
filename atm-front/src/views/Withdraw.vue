<template>
  <div class="page">
    <NavBar />

    <div class="card-container">
      <div class="card">
        <h2>取款</h2>

        <input
            v-model="amount"
            class="input"
            placeholder="请输入金额"
        />
        <p v-if="amountError" style="color: red; font-size: 14px;">{{ amountError }}</p>

        <button class="btn" @click="doWithdraw" :disabled="!!amountError || !amount">确认取款</button>
        <button class="btn secondary" @click="$router.push('/home')">返回</button>

        <p v-if="msg">{{ msg }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import NavBar from "@/components/NavBar.vue";
import axios from "axios";
import { ref, computed, watch } from "vue";
import { useRouter } from "vue-router";

const amount = ref("");
const msg = ref("");
const amountError = ref("");
const router = useRouter();

const computedAmountError = computed(() => {
  if (!amount.value) return "";
  const num = Number(amount.value);
  if (isNaN(num)) return "金额格式错误：仅允许输入纯数字";
  if (num <= 0) return "金额必须大于0";
  if (!/^\d+(\.\d{1,2})?$/.test(amount.value)) return "金额最多可包含两位小数";
  return "";
});

watch(computedAmountError, (newVal) => {
  amountError.value = newVal;
});

async function doWithdraw() {
  if (amountError.value) {
    msg.value = amountError.value;
    return;
  }

  const userInfo = JSON.parse(sessionStorage.getItem("userInfo"));
  if (!userInfo) {
    router.push("/login");
    return;
  }

  try {
    const amountValue = parseFloat(amount.value);
    if (isNaN(amountValue) || amountValue <= 0) {
      msg.value = "金额格式错误";
      return;
    }

    const res = await axios.post(`${import.meta.env.VITE_API_URL}/withdraw`, {
      card: userInfo.card,
      amount: amountValue,
      password: ""
    }, {
      headers: { token: sessionStorage.getItem("token") }
    });

    if (res.data.success) {
      userInfo.balance = res.data.data.balance;
      sessionStorage.setItem("userInfo", JSON.stringify(userInfo));
      msg.value = "取款成功！";
      setTimeout(() => {
        router.push("/home");
      }, 500);
    } else {
      msg.value = res.data.message || "取款失败";
    }
  } catch (e) {
    msg.value = e.response?.data?.message || "服务器错误";
  }
}
</script>

<style scoped>
@import "@/assets/styles/Withdraw.css";
.card-container {
  display: flex;
  justify-content: center;
  padding: 20px;
}
.card {
  width: 400px;
  background: #fff;
  padding: 25px;
  border-radius: 12px;
  box-shadow: var(--shadow);
}
</style>