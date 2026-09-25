import { http } from '../lib/http'
import type { BoxCreateDTO, BoxLoteDTO, BoxResponseDTO, PageResponse } from '../types/api'

export interface BoxFiltro {
  idUnidade: string
  numero?: string
  disponivel?: boolean
}

export const boxService = {
  listar(pagina: number, tamanho: number, filtro: BoxFiltro) {
    const params = new URLSearchParams({
      page: String(pagina),
      size: String(tamanho),
      sort: 'numero,asc',
      idUnidade: filtro.idUnidade,
    })
    if (filtro.numero) params.set('numero', filtro.numero)
    if (filtro.disponivel !== undefined) params.set('disponivel', String(filtro.disponivel))

    return http.get<PageResponse<BoxResponseDTO>>(`/v1/box?${params}`)
  },

  buscarPorId(id: string) {
    return http.get<BoxResponseDTO>(`/v1/box/${id}`)
  },

  criar(dados: BoxCreateDTO) {
    return http.post<BoxResponseDTO>('/v1/box', dados)
  },

  // Cadastra em uma única chamada os boxes do intervalo, ignorando os números que já existem na unidade.
  criarEmLote(dados: BoxLoteDTO) {
    return http.post<BoxResponseDTO[]>('/v1/box/lote', dados)
  },

  atualizar(id: string, dados: BoxCreateDTO) {
    return http.put<BoxResponseDTO>(`/v1/box/${id}`, dados)
  },

  excluir(id: string) {
    return http.delete<void>(`/v1/box/${id}`)
  },
}
