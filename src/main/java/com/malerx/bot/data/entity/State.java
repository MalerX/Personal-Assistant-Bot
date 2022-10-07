package com.malerx.bot.data.entity;

import com.malerx.bot.data.enums.Stage;
import com.malerx.bot.data.enums.Step;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@Accessors(chain = true)
@ToString
public class State {
    @Id
    private Long id;
    @Column(name = "state_machine")
    private String stateMachine;
    private Stage stage;
    private String message;
    private Step step;
    private String description;

    public SendMessage toMessage() {
        return new SendMessage(this.id.toString(), this.message);
    }
}
