<template>
  <div class="home-container">
    <NavBar/>

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
          <el-button class="btn" @click="router.push('/deposit')">存款</el-button>
          <el-button class="btn" @click="router.push('/withdraw')">取款</el-button>
          <el-button class="btn" @click="router.push('/transfer')">转账</el-button>
          <el-button class="btn" @click="router.push('/change-password')">修改密码</el-button>
          <el-button class="btn" @click="router.push('/transaction-records')">交易记录</el-button>
          <el-button class="btn" @click="dialogVisible = true">注销账户</el-button>
        </div>
      </div>

      <!-- 右侧交易记录 -->
      <div class="record-card">
        <h2 class="title">最近交易记录（最近 10 条）</h2>
        <div id="recordList" class="record-list"></div>
      </div>

    </div>
  </div>
  <el-dialog
      v-model="dialogVisible"
      :title="step === 1 ? '注销账户' : '验证账户'"
      width="420"
      align-center
      destroy-on-close
      :close-on-click-modal="false"
      @closed="reset"
  >
    <!-- 第一步：提示 -->
    <div v-if="step === 1" class="tip">
      <el-icon class="tip-icon" :size="24"><WarningFilled /></el-icon>
      <div>
        <p class="tip-title">确认要注销账户吗？</p>
        <p class="tip-desc">
          注销前请确保账户中已经没有余额，否则将会注销失败。
        </p>
      </div>
    </div>

    <!-- 第二步：验证密码 -->
    <el-form v-else label-position="top" @submit.prevent>
      <el-form-item label="登录密码">
        <el-input
            v-model="account.password"
            type="password"
            placeholder="请输入密码"
            show-password
            autofocus
            @keyup.enter="handleSubmit"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button v-if="step === 1" type="danger" @click="step = 2">
        继续
      </el-button>
      <el-button
          v-else
          type="danger"
          @click="handleSubmit"
      >
        确认注销
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import "@/assets/styles/home.css"
import NavBar from "@/components/NavBar.vue";
import {useRouter} from "vue-router";
import {ref, onMounted} from "vue";
import axios from "axios";
import {ElMessage} from "element-plus";
import { WarningFilled } from '@element-plus/icons-vue'
import {encryptPassword} from "@/assets/scripts/encryption.js";
import request from "@/utils/request.js";
const msg = ref("");

// 路由
const router = useRouter();

// Dialog 状态
const dialogVisible = ref(false);
const step = ref(1);

// 注销账户表单数据（用于提交后端）
const account = ref({
  card: "",
  password: ""
});

// 重置对话框状态
const reset = () => {
  step.value = 1;
  account.value.password = "";
};

// 处理注销提交
const handleSubmit = async () => {
  if (!account.value.password) {
    ElMessage.warning('请输入密码');
    return;
  }

  // 加密密码
  const encryptedPassword = encryptPassword(account.value.password);

  try {
    const res = await request.post("/close", {
      card: account.value.card,
      password: encryptedPassword
    });

    // 关闭对话框
    dialogVisible.value = false;

    if (res.data.success) {
      // 成功：显示成功消息并跳转
      ElMessage.success(res.data.message || '注销成功');
      alert("注销成功,7天后将彻底删除您的数据，若中途反悔可重新登陆以取消")
      setTimeout(() => {
        sessionStorage.clear();
        router.push('/login');
      }, 1500);
    } else {
      // 失败：显示错误消息
      ElMessage.error(res.data.message || "注销失败");
    }
  } catch (e) {
    // 关闭对话框
    dialogVisible.value = false;
    ElMessage.error(e?.message || '注销失败');
  }
};

/**
 * 初始化主页
 * 验证用户登录状态，加载账户信息和交易记录
 */
async function initHome() {
  // 从 sessionStorage 获取登录令牌
  const token = sessionStorage.getItem("token");
  if (!token) {
    // 未登录则跳转到登录页
    window.location.href = "/#/login";
    return;
  }

  // 获取用户信息
  const userInfo = JSON.parse(sessionStorage.getItem("userInfo"));
  if (!userInfo) {
    // 用户信息不存在则跳转到登录页
    window.location.href = "/#/login";
    return;
  }

  // 提取银行卡号
  const card = userInfo.card;
  // 并行加载账户信息和交易记录
  await loadAccountInfo(card, token);
  await loadRecords(card, token);
}

/**
 * 加载账户信息
 * @param {string} card - 银行卡号
 * @param {string} token - 认证令牌
 */
async function loadAccountInfo(card, token) {
  try {
    // 发送请求获取账户详细信息
    const res = await axios.get(`${import.meta.env.VITE_API_URL}/info?card=` + card, {
      headers: {token}
    });
    const data = res.data.data;

    // 更新页面上的账户信息显示
    document.getElementById("name").textContent = data.name;
    document.getElementById("card").textContent = data.card;
    document.getElementById("balance").textContent = data.balance;
    document.getElementById("gender").textContent = data.sex || "—";
    document.getElementById("limit").textContent = data.dailyLimit || 0;

    // 同步更新 sessionStorage 中的余额信息，保持数据一致性
    const userInfo = JSON.parse(sessionStorage.getItem("userInfo"));
    userInfo.balance = data.balance;
    sessionStorage.setItem("userInfo", JSON.stringify(userInfo));

    // 设置 account.card 用于注销提交
    account.value.card = data.card;
  } catch (err) {
    // 请求失败时提示用户
    alert("账号信息加载失败");
  }
}

/**
 * 加载交易记录
 * @param {string} card - 银行卡号
 * @param {string} token - 认证令牌
 */
async function loadRecords(card, token) {
  try {
    // 获取该卡号的所有交易记录
    const res = await axios.get(`${import.meta.env.VITE_API_URL}/transactions?card=` + card, {
      headers: {token}
    });
    const records = res.data.data;
    const list = document.getElementById("recordList");
    list.innerHTML = "";

    // 如果没有交易记录，显示提示信息
    if (!records || records.length === 0) {
      list.innerHTML = "<p>没有交易记录</p>";
      return;
    }

    // 遍历交易记录，动态生成 HTML 元素
    records.forEach(t => {
      const div = document.createElement("div");
      div.className = "record-item";
      // 构建交易记录的 HTML 结构（左侧：类型和备注，右侧：金额和时间）
      div.innerHTML = `
        <div class="left">
          <span class="type">${mapType(t.type)}</span>
          <span class="remark">${t.remark}</span>
        </div>
        <div class="right">
          <span class="amount">${formatAmount(t.type, t.amount)}</span>
          <span class="time">${t.time.replace("T", " ")}</span>
        </div>
      `;
      // 将记录项添加到列表中
      list.appendChild(div);
    });
  } catch (err) {
    // 请求失败时显示错误提示
    document.getElementById("recordList").innerHTML = "<p>记录加载失败</p>";
  }
}

/**
 * 将交易类型映射为中文显示
 * @param {string} type - 交易类型英文标识
 * @returns {string} 交易类型的中文名称
 */
function mapType(type) {
  switch (type) {
    case "DEPOSIT":
      return "存款";       // 存款操作
    case "WITHDRAW":
      return "取款";      // 取款操作
    case "TRANSFER_OUT":
      return "转出";  // 转账转出
    case "TRANSFER_IN":
      return "转入";   // 转账转入
    default:
      return type;                 // 未知类型返回原值
  }
}

/**
 * 格式化金额显示（收入显示 +，支出显示 -）
 * @param {string} type - 交易类型
 * @param {number} amount - 交易金额
 * @returns {string} 格式化后的金额字符串
 */
function formatAmount(type, amount) {
  // 存款和转入为收入，显示 + 号
  if (type === "DEPOSIT" || type === "TRANSFER_IN")
    return "+ " + amount;
  // 取款和转出为支出，显示 - 号
  return "- " + amount;
}

// 组件挂载时初始化
onMounted(() => {
  initHome();
});
</script>
<style scoped>
.tip {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}
.tip-icon {
  color: var(--el-color-warning);
  margin-top: 2px;
}
.tip-title {
  margin: 0 0 6px;
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}
.tip-desc {
  margin: 0;
  line-height: 1.6;
  color: var(--el-text-color-regular);
}
</style>
