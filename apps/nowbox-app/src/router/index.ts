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
        {
          path: 'modulos',
          name: 'modulos',
          component: () => import('../views/modulos/ModuloListView.vue'),
          meta: { title: 'Módulos', subtitle: 'Gerencie os módulos do menu do sistema' },
        },
        {
          path: 'modulos/novo',
          name: 'modulo-novo',
          component: () => import('../views/modulos/ModuloManterView.vue'),
          meta: { title: 'Novo módulo', subtitle: 'Cadastre um novo módulo de menu' },
        },
        {
          path: 'modulos/:id/editar',
          name: 'modulo-editar',
          component: () => import('../views/modulos/ModuloManterView.vue'),
          meta: { title: 'Editar módulo', subtitle: 'Atualize os dados do módulo' },
        },
        {
          path: 'operacoes-sistema',
          name: 'operacoes',
          component: () => import('../views/operacoes/OperacaoListView.vue'),
          meta: { title: 'Operações', subtitle: 'Gerencie as operações do sistema' },
        },
        {
          path: 'operacoes-sistema/nova',
          name: 'operacao-nova',
          component: () => import('../views/operacoes/OperacaoManterView.vue'),
          meta: { title: 'Nova operação', subtitle: 'Cadastre uma nova operação do sistema' },
        },
        {
          path: 'operacoes-sistema/:id/editar',
          name: 'operacao-editar',
          component: () => import('../views/operacoes/OperacaoManterView.vue'),
          meta: { title: 'Editar operação', subtitle: 'Atualize os dados da operação' },
        },
        {
          path: 'cargos',
          name: 'cargos',
          component: () => import('../views/cargos/CargoListView.vue'),
          meta: { title: 'Cargos', subtitle: 'Gerencie os cargos das unidades' },
        },
        {
          path: 'cargos/novo',
          name: 'cargo-novo',
          component: () => import('../views/cargos/CargoManterView.vue'),
          meta: { title: 'Novo cargo', subtitle: 'Cadastre um novo cargo em uma unidade' },
        },
        {
          path: 'cargos/:id/editar',
          name: 'cargo-editar',
          component: () => import('../views/cargos/CargoManterView.vue'),
          meta: { title: 'Editar cargo', subtitle: 'Atualize os dados do cargo' },
        },
        {
          path: 'permissoes',
          name: 'permissoes',
          component: () => import('../views/permissoes/PermissaoListView.vue'),
          meta: { title: 'Permissões', subtitle: 'Consulte as operações liberadas para cada cargo' },
        },
        {
          path: 'permissoes/nova',
          name: 'permissao-nova',
          component: () => import('../views/permissoes/PermissaoManterView.vue'),
          meta: { title: 'Gerenciar permissões', subtitle: 'Libere operações para um cargo' },
        },
        {
          path: 'permissoes/cargo/:idCargo/editar',
          name: 'permissao-editar',
          component: () => import('../views/permissoes/PermissaoManterView.vue'),
          meta: { title: 'Editar permissões', subtitle: 'Atualize as operações liberadas para o cargo' },
        },
        {
          path: 'estados',
          name: 'estados',
          component: () => import('../views/estados/EstadoListView.vue'),
          meta: { title: 'Estados', subtitle: 'Consulte os estados cadastrados no sistema' },
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
