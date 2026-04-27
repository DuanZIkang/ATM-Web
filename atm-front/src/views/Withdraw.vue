<template>
  <div class="page">
    <NavBar />

    <div class="card-container">
      <div class="card">
        <h2>取款</h2>

        <input v-model="amount" class="input" placeholder="请输入金额" />

        <button class="btn" @click="doWithdraw">确认取款</button>
        <button class="btn secondary" @click="$router.push('/home')">返回</button>

        <p>{{ msg }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
console.log("Withdraw.vue 已加载"); // 必须能看到

import NavBar from "@/components/NavBar.vue";
import axios from "axios";
import { ref } from "vue";
import { useRouter } from "vue-router";

const amount = ref("");
const msg = ref("");
const router = useRouter();

async function doWithdraw() {
  msg.value = "按钮已触发"; // 确认按钮触发

  const acc = JSON.parse(localStorage.getItem("account"));
  if (!acc) {
    msg.value = "未登录";
    return;
  }

  try {
    const res = await axios.post(`${import.meta.env.VITE_API_URL}/withdraw`, {
      card: acc.card,
      amount: Number(amount.value)
    });

    // 确认返回的是余额数据
    if (!res.data || !res.data.data) {
      msg.value = "取款失败";
      return;
    }

    // 更新余额
    acc.balance = res.data.data;
    localStorage.setItem("account", JSON.stringify(acc));

    msg.value = `取款成功，当前余额：${acc.balance}元`;
    router.push("/home");

  } catch (e) {
    msg.value = "服务器错误或余额不足";
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
@import "@/assets/styles/Withdraw.css";
</style>
