import { http } from '../lib/http'
import type {
  DashboardAluguelStatusDTO,
  DashboardEvolucaoDTO,
  DashboardOcupacaoDTO,
  DashboardPendenciaDTO,
  DashboardResumoDTO,
} from '../types/api'

export const dashboardService = {
  resumo(idUnidade: string) {
    return http.get<DashboardResumoDTO>(`/v1/dashboard/resumo?idUnidade=${idUnidade}`)
  },

  ocupacao(idUnidade: string) {
    return http.get<DashboardOcupacaoDTO>(`/v1/dashboard/ocupacao?idUnidade=${idUnidade}`)
  },

  alugueisPorStatus(idUnidade: string) {
    return http.get<DashboardAluguelStatusDTO[]>(`/v1/dashboard/alugueis-por-status?idUnidade=${idUnidade}`)
  },

  evolucaoAlugueis(idUnidade: string) {
    return http.get<DashboardEvolucaoDTO[]>(`/v1/dashboard/evolucao-alugueis?idUnidade=${idUnidade}`)
  },

  pendenciasAssinatura(idUnidade: string) {
    return http.get<DashboardPendenciaDTO[]>(`/v1/dashboard/pendencias-assinatura?idUnidade=${idUnidade}`)
  },
}
