import { http } from '../lib/http'
import type { ClienteCreateDTO, ClienteResponseDTO, PageResponse } from '../types/api'

export interface ClienteFiltro {
  idEstado?: string
  nome?: string
  cpf?: string
  email?: string
}

export const clienteService = {
  listar(pagina: number, tamanho: number, filtro: ClienteFiltro = {}) {
    const params = new URLSearchParams({ page: String(pagina), size: String(tamanho) })
    if (filtro.idEstado) params.set('idEstado', filtro.idEstado)
    if (filtro.nome) params.set('nome', filtro.nome)
    if (filtro.cpf) params.set('cpf', filtro.cpf)
    if (filtro.email) params.set('email', filtro.email)

    return http.get<PageResponse<ClienteResponseDTO>>(`/v1/cliente?${params}`)
  },

  buscarPorId(id: string) {
    return http.get<ClienteResponseDTO>(`/v1/cliente/${id}`)
  },

  criar(dados: ClienteCreateDTO) {
    return http.post<ClienteResponseDTO>('/v1/cliente', dados)
  },

  atualizar(id: string, dados: ClienteCreateDTO) {
    return http.put<ClienteResponseDTO>(`/v1/cliente/${id}`, dados)
  },

  excluir(id: string) {
    return http.delete<void>(`/v1/cliente/${id}`)
  },
}
