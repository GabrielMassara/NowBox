<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import AppIcon from '../components/AppIcon.vue'
import logo from '../assets/logo.svg'
import { ApiError } from '../lib/http'
import { authStore } from '../stores/auth'

const router = useRouter()

const email = ref('')
const senha = ref('')
const carregando = ref(false)
const erro = ref('')

async function onSubmit() {
  if (carregando.value) return

  carregando.value = true
  erro.value = ''

  try {
    await authStore.login(email.value.trim(), senha.value)
    router.push('/selecionar-unidade')
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível entrar. Tente novamente.'
  } finally {
    carregando.value = false
  }
}
</script>

<template>
  <div class="login">
    <div class="login__card">
      <img :src="logo" alt="NowBox" class="login__logo" />

      <h1 class="login__title">Login</h1>
      <p class="login__subtitle">Preencha os dados para continuar.</p>

      <form class="login__form" @submit.prevent="onSubmit">
        <input
          v-model="email"
          type="email"
          autocomplete="username"
          placeholder="E-mail"
          aria-label="E-mail"
          required
          :disabled="carregando"
          class="login__input"
        />

        <input
          v-model="senha"
          type="password"
          autocomplete="current-password"
          placeholder="Senha"
          aria-label="Senha"
          required
          :disabled="carregando"
          class="login__input"
        />

        <p v-if="erro" class="login__error">
          <AppIcon name="alert-circle" :size="15" />
          {{ erro }}
        </p>

        <button type="submit" class="login__submit" :disabled="carregando">
          <AppIcon v-if="carregando" name="loader" :size="15" class="login__spinner" />
          {{ carregando ? 'Entrando...' : 'Entrar' }}
        </button>
      </form>
    </div>
  </div>
</template>

<style scoped>
.login {
  min-height: 100svh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: #f4f5f7;
}

.login__card {
  width: 100%;
  max-width: 610px;
  padding: 48px;
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 1px 2px rgba(11, 11, 11, 0.04), 0 8px 24px -14px rgba(11, 11, 11, 0.12);
}

.login__logo {
  height: 44px;
  width: auto;
  display: block;
  margin-bottom: 30px;
}

.login__title {
  font-size: 22px;
  font-weight: 600;
  line-height: 1.2;
  color: var(--text-primary);
}

.login__subtitle {
  margin-top: 6px;
  font-size: 13.5px;
  font-weight: 400;
  color: var(--text-muted);
}

.login__form {
  margin-top: 32px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.login__input {
  width: 100%;
  border: none;
  border-radius: 6px;
  background: var(--bg-surface-sunken);
  padding: 15px 14px;
  font: inherit;
  font-size: 14px;
  color: var(--text-primary);
  outline: none;
  transition: box-shadow 0.15s ease;
}

.login__input::placeholder {
  color: var(--text-muted);
}

.login__input:focus {
  box-shadow: 0 0 0 2px var(--brand-100);
}

.login__error {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 13px;
  color: var(--text-critical);
  background: color-mix(in srgb, var(--status-critical) 10%, transparent);
  border-radius: var(--radius-sm);
  padding: 9px 11px;
}

.login__submit {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  align-self: flex-start;
  margin-top: 8px;
  border: none;
  border-radius: 6px;
  background: var(--brand-500);
  color: var(--brand-contrast);
  padding: 16px 40px;
  font-size: 14px;
  font-weight: 600;
  text-transform: uppercase;
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.login__submit:hover:not(:disabled) {
  background: var(--brand-600);
}

.login__submit:disabled {
  opacity: 0.7;
  cursor: default;
}

.login__spinner {
  animation: login-spin 0.8s linear infinite;
}

@keyframes login-spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
