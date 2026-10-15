<template>
  <nav class="nav">
    <div class="left" @click="$router.push('/home')">🏦 ATM 系统</div>

    <div class="right">
      <button class="logout" @click="logout">退出</button>
    </div>
  </nav>
</template>

<script>
export default {

  methods:{
    logout(){
      sessionStorage.clear();
      this.$router.push("/");
    }
  }
}
</script>

<style scoped>
.nav {
  height: 60px;
  background: var(--theme);
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
  font-size: 18px;
  position: fixed; /* 固定导航栏 */
  top: 0; /* 顶部 */
  width: 100%; /* 宽度为 100% */
  z-index: 10; /* 确保在页面上层 */
}

body {
  padding-top: 60px; /* 防止内容被导航栏遮挡 */
}


.left{
  cursor:pointer;
  font-weight: bold;
}
.right{
  display:flex;
  gap:12px;
  align-items:center;
}
.logout{
  background:white;
  color:var(--theme);
  padding:6px 10px;
  border-radius:6px;
  border:none;
}
</style>
