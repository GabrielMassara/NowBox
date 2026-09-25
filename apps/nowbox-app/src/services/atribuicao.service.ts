import { http } from '../lib/http'
import type { AtribuicaoCreateDTO, AtribuicaoResponseDTO, PageResponse } from '../types/api'

export interface AtribuicaoFiltro {
  idUsuario?: string
  idCargo?: string
}

export const atribuicaoService = {
  listar(pagina: number, tamanho: number, filtro: AtribuicaoFiltro = {}) {
    const params = new URLSearchParams({ page: String(pagina), size: String(tamanho) })
    if (filtro.idUsuario) params.set('idUsuario', filtro.idUsuario)
    if (filtro.idCargo) params.set('idCargo', filtro.idCargo)

    return http.get<PageResponse<AtribuicaoResponseDTO>>(`/v1/atribuicao?${params}`)
  },

  buscarPorId(id: string) {
    return http.get<AtribuicaoResponseDTO>(`/v1/atribuicao/${id}`)
  },

  criar(dados: AtribuicaoCreateDTO) {
    return http.post<AtribuicaoResponseDTO>('/v1/atribuicao', dados)
  },

  atualizar(id: string, dados: AtribuicaoCreateDTO) {
    return http.put<AtribuicaoResponseDTO>(`/v1/atribuicao/${id}`, dados)
  },

  excluir(id: string) {
    return http.delete<void>(`/v1/atribuicao/${id}`)
  },
}
