<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppIcon from '../AppIcon.vue'
import logo from '../../assets/logo.svg'
import { menuStore } from '../../stores/menu'

defineProps<{ recolhida?: boolean }>()
defineEmits<{ alternar: [] }>()

interface NavItem {
  label: string
  icon: string
  to?: string
}

interface NavGroup {
  title?: string
  items: NavItem[]
}

const iconePadrao = 'dashboard'

const iconesPorRota: Record<string, string> = {
  '/modulos': 'layers',
  '/operacoes-sistema': 'settings',
  '/estados': 'map-pin',
  '/sessoes': 'folder',
  '/cargos': 'tag',
  '/permissoes': 'shield',
  '/atribuicoes': 'clipboard-check',
  '/usuarios': 'user-circle',
  '/unidades': 'building',
  '/boxes': 'box',
  '/alugueis': 'bag',
  '/clientes': 'users',
}

const route = useRoute()
const router = useRouter()

const rotasRegistradas = new Set(router.getRoutes().map((r) => r.path))

const groups = computed<NavGroup[]>(() => [
  {
    items: [{ label: 'Dashboard', icon: 'dashboard', to: '/' }],
  },
  ...menuStore.state.sessoes.map((sessao) => ({
    title: sessao.nome,
    items: sessao.modulos.map((modulo) => ({
      label: modulo.nome,
      icon: iconesPorRota[modulo.rota] ?? iconePadrao,
      to: rotasRegistradas.has(modulo.rota) ? modulo.rota : undefined,
    })),
  })),
])

const estaAtivo = (item: NavItem) => {
  if (item.to === undefined) return false
  if (item.to === '/') return route.path === '/'
  return route.path === item.to || route.path.startsWith(`${item.to}/`)
}

onMounted(() => menuStore.carregar())
</script>

<template>
  <aside class="sidebar" :class="{ 'sidebar--recolhida': recolhida }">
    <div class="sidebar__brand">
      <button
        type="button"
        class="sidebar__toggle"
        :aria-label="recolhida ? 'Expandir menu' : 'Recolher menu'"
        @click="$emit('alternar')"
      >
        <AppIcon name="menu" :size="20" />
      </button>
      <img :src="logo" alt="NowBox" class="sidebar__logo" />
    </div>

    <nav class="sidebar__nav scroll-thin">
      <div v-for="(group, i) in groups" :key="group.title ?? i" class="sidebar__group">
        <p v-if="group.title" class="sidebar__group-title">{{ group.title }}</p>

        <component
          :is="item.to ? 'router-link' : 'a'"
          v-for="item in group.items"
          :key="item.label"
          :to="item.to"
          :href="item.to ? undefined : '#'"
          class="sidebar__item"
          :class="{ 'sidebar__item--active': estaAtivo(item) }"
          :title="item.label"
          @click="!item.to && $event.preventDefault()"
        >
          <AppIcon class="sidebar__icon" :name="item.icon" :size="20" />
          <span class="sidebar__label">{{ item.label }}</span>
        </component>
      </div>

      <p v-if="menuStore.state.erro" class="sidebar__aviso">{{ menuStore.state.erro }}</p>
    </nav>
  </aside>
</template>

<style scoped>
.sidebar {
  display: flex;
  flex-direction: column;
  height: 100vh;
  position: sticky;
  top: 0;
  background: var(--bg-surface-sunken);
}

.sidebar__brand {
  position: relative;
  flex: none;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 72px;
  padding: 0 16px;
}

.sidebar__toggle {
  position: absolute;
  left: 16px;
  flex: none;
  width: 32px;
  height: 32px;
  display: grid;
  place-items: center;
  border: none;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--text-primary);
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.sidebar__toggle:hover {
  background: color-mix(in srgb, var(--bg-surface) 70%, transparent);
}

.sidebar__logo {
  height: 54px;
  display: block;
}

.sidebar__nav {
  --nav-inline: 16px;
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 4px var(--nav-inline) 20px;
}

.sidebar__group + .sidebar__group {
  margin-top: 22px;
}

.sidebar__group-title {
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.07em;
  text-transform: uppercase;
  color: var(--text-muted);
  padding: 0 12px;
  margin-bottom: 8px;
}

.sidebar__item {
  display: flex;
  align-items: center;
  gap: 14px;
  min-height: 44px;
  padding: 0 12px;
  border-radius: var(--radius-md);
  color: var(--text-secondary);
  text-decoration: none;
  font-size: 14.5px;
  font-weight: 500;
  margin-bottom: 4px;
  transition: background-color 0.15s ease, color 0.15s ease, box-shadow 0.15s ease;
}

.sidebar__icon {
  flex: none;
  color: var(--text-primary);
}

.sidebar__item:hover {
  background: color-mix(in srgb, var(--bg-surface) 70%, transparent);
  color: var(--text-primary);
}

.sidebar__item--active,
.sidebar__item--active:hover {
  background: var(--bg-surface);
  color: var(--brand-500);
  font-weight: 600;
  box-shadow: var(--shadow-card);
  margin-left: calc(var(--nav-inline) * -1);
  padding-left: calc(var(--nav-inline) + 12px);
  border-radius: 0 var(--radius-md) var(--radius-md) 0;
}

.sidebar__item--active .sidebar__icon {
  color: var(--brand-500);
}

.sidebar__aviso {
  margin-top: 22px;
  padding: 0 12px;
  font-size: 12.5px;
  color: var(--text-muted);
}

.sidebar__label {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sidebar--recolhida .sidebar__brand {
  padding: 0;
}

.sidebar--recolhida .sidebar__toggle {
  position: static;
}

.sidebar--recolhida .sidebar__logo,
.sidebar--recolhida .sidebar__label,
.sidebar--recolhida .sidebar__aviso {
  display: none;
}

.sidebar--recolhida .sidebar__nav {
  --nav-inline: 14px;
}

.sidebar--recolhida .sidebar__group-title {
  height: 1px;
  margin: 0 12px 10px;
  padding: 0;
  overflow: hidden;
  text-indent: -999px;
  background: var(--border-hairline);
}

.sidebar--recolhida .sidebar__item {
  justify-content: center;
  padding: 0;
}

.sidebar--recolhida .sidebar__item--active {
  padding-left: var(--nav-inline);
}

@media (max-width: 760px) {
  .sidebar {
    display: none;
  }
}
</style>
