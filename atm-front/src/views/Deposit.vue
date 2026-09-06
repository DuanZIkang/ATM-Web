<template>
  <div class="page">
    <NavBar />

    <div class="card-container">
      <div class="card">
        <h2>存款</h2>

        <input 
          v-model="amount" 
          class="input" 
          placeholder="请输入金额"
        />
        
        <!-- ✅ 添加验证错误提示 -->
        <p v-if="amountError" style="color: red; font-size: 14px;">{{ amountError }}</p>

        <button class="btn" @click="doDeposit" :disabled="!!amountError || !amount">确认存款</button>
        <button class="btn secondary" @click="$router.push('/home')">返回</button>
        
        <p v-if="msg" style="margin-top: 10px;">{{ msg }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import NavBar from "@/components/NavBar.vue";
import axios from "axios";
import { ref, computed } from "vue";
import { useRouter } from "vue-router";

const amount = ref("");
const msg = ref("");
const amountError = ref("");
const router = useRouter();

// ✅ 计算验证错误
const computedAmountError = computed(() => {
  if (!amount.value) return "";
  
  const num = Number(amount.value);
  
  // 检查是否为有效数字
  if (isNaN(num)) {
    return "金额格式错误：仅允许输入纯数字";
  }
  
  // 检查是否为正数
  if (num <= 0) {
    return "金额必须大于0";
  }
  
  // 检查小数位数
  if (!/^\d+(\.\d{1,2})?$/.test(amount.value)) {
    return "金额最多可包含两位小数";
  }
  
  return "";
});

// 监听 computedAmountError 的变化
import { watch } from "vue";
watch(computedAmountError, (newVal) => {
  amountError.value = newVal;
});

async function doDeposit() {
  // ✅ 前端再次验证
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
    // ✅ 明确转换为数字，防止发送 NaN
    const amountValue = parseFloat(amount.value);
    
    if (isNaN(amountValue) || amountValue <= 0) {
      msg.value = "金额格式错误";
      return;
    }

    const res = await axios.post(`${import.meta.env.VITE_API_URL}/deposit`, {
      card: userInfo.card,
      amount: amountValue  // ✅ 确保是有效的数字
    }, {
      headers: { token: sessionStorage.getItem("token") }
    });

    if (res.data.success) {
      userInfo.balance = res.data.data.balance;
      sessionStorage.setItem("userInfo", JSON.stringify(userInfo));
      msg.value = "存款成功！";
      setTimeout(() => {
        router.push("/home");
      }, 500);
    } else {
      msg.value = res.data.message || "存款失败";
    }
  } catch (e) {
    msg.value = e.response?.data?.message || "服务器错误";
  }
}
</script>

<style scoped>
@import "@/assets/styles/ChangePassword.css";
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
