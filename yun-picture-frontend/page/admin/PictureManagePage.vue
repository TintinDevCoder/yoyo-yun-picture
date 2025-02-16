<template>
  <div id="pictureManagePage">
    <a-flex justify="space-between">
      <h2 style="text-align: center; flex: 1;">图片管理</h2> <!-- 标题居中 -->
      <a-space>
        <a-button type="primary" href="/add_picture" target="_blank">+ 创建图片</a-button>
        <a-button type="primary" href="/add_picture/batch" target="_blank" ghost>+ 批量创建图片</a-button>
      </a-space>
    </a-flex>
    <div style="margin-bottom: 16px" />
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="关键词">
        <a-input v-model:value="searchParams.searchText" placeholder="从名称和简介搜索" allow-clear />
      </a-form-item>
      <a-form-item label="类型">
        <a-input v-model:value="searchParams.category" placeholder="请输入类型" allow-clear />
      </a-form-item>
      <a-form-item label="标签">
        <a-select v-model:value="searchParams.tags" mode="tags" placeholder="请输入标签" style="min-width: 180px" allow-clear />
      </a-form-item>
      <a-form-item name="reviewStatus" label="审核状态">
        <a-select v-model:value="searchParams.reviewStatus" style="min-width: 180px" placeholder="请选择审核状态" :options="PIC_REVIEW_STATUS_OPTIONS" allow-clear />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
    </a-form>
    <div style="margin-bottom: 16px" />
    <!-- 表格 -->
    <a-table :columns="columns" :data-source="dataList" :pagination="pagination" @change="doTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'url'">
          <a-image :src="record.url" :width="150" /> <!-- 图片列加宽 -->
        </template>
        <template v-if="column.dataIndex === 'tags'">
          <a-space wrap>
            <a-tag v-for="tag in JSON.parse(record.tags || '[]')" :key="tag">{{ tag }}</a-tag>
          </a-space>
        </template>
        <template v-if="column.dataIndex === 'picInfo'">
          <div>格式：{{ record.picFormat }}</div>
          <div>宽度：{{ record.picWidth }}</div>
          <div>高度：{{ record.picHeight }}</div>
          <div>宽高比：{{ record.picScale }}</div>
          <div>大小：{{ (record.picSize / 1024).toFixed(2) }}KB</div>
        </template>
        <template v-if="column.dataIndex === 'reviewMessage'">
          <div>审核状态：{{ PIC_REVIEW_STATUS_MAP[record.reviewStatus] }}</div>
          <div>审核信息：{{ record.reviewMessage }}</div>
          <div>审核人：{{ record.reviewerId }}</div>
          <div v-if="record.reviewTime">审核时间：{{ dayjs(record.reviewTime).format('YYYY-MM-DD HH:mm:ss') }}</div>
        </template>
        <template v-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-if="column.dataIndex === 'editTime'">
          {{ dayjs(record.editTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space wrap>
            <a-button v-if="record.reviewStatus !== PIC_REVIEW_STATUS_ENUM.PASS" type="link" @click="showReviewModal(record, PIC_REVIEW_STATUS_ENUM.PASS)">通过</a-button>
            <a-button v-if="record.reviewStatus !== PIC_REVIEW_STATUS_ENUM.REJECT" type="link" danger @click="showReviewModal(record, PIC_REVIEW_STATUS_ENUM.REJECT)">拒绝</a-button>
            <a-button type="link" :href="`/add_picture?id=${record.id}`" target="_blank">编辑</a-button>
            <a-popconfirm title="您确定要删除这张图片吗？" @confirm="doDelete(record.id)">
              <a-button danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 审核原因模态框 -->
    <a-modal v-model:visible="isReviewModalVisible" title="审核原因" @ok="handleReviewConfirm" @cancel="handleReviewCancel">
      <a-form>
        <a-form-item label="审核原因">
          <a-input v-model:value="reviewReason" placeholder="请输入审核原因" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import {
  deletePictureUsingPost,
  doPictureReviewUsingPost,
  listPictureByPageUsingPost,
} from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import {
  PIC_REVIEW_STATUS_ENUM,
  PIC_REVIEW_STATUS_MAP,
  PIC_REVIEW_STATUS_OPTIONS
} from '@/constants/picture.ts'
import router from '@/router'

const columns = [
  {
    title: 'id',
    dataIndex: 'id',
    width: 80,
    align: 'center', // 居中对齐
  },
  {
    title: '图片',
    dataIndex: 'url',
    width: 150, // 图片列加宽
    align: 'center', // 居中对齐
  },
  {
    title: '名称',
    dataIndex: 'name',
    align: 'center', // 居中对齐
  },
  {
    title: '简介',
    dataIndex: 'introduction',
    ellipsis: true,
    align: 'center', // 居中对齐
  },
  {
    title: '类型',
    dataIndex: 'category',
    align: 'center', // 居中对齐
  },
  {
    title: '标签',
    dataIndex: 'tags',
    align: 'center', // 居中对齐
  },
  {
    title: '图片信息',
    dataIndex: 'picInfo',
    align: 'center', // 居中对齐
  },
  {
    title: '用户 id',
    dataIndex: 'userId',
    width: 80,
    align: 'center', // 居中对齐
  },
  {
    title: '空间 id',
    dataIndex: 'spaceId',
    width: 80,
    align: 'center', // 居中对齐
  },
  {
    title: '审核信息',
    dataIndex: 'reviewMessage',
    align: 'center', // 居中对齐
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    align: 'center', // 居中对齐
  },
  {
    title: '编辑时间',
    dataIndex: 'editTime',
    align: 'center', // 居中对齐
  },
  {
    title: '操作',
    key: 'action',
    align: 'center', // 居中对齐
  },
]

// 定义数据
const dataList = ref<API.Picture[]>([])
const total = ref(0)

// 搜索条件
const searchParams = reactive<API.PictureQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend',
})

// 模态框状态
const isReviewModalVisible = ref(false)
const reviewReason = ref('')
const currentReviewRecord = ref<API.Picture | null>(null)
const currentReviewStatus = ref<PIC_REVIEW_STATUS_ENUM | null>(null)

// 获取数据
const fetchData = async () => {
  const res = await listPictureByPageUsingPost({
    ...searchParams,
/*    nullSpaceId: true,*/
  })
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
}

// 页面加载时获取数据，请求一次
onMounted(() => {
  fetchData()
})

// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.current,
    pageSize: searchParams.pageSize,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total) => `共 ${total} 条`,
  }
})

// 表格变化之后，重新获取数据
const doTableChange = (page: any) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 搜索数据
const doSearch = () => {
  // 重置页码
  searchParams.current = 1
  fetchData()
}

// 删除数据
const doDelete = async (id: string) => {
  if (!id) {
    return
  }
  const res = await deletePictureUsingPost({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    // 刷新数据
    fetchData()
  } else {
    message.error('删除失败')
  }
}

// 显示审核模态框
const showReviewModal = (record: API.Picture, reviewStatus: PIC_REVIEW_STATUS_ENUM) => {
  currentReviewRecord.value = record
  currentReviewStatus.value = reviewStatus // 保存当前审核状态
  reviewReason.value = '' // 清空审核原因
  isReviewModalVisible.value = true
}

// 确认审核
const handleReviewConfirm = async () => {
  const record = currentReviewRecord.value
  if (!record || !reviewReason.value) {
    message.error('请填写审核原因')
    return
  }

  const res = await doPictureReviewUsingPost({
    id: record.id,
    reviewStatus: currentReviewStatus.value, // 使用当前审核状态
    reviewMessage: reviewReason.value,
  })

  if (res.data.code === 0) {
    message.success('审核操作成功')
    fetchData()
    isReviewModalVisible.value = false
  } else {
    message.error('审核操作失败，' + res.data.message)
  }
}

// 处理审核取消
const handleReviewCancel = () => {
  isReviewModalVisible.value = false
  reviewReason.value = ''
  currentReviewRecord.value = null
  currentReviewStatus.value = null // 清空审核状态
}
/*const picture = ref<API.PictureVO>({})
// 编辑
const doEdit = () => {
  router.push('/add_picture?id=' + picture.value.id)
}*/
</script>
