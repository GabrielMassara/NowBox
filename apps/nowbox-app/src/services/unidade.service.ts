import { http } from '../lib/http'
import type { AtribuicaoResponseDTO, CargoEntity, PageResponse, UnidadeEntity } from '../types/api'

export interface MinhaUnidade {
  unidade: UnidadeEntity
  cargo: CargoEntity
}

export const unidadeService = {
  listar(pagina: number, tamanho: number) {
    const params = new URLSearchParams({ page: String(pagina), size: String(tamanho) })

    return http.get<PageResponse<UnidadeEntity>>(`/v1/unidade?${params}`)
  },

  async listarMinhasUnidades(idUsuario: string): Promise<MinhaUnidade[]> {
    const params = new URLSearchParams({
      page: '0',
      size: '100',
      idUsuario,
    })

    const pagina = await http.get<PageResponse<AtribuicaoResponseDTO>>(`/v1/atribuicao?${params}`)

    const porUnidade = new Map<string, MinhaUnidade>()
    for (const atribuicao of pagina.content) {
      const unidade = atribuicao.cargo.unidade
      if (!porUnidade.has(unidade.id)) {
        porUnidade.set(unidade.id, { unidade, cargo: atribuicao.cargo })
      }
    }

    return [...porUnidade.values()].sort((a, b) => a.unidade.nome.localeCompare(b.unidade.nome))
  },
}
