package com.nexthome.collector.molit;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MolitTradeItem(
        String apartmentSequence,
        String apartmentName,
        String legalDongName,
        String lotNumber,
        long priceKrw,
        BigDecimal exclusiveAreaSqm,
        LocalDate contractDate,
        Integer floor,
        Integer buildYear,
        LocalDate cancellationDate) {

    public String address() {
        return legalDongName + " " + lotNumber;
    }

    public String sourceKey(String regionCode) {
        return String.join("|", regionCode, nullToEmpty(apartmentSequence), apartmentName,
                contractDate.toString(), exclusiveAreaSqm.toPlainString(), String.valueOf(floor),
                String.valueOf(priceKrw));
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
