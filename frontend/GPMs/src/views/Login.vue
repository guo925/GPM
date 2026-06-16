<template>
  <div class="login-container">
    <div class="login-panel">
      <section class="login-visual">
        <div class="visual-badge">GPMS</div>
        <h1>毕业设计全过程管理系统</h1>
        <div class="visual-grid">
          <div>
            <strong>课题</strong>
            <span>选题与审核</span>
          </div>
          <div>
            <strong>流程</strong>
            <span>节点与材料</span>
          </div>
          <div>
            <strong>成绩</strong>
            <span>评定与统计</span>
          </div>
        </div>
      </section>
      <section class="login-card">
        <div class="login-heading">
          <h2>账号登录</h2>
          <p>使用系统账号进入工作台</p>
        </div>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="0">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" size="large">
            <template #prefix><el-icon><User /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" size="large"
            autocomplete="current-password" @keyup.enter="handleLogin">
            <template #prefix><el-icon><Lock /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" style="width:100%" :loading="loading"
            @click="handleLogin">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore, getDashboardPath } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await authStore.login(form)
    router.push(getDashboardPath(authStore.roles))
  } catch {
    ElMessage.error('登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 32px;
  background:
    linear-gradient(135deg, rgba(37, 99, 235, .14), transparent 34%),
    linear-gradient(315deg, rgba(22, 163, 74, .12), transparent 30%),
    #f6f7fb;
}

.login-panel {
  display: grid;
  grid-template-columns: minmax(360px, 1fr) 420px;
  width: min(980px, 100%);
  min-height: 540px;
  overflow: hidden;
  background: #fff;
  border: 1px solid var(--gp-border);
  border-radius: 8px;
  box-shadow: 0 20px 50px rgba(15, 23, 42, .12);
}

.login-visual {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 44px;
  color: #fff;
  background:
    linear-gradient(140deg, rgba(23, 32, 51, .96), rgba(37, 99, 235, .78)),
    url('https://images.unsplash.com/photo-1497366754035-f200968a6e72?auto=format&fit=crop&w=1200&q=80') center/cover;
}

.visual-badge {
  width: max-content;
  padding: 7px 10px;
  background: rgba(255, 255, 255, .14);
  border: 1px solid rgba(255, 255, 255, .22);
  border-radius: 6px;
  font-weight: 750;
}

.login-visual h1 {
  max-width: 420px;
  margin: 0;
  font-size: 34px;
  line-height: 1.25;
  font-weight: 760;
}

.visual-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.visual-grid div {
  padding: 12px;
  background: rgba(255, 255, 255, .12);
  border: 1px solid rgba(255, 255, 255, .18);
  border-radius: 8px;
}

.visual-grid strong,
.visual-grid span {
  display: block;
}

.visual-grid span {
  margin-top: 5px;
  color: rgba(255, 255, 255, .76);
  font-size: 12px;
}

.login-card {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 46px;
}

.login-heading {
  margin-bottom: 28px;
}

.login-heading h2 {
  margin: 0;
  color: var(--gp-text);
  font-size: 24px;
  font-weight: 700;
}

.login-heading p {
  margin: 8px 0 0;
  color: var(--gp-text-muted);
  font-size: 13px;
}

@media (max-width: 860px) {
  .login-panel {
    grid-template-columns: 1fr;
  }

  .login-visual {
    min-height: 260px;
  }
}
</style>
