package com.malerx.bot.data.entity;

import com.malerx.bot.data.enums.Role;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.Set;

@Entity
@Getter
@Setter
@Accessors(chain = true)
@ToString
public class TGUser {
    @Id
    private Long id;
    private String nickname;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<GpgRecord> gpgPublicKeys;
    private Role role;
    @OneToOne(cascade = CascadeType.ALL)
    private Tenant tenant;
}
