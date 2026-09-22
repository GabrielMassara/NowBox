import { http } from '../lib/http'
import type { PagedResponse, SessaoCreateDTO, SessaoResponseDTO } from '../types/api'

export interface SessaoFiltro {
  nome?: string
  rota?: string
}

export const sessaoService = {
  listar(pagina: number, tamanho: number, filtro: SessaoFiltro = {}) {
    const params = new URLSearchParams({ page: String(pagina), size: String(tamanho) })
    if (filtro.nome) params.set('nome', filtro.nome)
    if (filtro.rota) params.set('rota', filtro.rota)

    return http.get<PagedResponse<SessaoResponseDTO>>(`/v1/sessao?${params}`)
  },

  buscarPorId(id: string) {
    return http.get<SessaoResponseDTO>(`/v1/sessao/${id}`)
  },

  criar(dados: SessaoCreateDTO) {
    return http.post<SessaoResponseDTO>('/v1/sessao', dados)
  },

  atualizar(id: string, dados: SessaoCreateDTO) {
    return http.put<void>(`/v1/sessao/${id}`, dados)
  },

  excluir(id: string) {
    return http.delete<void>(`/v1/sessao/${id}`)
  },
}
