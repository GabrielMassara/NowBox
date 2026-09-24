import { http } from '../lib/http'
import type { PageResponse, UsuarioCreateDTO, UsuarioResponseDTO } from '../types/api'

export interface UsuarioFiltro {
  nome?: string
  email?: string
  cpf?: string
}

export const usuarioService = {
  listar(pagina: number, tamanho: number, filtro: UsuarioFiltro = {}) {
    const params = new URLSearchParams({ page: String(pagina), size: String(tamanho) })
    if (filtro.nome) params.set('nome', filtro.nome)
    if (filtro.email) params.set('email', filtro.email)
    if (filtro.cpf) params.set('cpf', filtro.cpf)

    return http.get<PageResponse<UsuarioResponseDTO>>(`/v1/usuario?${params}`)
  },

  buscarPorId(id: string) {
    return http.get<UsuarioResponseDTO>(`/v1/usuario/${id}`)
  },

  criar(dados: UsuarioCreateDTO) {
    return http.post<UsuarioResponseDTO>('/v1/usuario', dados)
  },

  atualizar(id: string, dados: UsuarioCreateDTO) {
    return http.put<UsuarioResponseDTO>(`/v1/usuario/${id}`, dados)
  },

  excluir(id: string) {
    return http.delete<void>(`/v1/usuario/${id}`)
  },
}
