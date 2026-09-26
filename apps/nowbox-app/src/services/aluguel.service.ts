import { http } from '../lib/http'
import type { AluguelCreateDTO, AluguelResponseDTO, PageResponse } from '../types/api'

export interface AluguelFiltro {
  // A API só lista aluguéis de um box por vez, pois valida o acesso à unidade a partir dele.
  idBox: string
  idCliente?: string
  status?: boolean
}

export const aluguelService = {
  listar(pagina: number, tamanho: number, filtro: AluguelFiltro) {
    const params = new URLSearchParams({
      page: String(pagina),
      size: String(tamanho),
      sort: 'createdAt,desc',
      idBox: filtro.idBox,
    })
    if (filtro.idCliente) params.set('idCliente', filtro.idCliente)
    if (filtro.status !== undefined) params.set('status', String(filtro.status))

    return http.get<PageResponse<AluguelResponseDTO>>(`/v1/aluguel?${params}`)
  },

  buscarPorId(id: string) {
    return http.get<AluguelResponseDTO>(`/v1/aluguel/${id}`)
  },

  criar(dados: AluguelCreateDTO) {
    return http.post<AluguelResponseDTO>('/v1/aluguel', dados)
  },

  atualizar(id: string, dados: AluguelCreateDTO) {
    return http.put<AluguelResponseDTO>(`/v1/aluguel/${id}`, dados)
  },

  excluir(id: string) {
    return http.delete<void>(`/v1/aluguel/${id}`)
  },
}
