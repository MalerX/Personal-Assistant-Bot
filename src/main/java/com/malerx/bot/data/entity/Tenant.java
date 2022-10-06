package com.malerx.bot.data.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.Set;

@Entity
@Getter
@Setter
@Accessors(chain = true)
public class Tenant {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String surname;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id")
    private Address address;
    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL)
    private Set<Car> cars;
    @OneToOne
    @JoinColumn(name = "tg_user_id")
    private TGUser tgUser;
}
