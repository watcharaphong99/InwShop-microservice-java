package com.inwza.player.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "player_roles")
@IdClass(PlayerRoleEntity.PlayerRoleId.class)
public class PlayerRoleEntity {

    @Id
    @Column(name = "player_id", nullable = false)
    private UUID playerId;

    @Id
    @Column(name = "role_code", nullable = false)
    private int roleCode;

    @Column(name = "role_title", nullable = false, length = 64)
    private String roleTitle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", insertable = false, updatable = false)
    private PlayerEntity player;

    public static PlayerRoleEntity of(PlayerEntity player, String title, int code) {
        PlayerRoleEntity role = new PlayerRoleEntity();
        role.playerId = player.getId();
        role.player = player;
        role.roleTitle = title;
        role.roleCode = code;
        return role;
    }

    public int getRoleCode() {
        return roleCode;
    }

    public String getRoleTitle() {
        return roleTitle;
    }

    public record PlayerRoleId(UUID playerId, int roleCode) implements Serializable {
    }
}
