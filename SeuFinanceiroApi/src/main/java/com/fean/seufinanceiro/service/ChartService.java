package com.fean.seufinanceiro.service;

import com.fean.seufinanceiro.dto.ChartDto;
import com.fean.seufinanceiro.dto.FluxoDeCaixaMensalDto;
import com.fean.seufinanceiro.model.Movimentacao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChartService {

    private MovimentacaoService movimentacaoService;

    @Autowired
    public ChartService(MovimentacaoService movimentacaoService) {
        this.movimentacaoService = movimentacaoService;
    }


    public ChartDto findChartByYear(String year, Long userId) {
        ChartDto chartDto = new ChartDto();
        List<FluxoDeCaixaMensalDto> fluxoDeCaixaMensalDtos = new ArrayList<>();

        for(int month = 0; month < 12; month++){
            FluxoDeCaixaMensalDto fluxoDeCaixaMensalDto = new FluxoDeCaixaMensalDto(month);

            List<Movimentacao> movimentacaos = movimentacaoService.showAllDespesasByYearMonth(year, String.valueOf(month), userId);
            movimentacaos.forEach( movimentacao -> {
                fluxoDeCaixaMensalDto.setFluxoDeCaixa(fluxoDeCaixaMensalDto.getFluxoDeCaixa() + movimentacao.getValor());
            });

            fluxoDeCaixaMensalDtos.add(fluxoDeCaixaMensalDto);
        }

        chartDto.getFluxoDeCaixaMensalDtos().addAll(fluxoDeCaixaMensalDtos);
        return chartDto;
    }
}
