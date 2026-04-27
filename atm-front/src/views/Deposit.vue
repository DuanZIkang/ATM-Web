<template>
  <div class="page">
    <NavBar />

    <div class="card-container">
      <div class="card">
        <h2>存款</h2>

        <input v-model="amount" class="input" placeholder="请输入金额" />

        <button class="btn" @click="doDeposit">确认存款</button>
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

const amount = ref("");
const router = useRouter();

async function doDeposit() {
  const acc = JSON.parse(localStorage.getItem("account"));
  if (!acc) return;

  await axios.post(`${import.meta.env.VITE_API_URL}/deposit`, {
    card: acc.card,
    amount: Number(amount.value)
  });

  router.push("/home");
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
