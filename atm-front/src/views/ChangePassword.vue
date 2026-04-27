<template>
  <div class="page">
    <NavBar />

    <div class="card-container">
      <div class="card">
        <h2>修改密码</h2>

        <input v-model="oldPwd" class="input" placeholder="旧密码" />
        <input v-model="newPwd" class="input" placeholder="新密码" />

        <button class="btn" @click="changePwd">确认修改</button>
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

const oldPwd = ref("");
const newPwd = ref("");
const router = useRouter();

async function changePwd() {
  const acc = JSON.parse(localStorage.getItem("account"));
  if (!acc) return;

  await axios.post(`${import.meta.env.VITE_API_URL}/change`, {
    card: acc.card,
    oldPwd: oldPwd.value,
    newPwd: newPwd.value
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
<style scoped>
@import "@/assets/styles/ChangePassword.css";
</style>
