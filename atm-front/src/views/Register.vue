<template>
  <div class="register-page">

    <NavBar />

    <div class="register-card">
      <h2 class="title">开户申请</h2>

      <div class="input-group">
        <label>姓名</label>
        <input v-model="name" class="input" placeholder="请输入姓名" />
      </div>

      <div class="input-group">
        <label>密码</label>
        <input type="password" v-model="password" class="input" placeholder="请输入密码" />
      </div>

      <div class="input-group">
        <label>确认密码</label>
        <input type="password" v-model="confirm" class="input" placeholder="再次输入密码" />
      </div>

      <div class="input-group">
        <label>性别</label>
        <select v-model="sex" class="input">
          <option value="">请选择</option>
          <option value="男">男</option>
          <option value="女">女</option>
        </select>
      </div>

      <button class="btn primary" @click="submitRegister">提交开户申请</button>

      <p v-if="msg" class="msg">{{ msg }}</p>

      <button class="btn secondary" @click="$router.push('/login')">返回登录</button>
    </div>
  </div>
</template>

<script>
import axios from "axios";
import NavBar from "@/components/NavBar.vue";
import { encryptPassword } from "@/assets/scripts/encryption";

export default {
  components: { NavBar },

  data() {
    return {
      name: "",
      password: "",
      confirm: "",
      sex: "",
      msg: ""
    };
  },

  methods: {
    async submitRegister() {
      this.msg = "";

      if (!this.name || !this.password || !this.confirm || !this.sex) {
        this.msg = "请填写所有字段";
        return;
      }

      if (this.password !== this.confirm) {
        this.msg = "两次密码不一致";
        return;
      }

      try {
        const res = await axios.post(`${import.meta.env.VITE_API_URL}/register`, {
          name: this.name,
          password: encryptPassword(this.password),
          balance: 0,
          dailyLimit: 20000,   // 确保字段与后端一致
          sex: this.sex
        });

        const acc = res.data.data; // 后端返回完整 account 对象

        alert(
            `开户成功！您的卡号是：${acc.card}\n\n` +
            `请妥善保管，登录时需要使用此卡号。\n点击确定进入首页。`
        );

        // 必须保存到 localStorage，否则首页无法加载数据
        sessionStorage.setItem("account", JSON.stringify(acc));

        this.$router.push("/home");

      } catch (e) {
        this.msg = e.response?.data || "服务器错误";
      }
    }
  }
};
</script>

<style src="@/assets/styles/register.css"></style>
