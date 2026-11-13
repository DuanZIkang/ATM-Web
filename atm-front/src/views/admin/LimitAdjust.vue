<script setup>
import request from "@/utils/request";
import { ref } from "vue";
import { ElMessage } from "element-plus";


const card = ref("");
const account = ref(null); // 查询到的账户信息
const minLimit = ref("");
const maxLimit = ref("");
const searching = ref(false);
const submitting = ref(false);

const isPositiveInt = v => /^\d+$/.test(String(v)) && Number(v) > 0;

async function search() {
  const c = card.value.trim();
  if (!c) {
    ElMessage.warning("请输入卡号");
    return;
  }
  searching.value = true;
  try {
    const res = await request.get("/admin/limit", { params: { card: c } });
    if (res.data.success) {
      account.value = res.data.data;
      // 输入框默认带出当前上下限，方便在原值上改
      minLimit.value = account.value.minLimit ?? "";
      maxLimit.value = account.value.maxLimit ?? "";
    } else {
      account.value = null;
      ElMessage.error(res.data.message || "查询失败");
    }
  } catch (e) {
    account.value = null;
    ElMessage.error(e.message || "服务器错误");
  } finally {
    searching.value = false;
  }
}

async function submit() {
  if (!account.value) {
    ElMessage.warning("请先查询账户");
    return;
  }
  if (!isPositiveInt(minLimit.value) || !isPositiveInt(maxLimit.value)) {
    ElMessage.warning("最小、最大限额必须是正整数");
    return;
  }
  if (Number(minLimit.value) > Number(maxLimit.value)) {
    ElMessage.warning("最小限额不能大于最大限额");
    return;
  }

  submitting.value = true;
  try {
    const res = await request.post("/admin/limit", {
      card: account.value.card,
      minLimit: Number(minLimit.value),
      maxLimit: Number(maxLimit.value),
    });
    if (res.data.success) {
      ElMessage.success(res.data.data || "调整成功");
      await search(); // 重新查询，展示后端最终生效的数据
    } else {
      ElMessage.error(res.data.message || "调整失败");
    }
  } catch (e) {
    ElMessage.error(e.message || "服务器错误");
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <h2 class="page-title">限额调整</h2>

  <div class="panel">
    <div class="row">
      <input v-model="card" class="input" placeholder="输入卡号" @keyup.enter="search" />
      <button class="btn-outline" :disabled="searching" @click="search">
        {{ searching ? "查询中…" : "查询" }}
      </button>
    </div>

    <template v-if="account">
      <div class="info">
        <div>户主：{{ account.name }}</div>
        <div>卡号：{{ account.card }}</div>
        <div>当前每日限额：{{ account.dailyLimit }} 元</div>
        <div>状态：{{ account.status === "LOCKED" ? "已锁定" : "正常" }}</div>
        <div class="span2">
          当前可调范围：{{ account.minLimit }} ~ {{ account.maxLimit }} 元
        </div>
      </div>

      <label class="label">最小限额（元）</label>
      <input v-model="minLimit" class="input full" placeholder="例如 1000" />

      <label class="label">最大限额（元）</label>
      <input v-model="maxLimit" class="input full" placeholder="例如 50000" />

      <p class="hint">用户只能在该范围内自行修改每日限额，修改后立即生效。</p>

      <button class="btn-primary" :disabled="submitting" @click="submit">
        {{ submitting ? "提交中…" : "确认调整" }}
      </button>
    </template>
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
.span2 {
  grid-column: 1 / -1;
}
.label {
  display: block;
  font-size: 12px;
  color: #8a94a6;
  margin: 8px 0 10px;
}
.hint {
  font-size: 12px;
  color: #8a94a6;
  margin-top: 12px;
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
.btn-primary:disabled,
.btn-outline:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
