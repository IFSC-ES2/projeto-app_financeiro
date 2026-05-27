package bcd.appfinanceirobackend.model;

import bcd.appfinanceirobackend.model.enums.StatusFatura;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Fatura {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "conta_id")
    private Conta conta;

    @Convert(converter = YearMonthStringConverter.class)
    @Column(name = "mes_referencia", length = 7)
    private YearMonth mesReferencia;

    @Column
    private LocalDate dataVencimento;

    @Column
    private BigDecimal valorTotal;

    @Enumerated(EnumType.STRING)
    @Column
    private StatusFatura status;

    @Converter
    static class YearMonthStringConverter implements AttributeConverter<YearMonth, String> {
        @Override
        public String convertToDatabaseColumn(YearMonth ym) {
            return ym == null ? null : ym.toString();
        }

        @Override
        public YearMonth convertToEntityAttribute(String s) {
            return s == null ? null : YearMonth.parse(s);
        }
    }
}
