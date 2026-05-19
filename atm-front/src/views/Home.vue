<template>
  <div class="home-container">
    <NavBar />

    <div class="home-content">

      <!-- 左侧账户信息卡片 -->
      <div class="info-card">
        <h2 class="title">账户信息</h2>

        <p><strong>户主：</strong><span id="name"></span></p>
        <p><strong>卡号：</strong><span id="card"></span></p>
        <p><strong>余额：</strong><span id="balance"></span> 元</p>
        <p><strong>性别：</strong><span id="gender"></span></p>
        <p><strong>每日限额：</strong><span id="limit"></span> 元</p>

        <div class="btn-group">
          <button class="btn" @click="$router.push('/deposit')">存款</button>
          <button class="btn" @click="$router.push('/withdraw')">取款</button>
          <button class="btn" @click="$router.push('/transfer')">转账</button>
          <button class="btn" @click="$router.push('/change-password')">修改密码</button>
        </div>
      </div>

      <!-- 右侧交易记录 -->
      <div class="record-card">
        <h2 class="title">最近交易记录（仅保留 10 条）</h2>
        <div id="recordList" class="record-list"></div>
      </div>

    </div>
  </div>
</template>

<script>
import "@/assets/styles/home.css"
import NavBar from "@/components/NavBar.vue";
import initHome from '@/assets/scripts/home.js';
import {createRouter as $router} from "vue-router";

export default {
  methods: {$router},
  components: { NavBar },

  mounted() {
    initHome();   // 🟦 调用 home.js 中的初始化函数
  },
};
</script>
