import { salvarArquivo } from '../lib/arquivo'
import { http } from '../lib/http'
import type { ContratoResponseDTO, PageResponse } from '../types/api'

function listar(path: string, pagina: number, tamanho: number) {
  const params = new URLSearchParams({ page: String(pagina), size: String(tamanho) })
  return http.get<PageResponse<ContratoResponseDTO>>(`${path}?${params}`)
}

export const contratoService = {
  listarPorAluguel(idAluguel: string, pagina: number, tamanho: number) {
    return listar(`/v1/contrato/aluguel/${idAluguel}`, pagina, tamanho)
  },

  listarPorBox(idBox: string, pagina: number, tamanho: number) {
    return listar(`/v1/contrato/box/${idBox}`, pagina, tamanho)
  },

  async baixar(id: string, nomeSugerido = 'contrato.pdf') {
    const { blob, nomeArquivo } = await http.download(`/v1/contrato/${id}/download`)
    salvarArquivo(blob, nomeArquivo ?? nomeSugerido)
  },

  async baixarAtual(idAluguel: string) {
    const { blob, nomeArquivo } = await http.download(`/v1/contrato/aluguel/${idAluguel}/atual/download`)
    salvarArquivo(blob, nomeArquivo ?? 'contrato.pdf')
  },
}
