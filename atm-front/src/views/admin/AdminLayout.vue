<script setup>
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";

const route = useRoute();
const router = useRouter();

// 侧边栏菜单：以后新增接口/页面，只需在这里加一项，并在路由 children 里加对应页面
const menu = [
  {
    group: "账户管理",
    items: [
      { label: "锁定账户", path: "/admin/lock" },
      { label: "限额调整", path: "/admin/limit" },
    ],
  },
];

const adminName = computed(() => {
  try {
    return JSON.parse(sessionStorage.getItem("userInfo"))?.name || "admin";
  } catch {
    return "admin";
  }
});

function logout() {
  sessionStorage.clear();
  router.push("/login");
}
</script>

<template>
  <div class="admin">
    <header class="topbar">
      <div class="brand">ATM 管理后台</div>
      <div class="user">
        <span>{{ adminName }}</span>
        <button class="back" @click="router.push('/home')">返回首页</button>
        <button class="logout" @click="logout">退出</button>
      </div>
    </header>

    <aside class="sidebar">
      <div v-for="g in menu" :key="g.group" class="group">
        <div class="group-title">{{ g.group }}</div>
        <router-link
            v-for="it in g.items"
            :key="it.path"
            :to="it.path"
            class="menu-item"
            :class="{ active: route.path === it.path }"
        >
          {{ it.label }}
        </router-link>
      </div>
    </aside>

    <main class="content">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.back {
  height: 28px;
  padding: 0 16px;
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 6px;
  background: transparent;
  color: #fff;
  font-size: 13px;
  cursor: pointer;
  margin-right: -12px;
}
.back:hover {
  background: rgba(255, 255, 255, 0.15);
}
.admin {
  min-height: 100vh;
  background: #f5f8fd;
}

.topbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: #4a90e2;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  z-index: 10;
}
.brand {
  font-size: 18px;
  font-weight: 600;
}
.user {
  display: flex;
  align-items: center;
  gap: 24px;
  font-size: 13px;
}
.logout {
  height: 28px;
  padding: 0 16px;
  border: none;
  border-radius: 6px;
  background: #fff;
  color: #4a90e2;
  font-size: 13px;
  cursor: pointer;
}

.sidebar {
  position: fixed;
  top: 60px;
  bottom: 0;
  left: 0;
  width: 220px;
  background: #fff;
  border-right: 1px solid #e5eaf2;
  padding: 28px 12px;
  overflow-y: auto;
}
.group-title {
  font-size: 12px;
  color: #8a94a6;
  padding: 0 8px 10px;
}
.menu-item {
  display: block;
  height: 40px;
  line-height: 40px;
  padding: 0 20px;
  margin-bottom: 0;
  border-radius: 8px;
  font-size: 14px;
  color: #4a5568;
  text-decoration: none;
}
.menu-item:hover {
  background: #f3f6fb;
}
.menu-item.active {
  background: #e6f0fd;
  color: #0a66d9;
  font-weight: 600;
}

.content {
  margin-left: 220px;
  padding: 80px 40px 40px;
}
</style>
