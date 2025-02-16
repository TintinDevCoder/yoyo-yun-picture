<template>
  <div>
    <a-modal v-model:visible="visible" :title="title" :footer="false" @cancel="closeModal">
      <h4>复制分享链接</h4>
      <a-typography-link copyable>
        {{ link }}
      </a-typography-link>
      <div style="margin-bottom: 16px" />
      <h4>手机扫码查看</h4>
      <a-qrcode :value="link" />
      <div style="display: flex; justify-content: center; margin-top: 16px;">
        <a-button type="primary" @click="addShare(link)">分享图片</a-button>
      </div>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  deletePictureUsingPost,
  setSharePictureByIdUsingPost
} from '@/api/pictureController.ts'

interface Props {
  title: string;
  link: string;
}

const props = withDefaults(defineProps<Props>(), {
  title: "分享图片",
  link: 'https://github.com/TintinDevCoder'
})

// 是否可见
const visible = ref(false)

// 打开弹窗
const openModal = () => {
  visible.value = true
}

// 关闭弹窗
const closeModal = () => {
  visible.value = false;
}

// 暴露函数给父组件
defineExpose({
  openModal,
})


import { setSharePictureByIdUsingPost } from '@/api/pictureController.ts';
// 新增分享图片
const addShare = async (link: string) => {
  try {
    // 构建参数对象
    const params = { url: link }; // 将传入的 link 赋值给 url

    // 调用 API 接口
    const response = await setSharePictureByIdUsingPost(params);
    message.success('分享链接添加成功！');
  } catch (error) {
    message.error('请求失败: ' + error.message);
  }
}
</script>
