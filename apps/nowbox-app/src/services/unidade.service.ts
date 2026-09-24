import { http } from '../lib/http'
import type {
  AtribuicaoResponseDTO,
  CargoEntity,
  PageResponse,
  UnidadeCreateDTO,
  UnidadeEntity,
  UnidadeResponseDTO,
} from '../types/api'

export interface MinhaUnidade {
  unidade: UnidadeEntity
  cargo: CargoEntity
}

export interface UnidadeFiltro {
  idEstado?: string
  nome?: string
  cnpj?: string
  cidade?: string
}

export const unidadeService = {
  listar(pagina: number, tamanho: number, filtro: UnidadeFiltro = {}) {
    const params = new URLSearchParams({ page: String(pagina), size: String(tamanho) })
    if (filtro.idEstado) params.set('idEstado', filtro.idEstado)
    if (filtro.nome) params.set('nome', filtro.nome)
    if (filtro.cnpj) params.set('cnpj', filtro.cnpj)
    if (filtro.cidade) params.set('cidade', filtro.cidade)

    return http.get<PageResponse<UnidadeResponseDTO>>(`/v1/unidade?${params}`)
  },

  buscarPorId(id: string) {
    return http.get<UnidadeResponseDTO>(`/v1/unidade/${id}`)
  },

  criar(dados: UnidadeCreateDTO) {
    return http.post<UnidadeResponseDTO>('/v1/unidade', dados)
  },

  atualizar(id: string, dados: UnidadeCreateDTO) {
    return http.put<UnidadeResponseDTO>(`/v1/unidade/${id}`, dados)
  },

  excluir(id: string) {
    return http.delete<void>(`/v1/unidade/${id}`)
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
