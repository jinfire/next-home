package com.nexthome.collector.region;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "region")
public class Region {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 10)
    private String code;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false)
    private short level;

    protected Region() {}

    private Region(String code, String name, int level) {
        this.code = code;
        this.name = name;
        this.level = (short) level;
    }

    public static Region create(String code, String name, int level) {
        return new Region(code, name, level);
    }
}
