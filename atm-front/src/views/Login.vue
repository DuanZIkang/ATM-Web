<template>
  <div class="login-container">
    <div class="login-card">
      <h2 class="login-title">账户登录</h2>

      <div class="form-group">
        <label>卡号</label>
        <input id="card" type="text" v-model="card" class="input" placeholder="请输入卡号">
      </div>

      <div class="form-group">
        <label>密码</label>
        <input id="password" type="password" v-model="password" class="input" placeholder="请输入密码">
      </div>

      <button class="btn" @click="doLogin">登录</button>
      <p class="msg">{{ msg }}</p>
      <button class="btn-text" @click="$router.push('/register')">
        没有账户？去开户 →
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import axios from "axios";
import { useRouter} from "vue-router";
import { encryptPassword } from "@/assets/scripts/encryption";

const card = ref("");
const password = ref("");
const msg = ref("");
const router = useRouter();
async function doLogin() {
  msg.value = "";
  try {
    const encryptedPassword = encryptPassword(password.value);
    const api = import.meta.env.VITE_API_URL;

    const res = await axios.post(`${api}/login`, {
      card: card.value,
      password: encryptedPassword
    });

    if (!res.data.success) {
      msg.value = res.data.message || "登录失败";
      return;
    }

    const data = res.data.data;

    // ✅ 只存 token，其余存展示用的非敏感信息，彻底不存密码
    sessionStorage.setItem("token", data.token);
    sessionStorage.setItem("userInfo", JSON.stringify({
      card: data.card,
      name: data.name,
      sex: data.sex,
      dailyLimit: data.dailyLimit,
      balance: data.balance
    }));

    await router.push("/home");
  } catch (e) {
    msg.value = "服务器错误";
  }
}
</script>

<style scoped>
@import "@/assets/styles/login.css";
</style>