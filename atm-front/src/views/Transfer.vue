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
  msg.value = "按钮已触发"; // 确认按钮触发

  const acc = JSON.parse(sessionStorage.getItem("account"));
  if (!acc) {
    msg.value = "未登录";
    return;
  }

  if (!toCard.value || !amount.value) {
    msg.value = "请输入对方卡号和转账金额";
    return;
  }

  try {
    // 请求转账
    const res = await axios.post(`${import.meta.env.VITE_API_URL}/transfer`, {
      fromCard: acc.card,
      toCard: toCard.value,
      amount: Number(amount.value)
    });

    // 确保后端返回了新的余额
    if (!res.data || !res.data.data) {
      msg.value = "转账失败";
      return;
    }

    // 更新余额
    acc.balance = res.data.data;
    sessionStorage.setItem("account", JSON.stringify(acc));

    msg.value = `转账成功，当前余额：${acc.balance}元`;
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
@import "@/assets/styles/Transfer.css";
</style>
