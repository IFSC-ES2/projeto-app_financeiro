package bcd.appfinanceirobackend.mapper;

import bcd.appfinanceirobackend.dto.fatura.FaturaResumoDTO;
import bcd.appfinanceirobackend.model.Fatura;
import org.springframework.stereotype.Component;

@Component
public class FaturaMapper {

    public FaturaMapper() {}

    public FaturaResumoDTO toResumo(Fatura fatura) {
        FaturaResumoDTO resumoDTO = new FaturaResumoDTO();
        resumoDTO.setFaturaId(fatura.getId());
        resumoDTO.setMesReferencia(
                fatura.getMesReferencia() != null ? fatura.getMesReferencia().toString() : null
        );
        resumoDTO.setDataVencimento(fatura.getDataVencimento());
        resumoDTO.setValorTotal(fatura.getValorTotal());
        resumoDTO.setStatus(fatura.getStatus());
        if (fatura.getConta() != null) {
            resumoDTO.setContaId(fatura.getConta().getId());
            resumoDTO.setContaNome(fatura.getConta().getNome());
        }
        return resumoDTO;
    }
}
