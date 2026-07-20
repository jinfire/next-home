package com.nexthome.backend.trade;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.nexthome.backend.apartment.Apartment;
import jakarta.persistence.*;

@Entity
@Table(name = "trade")
class Trade {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "apartment_id") private Apartment apartment;
    @Column(name = "contract_date") private LocalDate contractDate;
    @Column(name = "price_krw") private long priceKrw;
    @Column(name = "exclusive_area_sqm") private BigDecimal exclusiveAreaSqm;
    private Short floor;
    @Column(name = "cancellation_date") private LocalDate cancellationDate;
    protected Trade() {}
    Long id() { return id; } LocalDate contractDate() { return contractDate; } long priceKrw() { return priceKrw; }
    BigDecimal exclusiveAreaSqm() { return exclusiveAreaSqm; } Integer floor() { return floor == null ? null : floor.intValue(); }
    boolean cancelled() { return cancellationDate != null; }
}
