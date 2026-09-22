import { http } from '../lib/http'
import type { OperacaoCreateDTO, OperacaoResponseDTO, PageResponse } from '../types/api'

export interface OperacaoFiltro {
  idModulo?: string
  nome?: string
  codigo?: string
}

export const operacaoService = {
  listar(pagina: number, tamanho: number, filtro: OperacaoFiltro = {}) {
    const params = new URLSearchParams({ page: String(pagina), size: String(tamanho) })
    if (filtro.idModulo) params.set('idModulo', filtro.idModulo)
    if (filtro.nome) params.set('nome', filtro.nome)
    if (filtro.codigo) params.set('codigo', filtro.codigo)

    return http.get<PageResponse<OperacaoResponseDTO>>(`/v1/operacao?${params}`)
  },

  buscarPorId(id: string) {
    return http.get<OperacaoResponseDTO>(`/v1/operacao/${id}`)
  },

  criar(dados: OperacaoCreateDTO) {
    return http.post<OperacaoResponseDTO>('/v1/operacao', dados)
  },

  atualizar(id: string, dados: OperacaoCreateDTO) {
    return http.put<OperacaoResponseDTO>(`/v1/operacao/${id}`, dados)
  },

  excluir(id: string) {
    return http.delete<void>(`/v1/operacao/${id}`)
  },
}
