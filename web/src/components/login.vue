<template>
    <div class="login-container">
        <div class="login-card">
            <h2 class="login-title">用户登录</h2>
            
            <div class="form-group">
                <label for="username">用户名</label>
                <input 
                    id="username"
                    type="text" 
                    v-model.trim="username" 
                    placeholder="请输入用户名"
                    @keyup.enter="handleLogin"
                    :class="{ 'input-error': errors.username }"
                >
                <span class="error-message" v-if="errors.username">{{ errors.username }}</span>
            </div>

            <div class="form-group">
                <label for="password">密码</label>
                <input 
                    id="password"
                    type="password" 
                    v-model.trim="password" 
                    placeholder="请输入密码"
                    @keyup.enter="handleLogin"
                    :class="{ 'input-error': errors.password }"
                >
                <span class="error-message" v-if="errors.password">{{ errors.password }}</span>
            </div>

            <div class="form-actions">
                <button 
                    class="login-btn" 
                    @click="handleLogin"
                    :disabled="isSubmitting"
                >
                    {{ isSubmitting ? '登录中...' : '登录' }}
                </button>
                <router-link to="/register" class="register-link">没有账号？立即注册</router-link>
            </div>

            <div class="server-error" v-if="serverError">{{ serverError }}</div>
        </div>
    </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const username = ref('')
const password = ref('')
const isSubmitting = ref(false)
const serverError = ref('')

const errors = reactive({
    username: '',
    password: ''
})

const validateForm = () => {
    let isValid = true
    
    // 重置错误信息
    errors.username = ''
    errors.password = ''
    
    // 验证用户名
    if (!username.value) {
        errors.username = '请输入用户名'
        isValid = false
    } else if (username.value.length < 4) {
        errors.username = '用户名长度不能少于4个字符'
        isValid = false
    }
    
    // 验证密码
    if (!password.value) {
        errors.password = '请输入密码'
        isValid = false
    } else if (password.value.length < 6) {
        errors.password = '密码长度不能少于6个字符'
        isValid = false
    }
    
    return isValid
}

const handleLogin = async () => {
    if (!validateForm()) return
    
    isSubmitting.value = true
    serverError.value = ''
    
    try {
        const response = await fetch('https://www.sunyuanling1.com/api/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                identifier: username.value,
                password: password.value
            })
        })
        
        const data = await response.json()
        
        if (response.ok) {
            console.log(data)
            localStorage.setItem('token', data.token)
            // 登录成功处理
            // 这里可以存储token等操作
            router.push('/')
        } else {
            console.log(data)
            // 服务器返回的错误信息
            serverError.value = data.message || '登录失败，请重试'
            localStorage.removeItem('token')
        }
    } catch (err) {
        console.error('登录请求失败:', err)
        console.log(data)
        serverError.value = '网络错误，请检查连接后重试'
        localStorage.removeItem('token')
    } finally {
        isSubmitting.value = false
    }
}
</script>

<style lang="scss" scoped>
.login-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
    min-width: 100vw;
    background-color: #f5f5f5;
    padding: 20px;
}

.login-card {
    background: white;
    padding: 2rem;
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    width: 100%;
    max-width: 400px;
}

.login-title {
    text-align: center;
    color: #333;
    margin-bottom: 1.5rem;
}

.form-group {
    margin-bottom: 1.5rem;
    
    label {
        display: block;
        margin-bottom: 0.5rem;
        color: #555;
        font-weight: 500;
    }
    
    input {
        width: calc(100% - 1.5rem);
        padding: 0.75rem;
        border: 1px solid #ddd;
        border-radius: 4px;
        font-size: 1rem;
        transition: border-color 0.3s;
        
        &:focus {
            border-color: #409eff;
            outline: none;
        }
        
        &.input-error {
            border-color: #f56c6c;
        }
    }
    
    .error-message {
        display: block;
        margin-top: 0.5rem;
        color: #f56c6c;
        font-size: 0.875rem;
    }
}

.form-actions {
    display: flex;
    flex-direction: column;
    gap: 1rem;
}

.login-btn {
    width: 100%;
    padding: 0.75rem;
    background-color: #409eff;
    color: white;
    border: none;
    border-radius: 4px;
    font-size: 1rem;
    cursor: pointer;
    transition: background-color 0.3s;
    
    &:hover {
        background-color: #66b1ff;
    }
    
    &:disabled {
        background-color: #a0cfff;
        cursor: not-allowed;
    }
}

.register-link {
    text-align: center;
    color: #409eff;
    text-decoration: none;
    font-size: 0.875rem;
    
    &:hover {
        text-decoration: underline;
    }
}

.server-error {
    margin-top: 1rem;
    padding: 0.75rem;
    background-color: #fef0f0;
    color: #f56c6c;
    border-radius: 4px;
    text-align: center;
    font-size: 0.875rem;
}
</style>