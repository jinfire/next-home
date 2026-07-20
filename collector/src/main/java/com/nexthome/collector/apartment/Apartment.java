package com.nexthome.collector.apartment;

import com.nexthome.collector.region.Region;
import jakarta.persistence.*;

@Entity
@Table(name = "apartment")
public class Apartment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;
    @Column(name = "external_id", length = 100)
    private String externalId;
    @Column(nullable = false, length = 200)
    private String name;
    @Column(nullable = false, length = 300)
    private String address;
    @Column(name = "build_year")
    private Short buildYear;

    protected Apartment() {}

    private Apartment(Region region, String externalId, String name, String address, Integer buildYear) {
        this.region = region;
        this.externalId = externalId;
        this.name = name;
        this.address = address;
        this.buildYear = buildYear == null ? null : buildYear.shortValue();
    }

    public static Apartment create(Region region, String externalId, String name, String address, Integer buildYear) {
        return new Apartment(region, externalId, name, address, buildYear);
    }
}
