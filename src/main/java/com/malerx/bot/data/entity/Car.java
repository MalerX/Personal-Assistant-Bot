package com.malerx.bot.data.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Accessors(chain = true)
public class Car {
    @Id
    @GeneratedValue
    private Long id;
    private String model;
    private String color;
    private String regNumber;
    @ManyToOne
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;
}
