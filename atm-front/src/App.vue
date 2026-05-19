<template>
  <div id="app">
    <router-view />

    <!-- 🟦 全局备案号 -->
    <IcpFooter />
  </div>
</template>

<script>
import IcpFooter from "@/components/IcpFooter.vue";

export default {
  components: { IcpFooter }
}
</script>

<style>
/* 这里无需修改 */
</style>
