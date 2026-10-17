package com.inwza.player.config;

import com.inwza.player.application.PlayerLedgerService;
import com.inwza.player.application.PlayerIdCodec;
import com.inwza.player.infrastructure.persistence.PlayerEntity;
import com.inwza.player.infrastructure.persistence.PlayerJpaRepository;
import com.inwza.player.infrastructure.persistence.PlayerRoleEntity;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@Profile("local")
public class LocalPlayerDataSeeder implements ApplicationRunner {

    private final PlayerJpaRepository players;
    private final PlayerLedgerService ledger;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public LocalPlayerDataSeeder(PlayerJpaRepository players, PlayerLedgerService ledger) {
        this.players = players;
        this.ledger = ledger;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (players.count() > 0) {
            return;
        }

        seed("player001@Iwpshop.com", "Player001", List.of(new RoleSeed("player", 0)));
        seed("player002@Iwpshop.com", "Player002", List.of(new RoleSeed("player", 0)));
        seed("player003@Iwpshop.com", "Player003", List.of(new RoleSeed("player", 0)));
        seed("admin001@Iwpshop.com", "Admin001", List.of(new RoleSeed("player", 0), new RoleSeed("admin", 1)));
    }

    private void seed(String email, String username, List<RoleSeed> roleSeeds) {
        PlayerEntity entity = new PlayerEntity();
        entity.setEmail(email);
        entity.setUsername(username);
        entity.setPassword(encoder.encode("123456"));
        players.save(entity);
        for (RoleSeed roleSeed : roleSeeds) {
            entity.getRoles().add(PlayerRoleEntity.of(entity, roleSeed.title(), roleSeed.code()));
        }
        players.save(entity);
        ledger.recordTransaction(PlayerIdCodec.format(entity.getId().toString()), BigDecimal.valueOf(1000), null);
    }

    private record RoleSeed(String title, int code) {
    }
}
