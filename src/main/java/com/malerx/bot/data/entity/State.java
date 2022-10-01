package com.malerx.bot.data.entity;

import com.malerx.bot.data.enums.Stage;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class State {
    @Id
    private Long id;
    private String stateMachina;
    private Stage stage;
}
