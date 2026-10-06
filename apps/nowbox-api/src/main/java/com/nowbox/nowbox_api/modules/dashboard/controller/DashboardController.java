package com.nowbox.nowbox_api.modules.dashboard.controller;

import com.nowbox.nowbox_api.modules.dashboard.dto.DashboardAluguelStatusDTO;
import com.nowbox.nowbox_api.modules.dashboard.dto.DashboardEvolucaoDTO;
import com.nowbox.nowbox_api.modules.dashboard.dto.DashboardOcupacaoDTO;
import com.nowbox.nowbox_api.modules.dashboard.dto.DashboardPendenciaDTO;
import com.nowbox.nowbox_api.modules.dashboard.dto.DashboardResumoDTO;
import com.nowbox.nowbox_api.modules.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/resumo")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('MOD_DASHBOARD_OPE_RESUMO') and @acessoUnidadeService.temAcesso(#idUnidade)")
    public DashboardResumoDTO resumo(@RequestParam UUID idUnidade) {
        return dashboardService.resumo(idUnidade);
    }

    @GetMapping("/ocupacao")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('MOD_DASHBOARD_OPE_OCUPACAO') and @acessoUnidadeService.temAcesso(#idUnidade)")
    public DashboardOcupacaoDTO ocupacao(@RequestParam UUID idUnidade) {
        return dashboardService.ocupacao(idUnidade);
    }

    @GetMapping("/alugueis-por-status")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('MOD_DASHBOARD_OPE_ALUGUEIS_STATUS') and @acessoUnidadeService.temAcesso(#idUnidade)")
    public List<DashboardAluguelStatusDTO> alugueisPorStatus(@RequestParam UUID idUnidade) {
        return dashboardService.alugueisPorStatus(idUnidade);
    }

    @GetMapping("/evolucao-alugueis")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('MOD_DASHBOARD_OPE_EVOLUCAO') and @acessoUnidadeService.temAcesso(#idUnidade)")
    public List<DashboardEvolucaoDTO> evolucaoAlugueis(@RequestParam UUID idUnidade) {
        return dashboardService.evolucaoAlugueis(idUnidade);
    }

    @GetMapping("/pendencias-assinatura")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('MOD_DASHBOARD_OPE_PENDENCIAS') and @acessoUnidadeService.temAcesso(#idUnidade)")
    public List<DashboardPendenciaDTO> pendenciasAssinatura(@RequestParam UUID idUnidade) {
        return dashboardService.pendenciasAssinatura(idUnidade);
    }

}
