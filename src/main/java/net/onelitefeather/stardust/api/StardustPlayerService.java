package net.onelitefeather.stardust.api;

import net.onelitefeather.stardust.api.player.StardustPlayer;
import net.onelitefeather.stardust.service.UserService;
import net.onelitefeather.stardust.user.User;

import java.util.List;
import java.util.UUID;

public interface StardustPlayerService {

    List<StardustPlayer> findAll(int limit);

    List<StardustPlayer> findOnlinePlayers();

    StardustPlayer findById(UUID playerUuid);

    void saveOrUpdate(StardustPlayer player);

    void delete(StardustPlayer player);

    boolean playerExists(UUID playerUuid);


    @Deprecated(forRemoval = true, since = "1.15.4")
    void migrateUser(User user, UserService userService);
}
