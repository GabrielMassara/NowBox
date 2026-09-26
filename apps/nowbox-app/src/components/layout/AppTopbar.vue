<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppIcon from '../AppIcon.vue'
import { authStore } from '../../stores/auth'
import { unidadeStore } from '../../stores/unidade'

defineProps<{ title: string; subtitle?: string }>()
defineEmits<{ 'abrir-menu': [] }>()

const router = useRouter()
const menuAberto = ref(false)
const menuRef = ref<HTMLElement | null>(null)

const iniciais = () => {
  const partes = authStore.nomeExibicao.value.trim().split(/\s+/)
  return partes
    .slice(0, 2)
    .map((p) => p[0]?.toUpperCase())
    .join('')
}

function aoClicarFora(evento: MouseEvent) {
  if (menuRef.value && !menuRef.value.contains(evento.target as Node)) {
    menuAberto.value = false
  }
}

onMounted(() => document.addEventListener('click', aoClicarFora))
onBeforeUnmount(() => document.removeEventListener('click', aoClicarFora))

function trocarUnidade() {
  menuAberto.value = false
  router.push('/selecionar-unidade')
}

function sair() {
  menuAberto.value = false
  authStore.logout()
  router.push('/login')
}
</script>

<template>
  <header class="topbar">
    <button
      type="button"
      class="topbar__menu-btn"
      aria-label="Abrir menu"
      @click="$emit('abrir-menu')"
    >
      <AppIcon name="menu" :size="20" />
    </button>

    <div class="topbar__title">
      <h1>{{ title }}</h1>
      <p v-if="subtitle">{{ subtitle }}</p>
    </div>

    <div class="topbar__actions">
      <button class="topbar__icon-btn" type="button" aria-label="Notificações">
        <AppIcon name="bell" :size="18" />
        <span class="topbar__dot"></span>
      </button>

      <div class="topbar__divider"></div>

      <div class="topbar__user-menu" ref="menuRef">
        <button class="topbar__user" type="button" @click="menuAberto = !menuAberto">
          <span class="topbar__avatar">{{ iniciais() }}</span>
          <span class="topbar__user-info">
            <strong>{{ authStore.nomeExibicao.value }}</strong>
            <span>{{ unidadeStore.state.selecionada?.nome ?? '—' }}</span>
          </span>
          <AppIcon name="chevron-down" :size="14" />
        </button>

        <div v-if="menuAberto" class="topbar__dropdown">
          <button type="button" class="topbar__dropdown-item" @click="trocarUnidade">
            <AppIcon name="building" :size="16" />
            Trocar unidade
          </button>
          <button type="button" class="topbar__dropdown-item" @click="sair">
            <AppIcon name="logout" :size="16" />
            Sair
          </button>
        </div>
      </div>
    </div>
  </header>
</template>

<style scoped>
.topbar {
  position: sticky;
  top: 0;
  z-index: 5;
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 16px 28px;
  background: color-mix(in srgb, var(--bg-page) 88%, transparent);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid var(--border-hairline);
}

.topbar__menu-btn {
  display: none;
  flex: none;
  width: 36px;
  height: 36px;
  place-items: center;
  border: 1px solid var(--border-hairline);
  border-radius: 999px;
  background: var(--bg-surface);
  color: var(--text-primary);
  cursor: pointer;
}

.topbar__title {
  min-width: 0;
}

.topbar__title h1 {
  font-size: 19px;
  font-weight: 700;
}

.topbar__title p {
  font-size: 12.5px;
  color: var(--text-muted);
  margin-top: 2px;
}

.topbar__search {
  flex: 1;
  max-width: 380px;
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--bg-surface);
  border: 1px solid var(--border-hairline);
  border-radius: var(--radius-sm);
  padding: 8px 12px;
  color: var(--text-muted);
}

.topbar__search input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 13.5px;
  color: var(--text-primary);
}

.topbar__search input::placeholder {
  color: var(--text-muted);
}

.topbar__search kbd {
  font: 11px system-ui;
  color: var(--text-muted);
  background: var(--bg-surface-sunken);
  border-radius: 5px;
  padding: 2px 6px;
}

.topbar__actions {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 14px;
}

.topbar__icon-btn {
  position: relative;
  width: 36px;
  height: 36px;
  display: grid;
  place-items: center;
  border-radius: 999px;
  border: 1px solid var(--border-hairline);
  background: var(--bg-surface);
  color: var(--text-secondary);
  cursor: pointer;
}

.topbar__icon-btn:hover {
  background: var(--bg-surface-sunken);
}

.topbar__dot {
  position: absolute;
  top: 8px;
  right: 9px;
  width: 6px;
  height: 6px;
  border-radius: 999px;
  background: var(--status-critical);
  border: 1.5px solid var(--bg-surface);
}

.topbar__divider {
  width: 1px;
  height: 24px;
  background: var(--border-hairline);
}

.topbar__user-menu {
  position: relative;
}

.topbar__user {
  display: flex;
  align-items: center;
  gap: 9px;
  background: transparent;
  border: none;
  cursor: pointer;
  color: var(--text-primary);
}

.topbar__dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  min-width: 180px;
  background: var(--bg-surface);
  border: 1px solid var(--border-hairline);
  border-radius: var(--radius-sm);
  box-shadow: var(--shadow-card);
  padding: 6px;
  display: flex;
  flex-direction: column;
  gap: 2px;
  z-index: 10;
}

.topbar__dropdown-item {
  display: flex;
  align-items: center;
  gap: 9px;
  border: none;
  background: transparent;
  color: var(--text-primary);
  font-size: 13px;
  font-weight: 500;
  text-align: left;
  padding: 8px 9px;
  border-radius: 6px;
  cursor: pointer;
}

.topbar__dropdown-item:hover {
  background: var(--bg-surface-sunken);
}

.topbar__avatar {
  width: 34px;
  height: 34px;
  border-radius: 999px;
  background: var(--brand-500);
  color: var(--brand-contrast);
  display: grid;
  place-items: center;
  font-size: 12.5px;
  font-weight: 700;
  flex: none;
}

.topbar__user-info {
  display: flex;
  flex-direction: column;
  text-align: left;
  line-height: 1.25;
}

.topbar__user-info strong {
  font-size: 13px;
}

.topbar__user-info span {
  font-size: 11.5px;
  color: var(--text-muted);
}

@media (max-width: 900px) {
  .topbar__search,
  .topbar__user-info {
    display: none;
  }
}

@media (max-width: 760px) {
  .topbar {
    gap: 12px;
    padding: 12px 16px;
  }

  .topbar__menu-btn {
    display: grid;
  }

  .topbar__title h1 {
    font-size: 17px;
  }

  .topbar__actions {
    flex: none;
    gap: 10px;
  }
}
</style>
