<script setup>
import { ref } from "vue";
import { ElMessage } from "element-plus";

const card = ref("");
const newLimit = ref("");
// 查询到的账户信息：{ name, card, dailyLimit, status }
const account = ref(null);

// TODO: 限额接口尚未提供，接口确定后在这两个函数里调用 request 即可
function search() {
  if (!card.value.trim()) {
    ElMessage.warning("请输入卡号");
    return;
  }
  ElMessage.info("查询接口尚未接入");
}

function submit() {
  if (!account.value) {
    ElMessage.warning("请先查询账户");
    return;
  }
  if (!/^\d+$/.test(newLimit.value) || Number(newLimit.value) <= 0) {
    ElMessage.warning("请输入有效的每日限额");
    return;
  }
  ElMessage.info("调整接口尚未接入");
}
</script>

<template>
  <h2 class="page-title">限额调整</h2>

  <div class="panel">
    <div class="row">
      <input v-model="card" class="input" placeholder="输入卡号" @keyup.enter="search" />
      <button class="btn-outline" @click="search">查询</button>
    </div>

    <div class="info" v-if="account">
      <div>户主：{{ account.name }}</div>
      <div>卡号：{{ account.card }}</div>
      <div>当前每日限额：{{ account.dailyLimit }} 元</div>
      <div>状态：{{ account.status === "LOCKED" ? "已锁定" : "正常" }}</div>
    </div>

    <label class="label">新的每日限额（元）</label>
    <input v-model="newLimit" class="input full" placeholder="50000" />
    <button class="btn-primary" @click="submit">确认调整</button>
  </div>
</template>

<style scoped>
.page-title {
  font-size: 22px;
  font-weight: 600;
  color: #0a4fa3;
  margin-bottom: 28px;
}
.panel {
  width: 480px;
  max-width: 100%;
  background: #fff;
  border: 1px solid #e5eaf2;
  border-radius: 14px;
  padding: 24px;
}
.row {
  display: flex;
  gap: 12px;
  margin-bottom: 18px;
}
.input {
  flex: 1;
  height: 40px;
  padding: 0 14px;
  border: 1px solid #d5dce8;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
}
.input:focus {
  border-color: #0a66d9;
}
.full {
  width: 100%;
  flex: none;
  display: block;
}
.btn-outline {
  width: 120px;
  height: 40px;
  border: 1px solid #0a66d9;
  border-radius: 8px;
  background: #fff;
  color: #0a66d9;
  font-size: 14px;
  cursor: pointer;
}
.info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px 16px;
  background: #f3f6fb;
  border-radius: 8px;
  padding: 16px;
  font-size: 13px;
  color: #1f2d3d;
  margin-bottom: 24px;
}
.label {
  display: block;
  font-size: 12px;
  color: #8a94a6;
  margin: 8px 0 10px;
}
.btn-primary {
  width: 100%;
  height: 42px;
  margin-top: 18px;
  border: none;
  border-radius: 8px;
  background: #0a66d9;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}
</style>
