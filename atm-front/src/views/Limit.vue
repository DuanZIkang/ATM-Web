<template>
  <div class="page">
    <NavBar />

    <div class="card-container">
      <div class="card">
        <h2>调整每日限额</h2>

        <p class="line">当前每日限额：<strong>{{ current }}</strong> 元</p>
        <p class="line">可调范围：<strong>{{ minLimit }} ~ {{ maxLimit }}</strong> 元</p>

        <input
            v-model="newLimit"
            class="input"
            :placeholder="`请输入新的每日限额（${minLimit} ~ ${maxLimit}）`"
            @keyup.enter="submit"
        />
        <p v-if="limitError" class="err">{{ limitError }}</p>

        <button class="btn" :disabled="!!limitError || !newLimit || loading" @click="submit">
          确认修改
        </button>
        <button class="btn secondary" @click="router.push('/home')">返回</button>

        <p v-if="msg" class="msg">{{ msg }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import NavBar from "@/components/NavBar.vue";
import request from "@/utils/request";
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import {ElMessage} from "element-plus";

const router = useRouter();
const current = ref("-");
const minLimit = ref("-");
const maxLimit = ref("-");
const newLimit = ref("");
const msg = ref("");
const loading = ref(false);

const limitError = computed(() => {
  if (!newLimit.value) return "";
  if (!/^\d+$/.test(newLimit.value)) return "请输入正整数";
  const n = Number(newLimit.value);
  if (n < Number(minLimit.value) || n > Number(maxLimit.value)) {
    return `限额必须在 ${minLimit.value} ~ ${maxLimit.value} 元之间`;
  }
  return "";
});

function getUser() {
  const userInfo = JSON.parse(sessionStorage.getItem("userInfo"));
  if (!userInfo) router.push("/login");
  return userInfo;
}

async function loadInfo() {
  const userInfo = getUser();
  if (!userInfo) return;
  try {
    const res = await request.get("/info", { params: { card: userInfo.card } });
    const d = res.data.data;
    current.value = d.dailyLimit;
    minLimit.value = d.minLimit;
    maxLimit.value = d.maxLimit;
  } catch (e) {
    msg.value = e.message || "加载失败";
  }
}

async function submit() {
  if (limitError.value || !newLimit.value) return;
  const userInfo = getUser();
  if (!userInfo) return;

  loading.value = true;
  msg.value = "";
  try {
    const res = await request.post("/limit", {
      card: userInfo.card,
      dailyLimit: Number(newLimit.value),
    });
    if (res.data.success) {
      userInfo.dailyLimit = Number(newLimit.value);
      sessionStorage.setItem("userInfo", JSON.stringify(userInfo));
      ElMessage.success(res.data.data || "修改成功")
      setTimeout(() => router.push("/home"), 500);
    } else {
      ElMessage.error(res.data.message || "修改失败")
    }
  } catch (e) {
    msg.value = e.message || "服务器错误";
  } finally {
    loading.value = false;
  }
}

onMounted(loadInfo);
</script>

<style scoped>
@import "@/assets/styles/Withdraw.css";
.card-container {
  display: flex;
  justify-content: center;
  padding: 20px;
}
.card {
  width: 400px;
  background: #fff;
  padding: 25px;
  border-radius: 12px;
  box-shadow: var(--shadow);
}
.line {
  margin-bottom: 10px;
  color: #4a5568;
}
.input {
  margin-top: 14px;
}
.err {
  color: red;
  font-size: 14px;
  margin: -12px 0 12px;
}
.btn {
  margin-bottom: 12px;
}
.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
