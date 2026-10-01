import { salvarArquivo } from '../lib/arquivo'
import { http } from '../lib/http'
import type { ClienteCreateDTO, ClienteResponseDTO, DocumentoHistoricoDTO, PageResponse } from '../types/api'

export interface ClienteFiltro {
  idEstado?: string
  nome?: string
  cpf?: string
  email?: string
}

function montarFormulario(dados: ClienteCreateDTO, documento?: File | null) {
  const form = new FormData()
  form.append('cliente', new Blob([JSON.stringify(dados)], { type: 'application/json' }))
  if (documento) form.append('documento', documento)
  return form
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

  criar(dados: ClienteCreateDTO, documento: File) {
    return http.postForm<ClienteResponseDTO>('/v1/cliente', montarFormulario(dados, documento))
  },

  atualizar(id: string, dados: ClienteCreateDTO, documento?: File | null) {
    return http.putForm<ClienteResponseDTO>(`/v1/cliente/${id}`, montarFormulario(dados, documento))
  },

  obterDocumento(id: string) {
    return http.download(`/v1/cliente/${id}/documento`)
  },

  listarDocumentos(id: string, pagina: number, tamanho: number) {
    const params = new URLSearchParams({ page: String(pagina), size: String(tamanho) })
    return http.get<PageResponse<DocumentoHistoricoDTO>>(`/v1/cliente/${id}/documentos?${params}`)
  },

  obterDocumentoHistorico(id: string, idDocumento: string) {
    return http.download(`/v1/cliente/${id}/documentos/${idDocumento}`)
  },

  async baixarDocumentoHistorico(id: string, idDocumento: string, nomeSugerido = 'documento') {
    const { blob, nomeArquivo } = await http.download(`/v1/cliente/${id}/documentos/${idDocumento}`)
    salvarArquivo(blob, nomeArquivo ?? nomeSugerido)
  },

  excluir(id: string) {
    return http.delete<void>(`/v1/cliente/${id}`)
  },
}
