<script setup>
import request from "@/utils/request";
import { ref, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";

const accounts = ref([]);
const loading = ref(false);
const unlocking = ref(""); // 正在解锁的卡号，防止重复点击

async function loadLocked() {
  loading.value = true;
  try {
    const res = await request.get("/admin/lock");
    if (res.data.success) {
      accounts.value = res.data.data || [];
    } else {
      ElMessage.error(res.data.message || "加载失败");
    }
  } catch (e) {
    ElMessage.error(e.message || "服务器错误");
    accounts.value = [];
  } finally {
    loading.value = false;
  }
}

async function unlock(row) {
  try {
    await ElMessageBox.confirm(
        `确认解锁 ${row.name}（${row.card}）的账户吗？`,
        "解锁确认",
        { confirmButtonText: "确认解锁", cancelButtonText: "取消", type: "warning" }
    );
  } catch {
    return; // 用户取消
  }

  unlocking.value = row.card;
  try {
    const res = await request.post("/admin/unlock", { card: row.card });
    if (res.data.success) {
      ElMessage.success(res.data.data || "解锁成功");
      accounts.value = accounts.value.filter(a => a.card !== row.card);
    } else {
      ElMessage.error(res.data.message || "解锁失败");
    }
  } catch (e) {
    ElMessage.error(e.message || "服务器错误");
  } finally {
    unlocking.value = "";
  }
}

onMounted(loadLocked);
</script>

<template>
  <h2 class="page-title">锁定账户</h2>

  <div class="panel" v-loading="loading">
    <table class="tbl">
      <thead>
      <tr>
        <th style="width: 190px">卡号</th>
        <th style="width: 200px">户主</th>
        <th style="width: 215px">锁定时间</th>
        <th>状态</th>
        <th class="right">操作</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="row in accounts" :key="row.card">
        <td>{{ row.card }}</td>
        <td>{{ row.name }}</td>
        <!-- 接口暂未返回锁定时间，有 lockTime 字段时会自动显示 -->
        <td>{{ row.lockTime || "-" }}</td>
        <td>
          <span class="tag" v-if="row.status === 'LOCKED'">已锁定</span>
          <span v-else>{{ row.status }}</span>
        </td>
        <td class="right">
          <button
              class="btn-unlock"
              :disabled="unlocking === row.card"
              @click="unlock(row)"
          >
            {{ unlocking === row.card ? "解锁中…" : "解锁" }}
          </button>
        </td>
      </tr>
      <tr v-if="!loading && accounts.length === 0">
        <td colspan="5" class="empty">暂无锁定账户</td>
      </tr>
      </tbody>
    </table>
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
  background: #fff;
  border: 1px solid #e5eaf2;
  border-radius: 14px;
  padding: 10px 30px;
  min-height: 120px;
}
.tbl {
  width: 100%;
  border-collapse: collapse;
}
.tbl th {
  text-align: left;
  font-size: 12px;
  font-weight: 400;
  color: #8a94a6;
  padding: 26px 0 12px;
  border-bottom: 1px solid #e5eaf2;
}
.tbl td {
  font-size: 14px;
  color: #1f2d3d;
  height: 52px;
  border-bottom: 1px solid #e5eaf2;
}
.tbl tr:last-child td {
  border-bottom: none;
}
.right {
  text-align: right !important;
}
.tag {
  display: inline-block;
  padding: 0 12px;
  height: 24px;
  line-height: 24px;
  border-radius: 6px;
  background: #fff4db;
  color: #8a5a00;
  font-size: 12px;
}
.btn-unlock {
  width: 60px;
  height: 28px;
  border: none;
  border-radius: 6px;
  background: #0a66d9;
  color: #fff;
  font-size: 13px;
  cursor: pointer;
}
.btn-unlock:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.empty {
  text-align: center !important;
  color: #8a94a6 !important;
}
</style>
