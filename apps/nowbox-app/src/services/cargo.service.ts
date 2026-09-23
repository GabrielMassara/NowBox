import { http } from '../lib/http'
import type { CargoCreateDTO, CargoResponseDTO, PageResponse } from '../types/api'

export interface CargoFiltro {
  idUnidade?: string
  nome?: string
}

export const cargoService = {
  listar(pagina: number, tamanho: number, filtro: CargoFiltro = {}) {
    const params = new URLSearchParams({ page: String(pagina), size: String(tamanho) })
    if (filtro.idUnidade) params.set('idUnidade', filtro.idUnidade)
    if (filtro.nome) params.set('nome', filtro.nome)

    return http.get<PageResponse<CargoResponseDTO>>(`/v1/cargo?${params}`)
  },

  buscarPorId(id: string) {
    return http.get<CargoResponseDTO>(`/v1/cargo/${id}`)
  },

  criar(dados: CargoCreateDTO) {
    return http.post<CargoResponseDTO>('/v1/cargo', dados)
  },

  atualizar(id: string, dados: CargoCreateDTO) {
    return http.put<CargoResponseDTO>(`/v1/cargo/${id}`, dados)
  },

  excluir(id: string) {
    return http.delete<void>(`/v1/cargo/${id}`)
  },
}
