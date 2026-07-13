<script setup>
import NavBar from "@/components/NavBar.vue";
import axios from "axios";
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const records = ref([]);
const loading = ref(false);

// VITE_API_URL 是 /api/atm 或 https://xxx/api/atm，去掉 /atm 得到 /api 前缀
const API_BASE = import.meta.env.VITE_API_URL.replace(/\/atm$/, "");

function isIncome(type) {
  return type === "DEPOSIT" || type === "TRANSFER_IN";
}

function formatAmount(type, amount) {
  return (isIncome(type) ? "+ " : "- ") + amount;
}

function formatTime(time) {
  return time ? time.replace("T", " ") : "";
}

async function loadRecords() {
  const userInfo = JSON.parse(sessionStorage.getItem("userInfo"));
  const token = sessionStorage.getItem("token");
  if (!userInfo || !token) {
    router.push("/login");
    return;
  }

  loading.value = true;
  try {
    const res = await axios.get(`${API_BASE}/transactions/all`, {
      params: { card: userInfo.card },
      headers: { token }
    });
    // 该接口直接返回数组，不是 Result 包装
    records.value = res.data || [];
  } catch (e) {
    records.value = [];
  } finally {
    loading.value = false;
  }
}

onMounted(loadRecords);
</script>

<template>
  <div class="page">
    <NavBar />

    <div class="card-container">
      <div class="card">
        <h2 class="title">全部交易记录</h2>

        <el-table
            :data="records"
            stripe
            height="520"
            style="width: 100%"
            v-loading="loading"
            empty-text="没有交易记录"
        >
          <el-table-column label="金额" width="150" align="center">
            <template #default="{ row }">
              <span :class="isIncome(row.type) ? 'income' : 'expense'">
                {{ formatAmount(row.type, row.amount) }}
              </span>
            </template>
          </el-table-column>

          <el-table-column prop="remark" label="备注" min-width="180" />

          <el-table-column label="时间" width="200" align="center">
            <template #default="{ row }">
              {{ formatTime(row.time) }}
            </template>
          </el-table-column>
        </el-table>

        <el-button class="back-btn" @click="router.push('/home')">返回首页</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.card-container {
  display: flex;
  justify-content: center;
  padding: 20px;
}

.card {
  width: 800px;
  background: #fff;
  padding: 25px;
  border-radius: 12px;
  box-shadow: var(--shadow);
}

.title {
  margin-bottom: 15px;
}

.income {
  color: #67c23a;
  font-weight: bold;
}

.expense {
  color: #f56c6c;
  font-weight: bold;
}

.back-btn {
  margin-top: 15px;
}
</style>