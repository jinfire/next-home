package com.nexthome.collector.trade;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.nexthome.collector.apartment.Apartment;
import com.nexthome.collector.molit.MolitTradeItem;
import jakarta.persistence.*;

@Entity
@Table(name = "trade")
public class Trade {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id", nullable = false)
    private Apartment apartment;
    @Column(name = "source_key", nullable = false, unique = true, length = 200)
    private String sourceKey;
    @Column(name = "contract_date", nullable = false)
    private LocalDate contractDate;
    @Column(name = "price_krw", nullable = false)
    private long priceKrw;
    @Column(name = "exclusive_area_sqm", nullable = false, precision = 10, scale = 4)
    private BigDecimal exclusiveAreaSqm;
    private Short floor;
    @Column(name = "cancellation_date")
    private LocalDate cancellationDate;

    protected Trade() {}

    public static Trade from(Apartment apartment, String sourceKey, MolitTradeItem item) {
        Trade trade = new Trade();
        trade.apartment = apartment;
        trade.sourceKey = sourceKey;
        trade.contractDate = item.contractDate();
        trade.priceKrw = item.priceKrw();
        trade.exclusiveAreaSqm = item.exclusiveAreaSqm();
        trade.floor = item.floor() == null ? null : item.floor().shortValue();
        trade.cancellationDate = item.cancellationDate();
        return trade;
    }
}
