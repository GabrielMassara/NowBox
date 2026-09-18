import { computed, reactive } from 'vue'
import { TOKEN_STORAGE_KEY } from '../lib/http'
import { decodeJwtPayload, isTokenExpired } from '../lib/jwt'
import { authService } from '../services/auth.service'
import type { UsuarioResponseDTO } from '../types/api'

const storedToken = localStorage.getItem(TOKEN_STORAGE_KEY)

const state = reactive({
  token: storedToken && !isTokenExpired(storedToken) ? storedToken : null,
  usuario: null as UsuarioResponseDTO | null,
  emailLogin: '',
})

if (!state.token) {
  localStorage.removeItem(TOKEN_STORAGE_KEY)
}

const usuarioId = computed(() => (state.token ? decodeJwtPayload(state.token)?.sub ?? null : null))
const isAuthenticated = computed(() => !!state.token)
const nomeExibicao = computed(() => state.usuario?.nome || state.emailLogin || 'Usuário')

function carregarUsuario() {
  if (!usuarioId.value) return
  authService
    .buscarUsuario(usuarioId.value)
    .then((usuario) => (state.usuario = usuario))
    .catch(() => {})
}

async function login(email: string, senha: string) {
  const { token } = await authService.login({ email, senha })
  state.token = token
  state.emailLogin = email
  localStorage.setItem(TOKEN_STORAGE_KEY, token)
  carregarUsuario()
}

if (state.token) {
  carregarUsuario()
}

function logout() {
  state.token = null
  state.usuario = null
  localStorage.removeItem(TOKEN_STORAGE_KEY)
  window.dispatchEvent(new Event('nowbox:logout'))
}

window.addEventListener('nowbox:unauthorized', logout)

export const authStore = {
  state,
  usuarioId,
  isAuthenticated,
  nomeExibicao,
  login,
  logout,
}
