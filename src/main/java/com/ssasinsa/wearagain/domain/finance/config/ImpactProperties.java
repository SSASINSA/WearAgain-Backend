package com.ssasinsa.wearagain.domain.finance.config;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "impact")
public record ImpactProperties(
        BigDecimal co2PerTicket,
        BigDecimal waterPerTicket,
        BigDecimal energyPerTicket
) {

    public ImpactProperties {
        co2PerTicket = defaultZero(co2PerTicket);
        waterPerTicket = defaultZero(waterPerTicket);
        energyPerTicket = defaultZero(energyPerTicket);
    }

    private static BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
