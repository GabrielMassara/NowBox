import { computed, reactive } from 'vue'
import { unidadeService, type MinhaUnidade } from '../services/unidade.service'
import type { CargoEntity, UnidadeEntity } from '../types/api'
import { authStore } from './auth'

const UNIDADE_STORAGE_KEY = 'nowbox.unidadeSelecionada'
const CARGO_STORAGE_KEY = 'nowbox.cargoSelecionado'

const state = reactive({
  minhasUnidades: [] as MinhaUnidade[],
  carregando: false,
  erro: null as string | null,
  selecionada: lerSalvo<UnidadeEntity>(UNIDADE_STORAGE_KEY),
  cargoSelecionado: lerSalvo<CargoEntity>(CARGO_STORAGE_KEY),
})

function lerSalvo<T>(chave: string): T | null {
  const bruto = localStorage.getItem(chave)
  if (!bruto) return null
  try {
    return JSON.parse(bruto)
  } catch {
    return null
  }
}

const temUnidadeSelecionada = computed(() => !!state.selecionada)

async function carregarMinhasUnidades() {
  const idUsuario = authStore.usuarioId.value
  if (!idUsuario) return

  state.carregando = true
  state.erro = null

  try {
    state.minhasUnidades = await unidadeService.listarMinhasUnidades(idUsuario)
  } catch {
    state.erro = 'Não foi possível carregar as unidades vinculadas ao seu usuário.'
  } finally {
    state.carregando = false
  }
}

function selecionarUnidade(unidade: UnidadeEntity, cargo: CargoEntity) {
  state.selecionada = unidade
  state.cargoSelecionado = cargo
  localStorage.setItem(UNIDADE_STORAGE_KEY, JSON.stringify(unidade))
  localStorage.setItem(CARGO_STORAGE_KEY, JSON.stringify(cargo))
}

function limpar() {
  state.selecionada = null
  state.cargoSelecionado = null
  state.minhasUnidades = []
  state.erro = null
  localStorage.removeItem(UNIDADE_STORAGE_KEY)
  localStorage.removeItem(CARGO_STORAGE_KEY)
}

window.addEventListener('nowbox:unauthorized', limpar)
window.addEventListener('nowbox:logout', limpar)

export const unidadeStore = {
  state,
  temUnidadeSelecionada,
  carregarMinhasUnidades,
  selecionarUnidade,
  limpar,
}
