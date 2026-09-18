import { http } from '../lib/http'
import type { LoginRequestDTO, LoginResponseDTO, UsuarioResponseDTO } from '../types/api'

export const authService = {
  login(credenciais: LoginRequestDTO) {
    return http.post<LoginResponseDTO>('/v1/auth/login', credenciais)
  },

  buscarUsuario(id: string) {
    return http.get<UsuarioResponseDTO>(`/v1/usuario/${id}`)
  },
}
