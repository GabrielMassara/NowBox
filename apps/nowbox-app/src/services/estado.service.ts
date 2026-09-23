import { http } from '../lib/http'
import type { EstadoEntity, PagedResponse } from '../types/api'

export const estadoService = {
  listar(pagina: number, tamanho: number) {
    const params = new URLSearchParams({ page: String(pagina), size: String(tamanho) })

    return http.get<PagedResponse<EstadoEntity>>(`/v1/estado?${params}`)
  },

  buscarPorId(id: string) {
    return http.get<EstadoEntity>(`/v1/estado/${id}`)
  },
}
