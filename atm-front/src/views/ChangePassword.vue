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
import { encryptPassword } from "@/assets/scripts/encryption";

const oldPwd = ref("");
const newPwd = ref("");
const msg = ref("");
const router = useRouter();

async function changePwd() {
  // ✅ 从 userInfo 读取 card
  const userInfo = JSON.parse(sessionStorage.getItem("userInfo"));
  if (!userInfo) {
    router.push("/login");
    return;
  }

  try {
    const res = await axios.post(`${import.meta.env.VITE_API_URL}/change`, {
      card: userInfo.card,
      // ✅ 旧密码和新密码都 AES 加密后传输
      oldPwd: encryptPassword(oldPwd.value),
      newPwd: encryptPassword(newPwd.value)
    }, {
      headers: { token: sessionStorage.getItem("token") }
    });

    if (res.data.success) {
      msg.value = "修改成功";
      router.push("/home");
    } else {
      msg.value = res.data.message || "修改失败";
    }
  } catch (e) {
    msg.value = "服务器错误";
  }
}
</script>

<style scoped>
@import "@/assets/styles/ChangePassword.css";
</style>
