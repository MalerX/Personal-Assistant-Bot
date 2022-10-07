package com.malerx.bot.data.entity;

import com.malerx.bot.data.enums.Stage;
import com.malerx.bot.data.enums.Step;
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
public class State {
    @Id
    @GeneratedValue
    private Long id;
    private Long chatId;
    @Column(name = "state_machine")
    private String stateMachine;
    private Stage stage;
    private Step step;
    private String description;
    @DateCreated
    private Date start;
    @DateUpdated
    private Date process;
    @Transient
    private Object message;

    public Object toMessage() {
        if (message instanceof String)
            return new SendMessage(
                    this.chatId.toString(),
                    this.message.toString()
            );
        return this.message;
    }
}
