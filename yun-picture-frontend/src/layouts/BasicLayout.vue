<template>
  <div id="basicLayout">
    <a-layout style="min-height: 100vh">
      <a-layout-header class="header">
        <GlobalHeader />
      </a-layout-header>
      <a-layout>
        <GlobalSider class="sider" />
        <a-layout-content class="content">
          <router-view />
        </a-layout-content>
      </a-layout>
      <a-layout-footer class="footer">
        <div  v-if="loginUserStore.loginUser.id" class="footer-left">ID: {{ loginUserStore.loginUser.id}}</div>
        <div class="footer-center">
          <a href="https://github.com/TintinDevCoder" target="_blank">悠悠云图库 by 程序员丁丁</a>
        </div>
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalSider from "@/components/GlobalSider.vue";
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'

// 引入用户状态
const loginUserStore = useLoginUserStore();
</script>

<style scoped>
#basicLayout .header {
  padding-inline: 20px;
  background: white;
  color: unset;
  margin-bottom: 1px;
}

#basicLayout .sider {
  background: #fff;
  border-right: 0.5px solid #eee;
  padding-top: 20px;
}

#basicLayout :deep(.ant-menu-root) {
  border-bottom: none !important;
  border-inline-end: none !important;
}

#basicLayout .content {
  padding: 28px;
  background: linear-gradient(to right, #fefefe, #fff);
  margin-bottom: 28px;
}

#basicLayout .footer {
  background: #efefef;
  padding: 16px;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex; /* 使用 flexbox */
  align-items: center; /* 垂直居中 */
}

.footer-left {
  margin-left: 20px; /* 左侧留一点边距 */
  margin-right: auto; /* 将左侧内容推到最左边 */
}

.footer-center {
  flex-grow: 1; /* 向中间扩展 */
  text-align: center; /* 居中对齐 */
}
</style>
