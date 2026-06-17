<template>
  <div class="page">
    <NavBar />

    <div class="card-container">
      <div class="card">
        <h2>存款</h2>

        <input v-model="amount" class="input" placeholder="请输入金额" />

        <button class="btn" @click="doDeposit">确认存款</button>
        <button class="btn secondary" @click="$router.push('/home')">返回</button>
        <p>{{ msg }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import NavBar from "@/components/NavBar.vue";
import axios from "axios";
import { ref } from "vue";
import { useRouter } from "vue-router";

const amount = ref("");
const msg = ref("");
const router = useRouter();

async function doDeposit() {
  // ✅ 从 userInfo 读取 card
  const userInfo = JSON.parse(sessionStorage.getItem("userInfo"));
  if (!userInfo) {
    router.push("/login");
    return;
  }

  try {
    const res = await axios.post(`${import.meta.env.VITE_API_URL}/deposit`, {
      card: userInfo.card,
      amount: Number(amount.value)
    }, {
      headers: { token: sessionStorage.getItem("token") }
    });

    if (res.data.success) {
      // ✅ 更新 userInfo 里的余额
      userInfo.balance = res.data.data.balance;
      sessionStorage.setItem("userInfo", JSON.stringify(userInfo));
      router.push("/home");
    } else {
      msg.value = res.data.message || "存款失败";
    }
  } catch (e) {
    msg.value = "服务器错误";
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
