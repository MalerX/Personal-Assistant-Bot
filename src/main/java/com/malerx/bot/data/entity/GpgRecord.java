package com.malerx.bot.data.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@Accessors(chain = true)
public class GpgRecord {
    @Id
    @GeneratedValue
    private Long id;
    @Column(name = "pgp_key")
    private String pgpKey;

    @Override
    public String toString() {
        return this.pgpKey;
    }
}
