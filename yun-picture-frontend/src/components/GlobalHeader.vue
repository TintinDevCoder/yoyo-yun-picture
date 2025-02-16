<template>
  <a-row :wrap="false">
    <a-col flex="200px">
      <RouterLink to="/">
        <div class="title-bar">
          <img class="logo" src="../assets/yoyo.png" alt="logo" />
          <div class="title">悠悠云图库</div>
        </div>
      </RouterLink>
    </a-col>
    <a-col flex="auto">
      <a-menu
        v-model:selectedKeys="current"
        mode="horizontal"
        :items="items"
        @click="doMenuClick"
      />
    </a-col>
    <a-col flex="120px">
      <div class="user-login-status">
        <div v-if="loginUserStore.loginUser.id">
          <a-dropdown>
            <ASpace>
              <a-avatar :src="loginUserStore.loginUser.userAvatar" />
              {{ loginUserStore.loginUser.userName ?? '无名' }}
            </ASpace>
            <template #overlay>
              <a-menu>
                <a-menu-item @click="doEditProfile">
                  <EditOutlined />
                  编辑个人信息
                </a-menu-item>
                <a-menu-item>
                  <router-link to="/my_space">
                    <UserOutlined />
                    我的空间
                  </router-link>
                </a-menu-item>
                <a-menu-item @click="doLogout">
                  <LogoutOutlined />
                  退出登录
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
        <div v-else>
          <a-button type="primary" href="/user/login">登录</a-button>
        </div>
      </div>
    </a-col>
  </a-row>

  <!-- 编辑个人信息的模态框 -->
  <a-modal v-model:visible="isEditModalVisible" title="编辑个人信息" @ok="handleEdit">
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
    </a-form>
  </a-modal>
</template>

<script lang="ts" setup>
import { computed, h, reactive, ref } from 'vue'
import { HomeOutlined, LogoutOutlined, EditOutlined,UserOutlined } from '@ant-design/icons-vue';
import { MenuProps, message } from 'ant-design-vue';
import { useRouter } from 'vue-router';
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts';
import { userLogoutUsingPost } from '@/api/userController.ts';

const originItems = [
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: '主页',
    title: '主页',
  },
  {
    key: '/add_picture',
    label: '创建图片',
    title: '创建图片',
  },
  {
    key: '/admin/pictureManage',
    label: '图片管理',
    title: '图片管理',
  },
  {
    key: '/admin/userManage',
    label: '用户管理',
    title: '用户管理',
  },
  {
    key: '/admin/spaceManage',
    label: '空间管理',
    title: '空间管理',
  },
  {
    key: 'others',
    label: h('a', { href: 'https://github.com/TintinDevCoder', target: '_blank' }, '关于作者'),
    title: '关于作者',
  },
];

// 过滤菜单项
const filterMenus = (menus = [] as MenuProps['items']) => {
  return menus?.filter((menu) => {
    if (menu?.key?.startsWith('/admin')) {
      const loginUser = loginUserStore.loginUser;
      if (!loginUser || loginUser.userRole !== "admin") {
        return false;
      }
    }
    return true;
  });
};

// 展示在菜单的路由数组
const items = computed(() => filterMenus(originItems));
const router = useRouter();
const current = ref<string[]>([]);
const isEditModalVisible = ref(false);
const editUserData = reactive({
  userName: '',
  userAvatar: '',
  userProfile: '',
  userRole: '',
});

// 路由跳转事件
const doMenuClick = ({ key }: { key: string }) => {
  router.push({
    path: key,
  });
};

// 监听路由变化，更新当前选中菜单
router.afterEach((to) => {
  current.value = [to.path];
});

// 引入用户状态
const loginUserStore = useLoginUserStore();

// 用户注销
const doLogout = async () => {
  const res = await userLogoutUsingPost();
  console.log(res);
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({
      userName: '未登录',
    });
    message.success('退出登录成功');
    await router.push('/user/login');
  } else {
    message.error('退出登录失败，' + res.data.message);
  }
};

// 编辑个人信息
const doEditProfile = () => {
  const user = loginUserStore.loginUser;
  editUserData.userName = user.userName;
  editUserData.userAvatar = user.userAvatar;
  editUserData.userProfile = user.userProfile;
  editUserData.userRole = user.userRole;
  isEditModalVisible.value = true;
};

// 处理编辑提交
const handleEdit = async () => {
  // 处理编辑逻辑，例如调用 API 更新用户信息
  // const res = await updateUserUsingPost(editUserData);
  // 处理返回结果
  message.success('个人信息更新成功');
  isEditModalVisible.value = false;
};
</script>

<style scoped>
.title-bar {
  display: flex;
  align-items: center;
}

.title {
  color: black;
  font-size: 18px;
  margin-left: 16px;
}

.logo {
  height: 48px;
}
</style>
