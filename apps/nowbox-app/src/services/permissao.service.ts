import { http } from '../lib/http'
import type { PageResponse, PermissaoCreateDTO, PermissaoLoteDTO, PermissaoResponseDTO } from '../types/api'

export interface PermissaoFiltro {
  idCargo?: string
  idOperacao?: string
}

export const permissaoService = {
  listar(pagina: number, tamanho: number, filtro: PermissaoFiltro = {}) {
    const params = new URLSearchParams({ page: String(pagina), size: String(tamanho) })
    if (filtro.idCargo) params.set('idCargo', filtro.idCargo)
    if (filtro.idOperacao) params.set('idOperacao', filtro.idOperacao)

    return http.get<PageResponse<PermissaoResponseDTO>>(`/v1/permissao?${params}`)
  },

  buscarPorId(id: string) {
    return http.get<PermissaoResponseDTO>(`/v1/permissao/${id}`)
  },

  criar(dados: PermissaoCreateDTO) {
    return http.post<PermissaoResponseDTO>('/v1/permissao', dados)
  },

  atualizar(id: string, dados: PermissaoCreateDTO) {
    return http.put<PermissaoResponseDTO>(`/v1/permissao/${id}`, dados)
  },

  // Substitui, em uma única chamada, todas as operações liberadas para o cargo.
  sincronizarCargo(idCargo: string, dados: PermissaoLoteDTO) {
    return http.put<PermissaoResponseDTO[]>(`/v1/permissao/cargo/${idCargo}`, dados)
  },

  excluir(id: string) {
    return http.delete<void>(`/v1/permissao/${id}`)
  },
}
