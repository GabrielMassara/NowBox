import { reactive } from 'vue'
import { menuService } from '../services/menu.service'
import type { MenuSessaoResponseDTO } from '../types/api'
import { authStore } from './auth'

const state = reactive({
  sessoes: [] as MenuSessaoResponseDTO[],
  carregando: false,
  erro: null as string | null,
})

async function carregar() {
  state.carregando = true
  state.erro = null

  try {
    const sessoes = await menuService.listar()
    if (authStore.isAuthenticated.value) state.sessoes = sessoes
  } catch {
    state.erro = 'Não foi possível carregar o menu.'
  } finally {
    state.carregando = false
  }
}

function limpar() {
  state.sessoes = []
  state.erro = null
}

window.addEventListener('nowbox:unauthorized', limpar)
window.addEventListener('nowbox:logout', limpar)

export const menuStore = {
  state,
  carregar,
  limpar,
}
