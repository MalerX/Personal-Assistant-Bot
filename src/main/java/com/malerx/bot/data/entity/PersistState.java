package com.malerx.bot.data.entity;

import com.malerx.bot.data.enums.Stage;
import com.malerx.bot.data.enums.Step;
import com.malerx.bot.data.model.OutgoingMessage;
import io.micronaut.data.annotation.DateCreated;
import io.micronaut.data.annotation.DateUpdated;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@Accessors(chain = true)
@ToString
public class PersistState {
    @Id
    @GeneratedValue
    private Long id;
    private Long chatId;
    private String stateMachine;
    private Stage stage;
    private Step step;
    private String description;
    @DateCreated
    private Date start;
    @DateUpdated
    private Date process;
}
