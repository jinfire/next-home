package com.nexthome.backend.grade;

import java.math.BigDecimal;
import com.nexthome.backend.region.Region;
import jakarta.persistence.*;

@Entity
@Table(name = "region_grade")
class RegionGrade {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "region_id") private Region region;
    private short year;
    @Column(name = "average_price_per_pyeong") private BigDecimal averagePricePerPyeong;
    private short grade;
    @Column(name = "trade_count") private int tradeCount;
    protected RegionGrade() {}
    static RegionGrade create(Region region, CalculatedRegionGrade value) {
        RegionGrade entity = new RegionGrade(); entity.region = region; entity.year = (short) value.year();
        entity.averagePricePerPyeong = value.averagePricePerPyeong(); entity.grade = (short) value.grade(); entity.tradeCount = value.tradeCount(); return entity;
    }
    Region region() { return region; } int year() { return year; } BigDecimal average() { return averagePricePerPyeong; }
    int grade() { return grade; } int tradeCount() { return tradeCount; }
}
