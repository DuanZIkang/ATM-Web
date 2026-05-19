<template>
  <footer class="icp-footer">
    <div class="copyright">
      © 2025 vaedu 模拟ATM示例 - 个人全栈学习项目
    </div>

    <div class="beian-container">
      <!-- ICP 备案 -->
      <a
          href="https://beian.miit.gov.cn/"
          target="_blank"
          rel="noreferrer"
          class="beian-link"
      >
        粤ICP备2025503821号-1
      </a>

      <span class="separator">|</span>

      <!-- 公安备案 + 小警徽图标 -->
      <a
          href="https://beian.mps.gov.cn/#/query/webSearch?code=44030002009173"
          target="_blank"
          rel="noreferrer"
          class="beian-link police-link"
      >
        <img
            src="../assets/icon/备案图标.png"
            alt="公安备案图标"
            class="police-icon"
        />
        粤公网安备44030002009173号
      </a>
    </div>
  </footer>
</template>

<script setup lang="ts">
// 可以留空，或者加一些动态备案号的逻辑
</script>

<style scoped>
.icp-footer {
  margin-top: 60px;
  padding: 20px 0;
  text-align: center;
  font-size: 14px;
  color: #999;
  background-color: #fafafa;
  border-top: 1px solid #eee;
}

.copyright {
  margin-bottom: 10px;
  font-size: 13px;
  color: #aaa;
}

.beian-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
}

.beian-link {
  color: #666;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: color 0.3s;
}

.beian-link:hover {
  color: #1e90ff;
}

.separator {
  color: #ddd;
}

.police-icon {
  height: 16px;
  width: 16px;
  vertical-align: middle;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .beian-container {
    flex-direction: column;
    gap: 8px;
  }
  .separator {
    display: none;
  }
}
</style>