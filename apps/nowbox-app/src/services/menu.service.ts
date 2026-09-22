import { http } from '../lib/http'
import type { MenuSessaoResponseDTO } from '../types/api'

export const menuService = {
  listar() {
    return http.get<MenuSessaoResponseDTO[]>('/v1/menu')
  },
}
