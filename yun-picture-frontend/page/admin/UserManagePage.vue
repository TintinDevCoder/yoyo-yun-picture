<template>
  <div id="userManagePage">
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="账号">
        <a-input v-model:value="searchParams.userAccount" placeholder="输入账号" />
      </a-form-item>
      <a-form-item label="用户名">
        <a-input v-model:value="searchParams.userName" placeholder="输入用户名" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
    </a-form>

    <a-table
      :columns="columns"
      :data-source="dataList"
      :pagination="pagination"
      @change="doTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'userAvatar'">
          <a-image :src="record.userAvatar" :width="120" />
        </template>
        <template v-else-if="column.dataIndex === 'userRole'">
          <div v-if="record.userRole === 'admin'">
            <a-tag color="green">管理员</a-tag>
          </div>
          <div v-else>
            <a-tag color="blue">普通用户</a-tag>
          </div>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-button @click="doEdit(record)">编辑</a-button>
          <a-button danger @click="doDelete(record.id)">删除</a-button>
        </template>
      </template>
    </a-table>

    <!-- 编辑用户信息的模态框 -->
    <a-modal v-model:visible="isEditModalVisible" title="编辑用户信息" @ok="handleEdit">
      <a-form :model="editUserData">
        <a-form-item label="用户名">
          <a-input v-model:value="editUserData.userName" placeholder="输入用户名" />
        </a-form-item>
        <a-form-item label="头像">
          <a-input v-model:value="editUserData.userAvatar" placeholder="输入头像 URL" />
        </a-form-item>
        <a-form-item label="简介">
          <a-input v-model:value="editUserData.userProfile" placeholder="输入用户简介" />
        </a-form-item>
        <a-form-item label="角色">
          <a-select v-model:value="editUserData.userRole" placeholder="选择用户角色">
            <a-select-option value="admin">管理员</a-select-option>
            <a-select-option value="user">普通用户</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { deleteUserByIdUsingPost, listUserVoByPageUsingPost, updateUserUsingPost } from '@/api/userController.ts';
import dayjs from 'dayjs';

const columns = [
  { title: 'id', dataIndex: 'id', align: 'center' },
  { title: '账号', dataIndex: 'userAccount', align: 'center' },
  { title: '用户名', dataIndex: 'userName', align: 'center' },
  { title: '头像', dataIndex: 'userAvatar', align: 'center' },
  { title: '简介', dataIndex: 'userProfile', align: 'center' },
  { title: '用户角色', dataIndex: 'userRole', align: 'center' },
  { title: '创建时间', dataIndex: 'createTime', align: 'center' },
  { title: '操作', key: 'action', align: 'center' },
];

// 数据
const dataList = ref<API.UserVO[]>([]);
const total = ref(0);
const isEditModalVisible = ref(false);
const editUserData = reactive<API.UserUpdateDTO>({
  userName: '',
  userAvatar: '',
  userProfile: '',
  userRole: '',
  id: undefined, // `id` 为可选属性
});

// 搜索条件
const searchParams = reactive<API.UserQueryDTO>({
  current: 1,
  pageSize: 10,
});

// 获取数据
const fetchData = async () => {
  const res = await listUserVoByPageUsingPost({ ...searchParams });
  if (res.data.data) {
    dataList.value = res.data.data.records ?? [];
    total.value = res.data.data.total ?? 0;
  } else {
    message.error('获取数据失败，' + res.data.message);
  }
};

// 分页参数
const pagination = computed(() => ({
  current: searchParams.current ?? 1,
  pageSize: searchParams.pageSize ?? 10,
  total: total.value,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条`,
}));

// 表格变化处理
const doTableChange = (page: any) => {
  searchParams.current = page.current;
  searchParams.pageSize = page.pageSize;
  fetchData();
};

// 页面加载时请求一次
onMounted(() => {
  fetchData();
});

// 搜索
const doSearch = () => {
  searchParams.current = 1;
  fetchData();
};

// 删除数据
const doDelete = async (id: string) => {
  if (!id) return;
  const res = await deleteUserByIdUsingPost({ id });
  if (res.data.code === 0) {
    message.success('删除成功');
    fetchData();
  } else {
    message.error('删除失败');
  }
};

// 编辑用户信息
const doEdit = (record: API.UserVO) => {
  editUserData.userName = record.userName;
  editUserData.userAvatar = record.userAvatar;
  editUserData.userProfile = record.userProfile;
  editUserData.userRole = record.userRole;
  editUserData.id = record.id;
  isEditModalVisible.value = true;
};

// 处理编辑提交
const handleEdit = async () => {
  const res = await updateUserUsingPost(editUserData);
  if (res.data.code === 0) {
    message.success('用户信息更新成功');
    isEditModalVisible.value = false;
    fetchData(); // 刷新数据
  } else {
    message.error('更新失败');
  }
};

</script>

<style scoped>

</style>
