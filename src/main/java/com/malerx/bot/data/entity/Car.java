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
public class Car {
    @Id
    @GeneratedValue
    private Long id;
    private String model;
    private String color;
    private String regNumber;

    @Override
    public String toString() {
        String sb = "Модель: " +
                ";\nцвет: " +
                color +
                ";\nрегистрационный номер: " +
                regNumber;
        return """
                модель: %s
                цветЖ %s
                гос номер: %s"""
                .formatted(this.model, this.color, this.regNumber);
    }
}
