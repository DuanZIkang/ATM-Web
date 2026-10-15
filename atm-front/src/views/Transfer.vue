<template>
  <div class="page">
    <NavBar />

    <div class="card-container">
      <div class="card">
        <h2>转账</h2>

        <input 
          v-model="toCard" 
          class="input"
          placeholder="目标卡号"
        />
        <input 
          v-model="amount" 
          class="input"
          placeholder="请输入金额"
        />
        <p v-if="amountError" style="color: red; font-size: 14px;">{{ amountError }}</p>

        <button class="btn" @click="doTransfer" :disabled="!!amountError || !amount || !toCard">确认转账</button>
        <button class="btn secondary" @click="$router.push('/home')">返回</button>
        <p v-if="msg">{{ msg }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import NavBar from "@/components/NavBar.vue";
import axios from "axios";
import { computed, ref } from "vue";
import { useRouter } from "vue-router";

const toCard = ref("");
const amount = ref("");
const msg = ref("");
const router = useRouter();

// 金额校验放在函数外面，模板里的 amountError 才能读到
const amountError = computed(() => {
  if (!amount.value) return "";
  const num = Number(amount.value);
  if (isNaN(num)) return "金额格式错误：仅允许输入纯数字";
  if (num <= 0) return "金额必须大于0";
  if (!/^\d+(\.\d{1,2})?$/.test(amount.value)) return "金额最多可包含两位小数";
  return "";
});

async function doTransfer() {
  if (amountError.value) {
    msg.value = amountError.value;
    return;
  }
  if (!toCard.value || !amount.value) {
    msg.value = "请输入对方卡号和转账金额";
    return;
  }

  const userInfo = JSON.parse(sessionStorage.getItem("userInfo"));
  if (!userInfo) {
    await router.push("/login");
    return;
  }

  try {
    const res = await axios.post(`${import.meta.env.VITE_API_URL}/transfer`, {
      fromCard: userInfo.card,
      toCard: toCard.value,
      amount: parseFloat(amount.value)
    }, {
      headers: { token: sessionStorage.getItem("token") }
    });

    if (res.data.success) {
      userInfo.balance = res.data.data.balance;
      sessionStorage.setItem("userInfo", JSON.stringify(userInfo));
      msg.value = `转账成功，当前余额：${userInfo.balance} 元`;
      await router.push("/home");
    } else {
      msg.value = res.data.message || "转账失败";
    }
  } catch (e) {
    msg.value = e.response?.data?.message || "服务器错误";
  }
}
</script>

<style scoped>
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
<style scoped>
@import "@/assets/styles/Transfer.css";
</style>
