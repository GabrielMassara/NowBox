import { http } from '../lib/http'
import type { ModuloCreateDTO, ModuloResponseDTO, PageResponse } from '../types/api'

export interface ModuloFiltro {
  idSessao?: string
  nome?: string
  rota?: string
}

export const moduloService = {
  listar(pagina: number, tamanho: number, filtro: ModuloFiltro = {}) {
    const params = new URLSearchParams({ page: String(pagina), size: String(tamanho) })
    if (filtro.idSessao) params.set('idSessao', filtro.idSessao)
    if (filtro.nome) params.set('nome', filtro.nome)
    if (filtro.rota) params.set('rota', filtro.rota)

    return http.get<PageResponse<ModuloResponseDTO>>(`/v1/modulo?${params}`)
  },

  buscarPorId(id: string) {
    return http.get<ModuloResponseDTO>(`/v1/modulo/${id}`)
  },

  criar(dados: ModuloCreateDTO) {
    return http.post<ModuloResponseDTO>('/v1/modulo', dados)
  },

  atualizar(id: string, dados: ModuloCreateDTO) {
    return http.put<ModuloResponseDTO>(`/v1/modulo/${id}`, dados)
  },

  excluir(id: string) {
    return http.delete<void>(`/v1/modulo/${id}`)
  },
}
