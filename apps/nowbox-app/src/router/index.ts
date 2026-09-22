import { createRouter, createWebHistory } from 'vue-router'
import AppShellLayout from '../layouts/AppShellLayout.vue'
import { authStore } from '../stores/auth'
import { unidadeStore } from '../stores/unidade'

declare module 'vue-router' {
  interface RouteMeta {
    public?: boolean
    requiresUnidade?: boolean
    title?: string
    subtitle?: string
  }
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
      meta: { public: true },
    },
    {
      path: '/selecionar-unidade',
      name: 'selecionar-unidade',
      component: () => import('../views/SelectUnidadeView.vue'),
    },
    {
      path: '/',
      component: AppShellLayout,
      meta: { requiresUnidade: true },
      children: [
        {
          path: '',
          name: 'dashboard',
          component: () => import('../views/DashboardView.vue'),
          meta: { title: 'Dashboard', subtitle: 'Visão geral das operações NowBox' },
        },
        {
          path: 'sessoes',
          name: 'sessoes',
          component: () => import('../views/sessoes/SessaoListView.vue'),
          meta: { title: 'Sessões', subtitle: 'Gerencie as sessões do menu do sistema' },
        },
        {
          path: 'sessoes/nova',
          name: 'sessao-nova',
          component: () => import('../views/sessoes/SessaoManterView.vue'),
          meta: { title: 'Nova sessão', subtitle: 'Cadastre uma nova sessão de menu' },
        },
        {
          path: 'sessoes/:id/editar',
          name: 'sessao-editar',
          component: () => import('../views/sessoes/SessaoManterView.vue'),
          meta: { title: 'Editar sessão', subtitle: 'Atualize os dados da sessão' },
        },
      ],
    },
  ],
})

router.beforeEach((to) => {
  const autenticado = authStore.isAuthenticated.value
  const temUnidade = unidadeStore.temUnidadeSelecionada.value

  if (to.meta.public) {
    if (!autenticado) return true
    return temUnidade ? { path: '/' } : { path: '/selecionar-unidade' }
  }

  if (!autenticado) return { path: '/login' }
  if (to.meta.requiresUnidade && !temUnidade) return { path: '/selecionar-unidade' }

  return true
})

export default router
