<template>
  <div class="page">
    <NavBar />

    <div class="card-container">
      <div class="card">
        <h2>转账</h2>

        <input v-model="toCard" class="input" placeholder="对方卡号" />
        <input v-model="amount" class="input" placeholder="金额" />

        <button class="btn" @click="doTransfer">确认转账</button>
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

const toCard = ref("");
const amount = ref("");
const msg = ref("");
const router = useRouter();

async function doTransfer() {
  // ✅ 从 userInfo 读取 card
  const userInfo = JSON.parse(sessionStorage.getItem("userInfo"));
  if (!userInfo) {
    router.push("/login");
    return;
  }

  if (!toCard.value || !amount.value) {
    msg.value = "请输入对方卡号和转账金额";
    return;
  }

  try {
    const res = await axios.post(`${import.meta.env.VITE_API_URL}/transfer`, {
      fromCard: userInfo.card,
      toCard: toCard.value,
      amount: Number(amount.value)
    }, {
      headers: { token: sessionStorage.getItem("token") }
    });

    if (res.data.success) {
      // ✅ 更新 userInfo 里的余额
      userInfo.balance = res.data.data.balance;
      sessionStorage.setItem("userInfo", JSON.stringify(userInfo));
      msg.value = `转账成功，当前余额：${userInfo.balance} 元`;
      router.push("/home");
    } else {
      msg.value = res.data.message || "转账失败";
    }
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
@import "@/assets/styles/Transfer.css";
</style>
