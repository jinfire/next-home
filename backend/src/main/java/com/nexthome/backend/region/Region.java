package com.nexthome.backend.region;

import jakarta.persistence.*;

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

    public Long id() { return id; }
    public String code() { return code; }
    public String name() { return name; }
    public int level() { return level; }
}
