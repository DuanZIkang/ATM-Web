<template>
  <div class="container">
    <NavBar/>

    <div class="card">
      <h2>账户信息</h2>

      <p>姓名：{{ info.name }}</p>
      <p>卡号：{{ info.card }}</p>
      <p>余额：{{ info.balance }} 元</p>
      <p>每日限额：{{ info.dailyLimit }} 元</p>
      <p>性别：{{ info.sex }}</p>

      <button class="btn" @click="$router.push('/home')">返回首页</button>
    </div>
  </div>
</template>

<script>
import NavBar from '@/components/NavBar.vue';
import axios from 'axios';
import { createRouter as $router } from "vue-router";

export default {
  methods: { $router },
  components: { NavBar },
  data() {
    return {
      info: {}
    }
  },
  async created() {
    // ✅ 从 userInfo 读取 card
    const userInfo = JSON.parse(sessionStorage.getItem("userInfo"));
    if (!userInfo) {
      window.location.href = "/#/login";
      return;
    }

    const r = await axios.get(`${import.meta.env.VITE_API_URL}/info`, {
      params: { card: userInfo.card },
      headers: { token: sessionStorage.getItem("token") }
    });

    if (r.data.success) {
      this.info = r.data.data;
    }
  }
}
</script>
