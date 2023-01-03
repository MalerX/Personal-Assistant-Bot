package com.malerx.bot.data.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@Accessors(chain = true)
public class Address {
    @Id
    @GeneratedValue
    private Long id;
    private String street;
    private String build;
    private String apartment;

    @Override
    public String toString() {
        return this.build + "/" + this.apartment;
    }
}
