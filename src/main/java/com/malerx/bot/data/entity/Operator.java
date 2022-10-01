package com.malerx.bot.data.entity;

import com.malerx.bot.data.enums.Role;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Сущность определяющая оператора системы.
 */
@Entity
@Getter
@Setter
public class Operator {
    @Id
    private Long id;
    private Role role;
    private Boolean active;
}
