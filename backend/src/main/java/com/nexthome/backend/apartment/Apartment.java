package com.nexthome.backend.apartment;

import com.nexthome.backend.region.Region;
import jakarta.persistence.*;

@Entity
@Table(name = "apartment")
public class Apartment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;
    @Column(nullable = false) private String name;
    @Column(nullable = false) private String address;
    @Column(name = "build_year") private Short buildYear;
    protected Apartment() {}
    public Long id() { return id; }
    public Region region() { return region; }
    public String name() { return name; }
    public String address() { return address; }
    public Integer buildYear() { return buildYear == null ? null : buildYear.intValue(); }
}
