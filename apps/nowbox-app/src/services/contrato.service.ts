import { salvarArquivo } from '../lib/arquivo'
import { http } from '../lib/http'
import type { AditivoResponseDTO, PageResponse } from '../types/api'

function listar(path: string, pagina: number, tamanho: number) {
  const params = new URLSearchParams({ page: String(pagina), size: String(tamanho) })
  return http.get<PageResponse<AditivoResponseDTO>>(`${path}?${params}`)
}

export const contratoService = {
  listarAditivosPorAluguel(idAluguel: string, pagina: number, tamanho: number) {
    return listar(`/v1/contrato/aluguel/${idAluguel}/aditivos`, pagina, tamanho)
  },

  async baixarAditivo(id: string, nomeSugerido = 'aditivo.pdf') {
    const { blob, nomeArquivo } = await http.download(`/v1/contrato/aditivo/${id}/download`)
    salvarArquivo(blob, nomeArquivo ?? nomeSugerido)
  },

  async baixarDistrato(idAluguel: string) {
    const { blob, nomeArquivo } = await http.download(`/v1/contrato/aluguel/${idAluguel}/distrato/download`)
    salvarArquivo(blob, nomeArquivo ?? 'distrato.pdf')
  },

  // Contrato original do aluguel, que nao muda quando o aluguel e editado
  async baixarContrato(idAluguel: string) {
    const { blob, nomeArquivo } = await http.download(`/v1/contrato/aluguel/${idAluguel}/download`)
    salvarArquivo(blob, nomeArquivo ?? 'contrato.pdf')
  },
}
