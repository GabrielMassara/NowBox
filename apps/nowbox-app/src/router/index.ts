import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import SelectUnidadeView from '../views/SelectUnidadeView.vue'
import DashboardView from '../views/DashboardView.vue'
import SessaoListView from '../views/sessoes/SessaoListView.vue'
import SessaoManterView from '../views/sessoes/SessaoManterView.vue'
import ModuloListView from '../views/modulos/ModuloListView.vue'
import ModuloManterView from '../views/modulos/ModuloManterView.vue'
import OperacaoListView from '../views/operacoes/OperacaoListView.vue'
import OperacaoManterView from '../views/operacoes/OperacaoManterView.vue'
import AtribuicaoListView from '../views/atribuicoes/AtribuicaoListView.vue'
import AtribuicaoManterView from '../views/atribuicoes/AtribuicaoManterView.vue'
import UsuarioListView from '../views/usuarios/UsuarioListView.vue'
import UsuarioManterView from '../views/usuarios/UsuarioManterView.vue'
import UnidadeListView from '../views/unidades/UnidadeListView.vue'
import UnidadeManterView from '../views/unidades/UnidadeManterView.vue'
import CargoListView from '../views/cargos/CargoListView.vue'
import CargoManterView from '../views/cargos/CargoManterView.vue'
import PermissaoListView from '../views/permissoes/PermissaoListView.vue'
import PermissaoManterView from '../views/permissoes/PermissaoManterView.vue'
import ClienteListView from '../views/clientes/ClienteListView.vue'
import ClienteManterView from '../views/clientes/ClienteManterView.vue'
import BoxListView from '../views/boxes/BoxListView.vue'
import BoxManterView from '../views/boxes/BoxManterView.vue'
import BoxLoteView from '../views/boxes/BoxLoteView.vue'
import AluguelListView from '../views/alugueis/AluguelListView.vue'
import AluguelManterView from '../views/alugueis/AluguelManterView.vue'
import AluguelAssinaturaView from '../views/alugueis/AluguelAssinaturaView.vue'
import EstadoListView from '../views/estados/EstadoListView.vue'
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
      component: LoginView,
      meta: { public: true },
    },
    {
      path: '/selecionar-unidade',
      name: 'selecionar-unidade',
      component: SelectUnidadeView,
    },
    {
      path: '/',
      component: AppShellLayout,
      meta: { requiresUnidade: true },
      children: [
        {
          path: '',
          name: 'dashboard',
          component: DashboardView,
          meta: { title: 'Dashboard', subtitle: 'Visão geral das operações NowBox' },
        },
        {
          path: 'sessoes',
          name: 'sessoes',
          component: SessaoListView,
          meta: { title: 'Sessões', subtitle: 'Gerencie as sessões do menu do sistema' },
        },
        {
          path: 'sessoes/nova',
          name: 'sessao-nova',
          component: SessaoManterView,
          meta: { title: 'Nova sessão', subtitle: 'Cadastre uma nova sessão de menu' },
        },
        {
          path: 'sessoes/:id/editar',
          name: 'sessao-editar',
          component: SessaoManterView,
          meta: { title: 'Editar sessão', subtitle: 'Atualize os dados da sessão' },
        },
        {
          path: 'modulos',
          name: 'modulos',
          component: ModuloListView,
          meta: { title: 'Módulos', subtitle: 'Gerencie os módulos do menu do sistema' },
        },
        {
          path: 'modulos/novo',
          name: 'modulo-novo',
          component: ModuloManterView,
          meta: { title: 'Novo módulo', subtitle: 'Cadastre um novo módulo de menu' },
        },
        {
          path: 'modulos/:id/editar',
          name: 'modulo-editar',
          component: ModuloManterView,
          meta: { title: 'Editar módulo', subtitle: 'Atualize os dados do módulo' },
        },
        {
          path: 'operacoes-sistema',
          name: 'operacoes',
          component: OperacaoListView,
          meta: { title: 'Operações', subtitle: 'Gerencie as operações do sistema' },
        },
        {
          path: 'operacoes-sistema/nova',
          name: 'operacao-nova',
          component: OperacaoManterView,
          meta: { title: 'Nova operação', subtitle: 'Cadastre uma nova operação do sistema' },
        },
        {
          path: 'operacoes-sistema/:id/editar',
          name: 'operacao-editar',
          component: OperacaoManterView,
          meta: { title: 'Editar operação', subtitle: 'Atualize os dados da operação' },
        },
        {
          path: 'atribuicoes',
          name: 'atribuicoes',
          component: AtribuicaoListView,
          meta: { title: 'Atribuições', subtitle: 'Gerencie os cargos atribuídos aos usuários' },
        },
        {
          path: 'atribuicoes/nova',
          name: 'atribuicao-nova',
          component: AtribuicaoManterView,
          meta: { title: 'Nova atribuição', subtitle: 'Atribua um cargo a um usuário' },
        },
        {
          path: 'atribuicoes/:id/editar',
          name: 'atribuicao-editar',
          component: AtribuicaoManterView,
          meta: { title: 'Editar atribuição', subtitle: 'Atualize os dados da atribuição' },
        },
        {
          path: 'usuarios',
          name: 'usuarios',
          component: UsuarioListView,
          meta: { title: 'Usuários', subtitle: 'Gerencie os usuários do sistema' },
        },
        {
          path: 'usuarios/novo',
          name: 'usuario-novo',
          component: UsuarioManterView,
          meta: { title: 'Novo usuário', subtitle: 'Cadastre um novo usuário' },
        },
        {
          path: 'usuarios/:id/editar',
          name: 'usuario-editar',
          component: UsuarioManterView,
          meta: { title: 'Editar usuário', subtitle: 'Atualize os dados do usuário' },
        },
        {
          path: 'unidades',
          name: 'unidades',
          component: UnidadeListView,
          meta: { title: 'Unidades', subtitle: 'Gerencie as unidades do sistema' },
        },
        {
          path: 'unidades/novo',
          name: 'unidade-novo',
          component: UnidadeManterView,
          meta: { title: 'Nova unidade', subtitle: 'Cadastre uma nova unidade' },
        },
        {
          path: 'unidades/:id/editar',
          name: 'unidade-editar',
          component: UnidadeManterView,
          meta: { title: 'Editar unidade', subtitle: 'Atualize os dados da unidade' },
        },
        {
          path: 'cargos',
          name: 'cargos',
          component: CargoListView,
          meta: { title: 'Cargos', subtitle: 'Gerencie os cargos das unidades' },
        },
        {
          path: 'cargos/novo',
          name: 'cargo-novo',
          component: CargoManterView,
          meta: { title: 'Novo cargo', subtitle: 'Cadastre um novo cargo em uma unidade' },
        },
        {
          path: 'cargos/:id/editar',
          name: 'cargo-editar',
          component: CargoManterView,
          meta: { title: 'Editar cargo', subtitle: 'Atualize os dados do cargo' },
        },
        {
          path: 'permissoes',
          name: 'permissoes',
          component: PermissaoListView,
          meta: { title: 'Permissões', subtitle: 'Consulte as operações liberadas para cada cargo' },
        },
        {
          path: 'permissoes/nova',
          name: 'permissao-nova',
          component: PermissaoManterView,
          meta: { title: 'Gerenciar permissões', subtitle: 'Libere operações para um cargo' },
        },
        {
          path: 'permissoes/cargo/:idCargo/editar',
          name: 'permissao-editar',
          component: PermissaoManterView,
          meta: { title: 'Editar permissões', subtitle: 'Atualize as operações liberadas para o cargo' },
        },
        {
          path: 'clientes',
          name: 'clientes',
          component: ClienteListView,
          meta: { title: 'Clientes', subtitle: 'Gerencie os clientes cadastrados' },
        },
        {
          path: 'clientes/novo',
          name: 'cliente-novo',
          component: ClienteManterView,
          meta: { title: 'Novo cliente', subtitle: 'Cadastre um novo cliente' },
        },
        {
          path: 'clientes/:id/editar',
          name: 'cliente-editar',
          component: ClienteManterView,
          meta: { title: 'Editar cliente', subtitle: 'Atualize os dados do cliente' },
        },
        {
          path: 'boxes',
          name: 'boxes',
          component: BoxListView,
          meta: { title: 'Boxes', subtitle: 'Gerencie os boxes da unidade selecionada' },
        },
        {
          path: 'boxes/novo',
          name: 'box-novo',
          component: BoxManterView,
          meta: { title: 'Novo box', subtitle: 'Cadastre um novo box na unidade' },
        },
        {
          path: 'boxes/lote',
          name: 'box-lote',
          component: BoxLoteView,
          meta: { title: 'Cadastro em lote', subtitle: 'Cadastre vários boxes de uma vez informando um intervalo' },
        },
        {
          path: 'boxes/:id/editar',
          name: 'box-editar',
          component: BoxManterView,
          meta: { title: 'Editar box', subtitle: 'Atualize os dados do box' },
        },
        {
          path: 'alugueis',
          name: 'alugueis',
          component: AluguelListView,
          meta: { title: 'Aluguéis', subtitle: 'Gerencie os aluguéis dos boxes da unidade selecionada' },
        },
        {
          path: 'alugueis/novo',
          name: 'aluguel-novo',
          component: AluguelManterView,
          meta: { title: 'Novo aluguel', subtitle: 'Registre o aluguel de um box para um cliente' },
        },
        {
          path: 'alugueis/:id/editar',
          name: 'aluguel-editar',
          component: AluguelManterView,
          meta: { title: 'Editar aluguel', subtitle: 'Atualize os dados do aluguel' },
        },
        {
          path: 'alugueis/:id/assinatura-contrato',
          name: 'aluguel-assinatura-contrato',
          component: AluguelAssinaturaView,
          props: { tipo: 'contrato' },
          meta: { title: 'Assinatura do contrato', subtitle: 'Baixe o contrato, colha as assinaturas e envie o arquivo assinado' },
        },
        {
          path: 'alugueis/:id/assinatura-aditivo',
          name: 'aluguel-assinatura-aditivo',
          component: AluguelAssinaturaView,
          props: { tipo: 'aditivo' },
          meta: { title: 'Assinatura do aditivo', subtitle: 'Baixe o aditivo, colha as assinaturas e envie o arquivo assinado' },
        },
        {
          path: 'alugueis/:id/assinatura-distrato',
          name: 'aluguel-assinatura-distrato',
          component: AluguelAssinaturaView,
          props: { tipo: 'distrato' },
          meta: { title: 'Assinatura do distrato', subtitle: 'Baixe o distrato, colha as assinaturas e envie o arquivo assinado' },
        },
        {
          path: 'estados',
          name: 'estados',
          component: EstadoListView,
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
