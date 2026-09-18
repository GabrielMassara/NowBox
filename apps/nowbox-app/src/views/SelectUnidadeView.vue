<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppIcon from '../components/AppIcon.vue'
import logo from '../assets/logo.svg'
import storageImage from '../assets/storage.png'
import { authStore } from '../stores/auth'
import type { MinhaUnidade } from '../services/unidade.service'
import { unidadeStore } from '../stores/unidade'

const router = useRouter()

onMounted(() => {
  unidadeStore.carregarMinhasUnidades()
})

function escolher(item: MinhaUnidade) {
  unidadeStore.selecionarUnidade(item.unidade, item.cargo)
  router.push('/')
}

function sair() {
  authStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="select-unidade">
    <aside class="select-unidade__aside">
      <img :src="storageImage" alt="" class="select-unidade__aside-image" />
      <img :src="logo" alt="NowBox" class="select-unidade__aside-logo" />
    </aside>

    <div class="select-unidade__content">
      <div class="select-unidade__topbar">
        <button type="button" class="btn select-unidade__logout" @click="sair">
          <AppIcon name="logout" :size="15" />
          Sair
        </button>
      </div>

      <div class="select-unidade__card card">
        <h1>Selecione uma unidade</h1>
        <p class="select-unidade__subtitle">Olá, {{ authStore.nomeExibicao.value }}. Escolha a unidade para continuar.</p>

        <div v-if="unidadeStore.state.carregando" class="select-unidade__state">
          <AppIcon name="loader" :size="20" class="select-unidade__spinner" />
          <span>Carregando unidades...</span>
        </div>

        <div v-else-if="unidadeStore.state.erro" class="select-unidade__state select-unidade__state--error">
          <AppIcon name="alert-circle" :size="20" />
          <span>{{ unidadeStore.state.erro }}</span>
          <button type="button" class="btn" @click="unidadeStore.carregarMinhasUnidades()">Tentar novamente</button>
        </div>

        <div v-else-if="unidadeStore.state.minhasUnidades.length === 0" class="select-unidade__state">
          <AppIcon name="building" :size="20" />
          <span>Nenhuma unidade está vinculada ao seu usuário.</span>
        </div>

        <ul v-else class="select-unidade__list">
          <li v-for="item in unidadeStore.state.minhasUnidades" :key="item.unidade.id">
            <button type="button" class="unit-option" @click="escolher(item)">
              <span class="unit-option__info">
                <strong>{{ item.unidade.nome }}</strong>
                <span>{{ item.unidade.cidade }} · {{ item.unidade.estado.uf }} · {{ item.cargo.nome }}</span>
              </span>
              <AppIcon name="chevron-right" :size="18" />
            </button>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<style scoped>
.select-unidade {
  min-height: 100svh;
  display: flex;
}

.select-unidade__aside {
  flex: 0 0 38%;
  min-height: 100svh;
  position: relative;
  overflow: hidden;
  display: grid;
  place-items: center;
}

.select-unidade__aside-image {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  filter: blur(4px);
  transform: scale(1.05);
}

.select-unidade__aside-logo {
  position: relative;
  z-index: 1;
  height: 72px;
}

.select-unidade__content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  padding: 28px clamp(24px, 5vw, 64px);
  background: linear-gradient(180deg, var(--brand-050), var(--bg-page) 55%);
}

.select-unidade__topbar {
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.select-unidade__logout {
  font-size: 12.5px;
  padding: 6px 11px;
}

.select-unidade__card {
  width: 100%;
  max-width: 460px;
  padding: 32px;
  margin: auto;
}

.select-unidade__card h1 {
  font-size: 20px;
  font-weight: 700;
}

@media (max-width: 860px) {
  .select-unidade__aside {
    display: none;
  }
}

.select-unidade__subtitle {
  margin-top: 6px;
  font-size: 13.5px;
  color: var(--text-muted);
}

.select-unidade__state {
  margin-top: 28px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 24px 10px;
  text-align: center;
  color: var(--text-muted);
  font-size: 13.5px;
}

.select-unidade__state--error {
  color: var(--text-critical);
}

.select-unidade__spinner {
  animation: select-unidade-spin 0.8s linear infinite;
  color: var(--brand-500);
}

@keyframes select-unidade-spin {
  to {
    transform: rotate(360deg);
  }
}

.select-unidade__list {
  list-style: none;
  margin: 24px 0 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.unit-option {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 13px;
  background: var(--bg-surface);
  border: 1px solid var(--border-hairline);
  border-radius: var(--radius-md);
  padding: 13px 14px;
  cursor: pointer;
  text-align: left;
  color: var(--text-primary);
  transition: border-color 0.15s ease, background-color 0.15s ease;
}

.unit-option:hover {
  border-color: var(--brand-500);
  background: var(--brand-050);
}

.unit-option__info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.unit-option__info strong {
  font-size: 14px;
}

.unit-option__info span {
  font-size: 12.5px;
  color: var(--text-muted);
}
</style>
