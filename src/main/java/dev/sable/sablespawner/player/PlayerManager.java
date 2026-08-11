package dev.sable.sablespawner.player;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {
    public static final PlayerManager INSTANCE = new PlayerManager();

    private final Map<UUID, PlayerStatus> PlayerTracker = new HashMap<>();

    private PlayerManager() {
        // load from file
    }

    public void appendPlayer( ServerPlayer player ) {

    }

    private PlayerStatus buildNewPlayer( ServerPlayer player ) {
        return new PlayerStatus(player);
    }

    private PlayerStatus playerStatusBuilder() {
        // 读文件用
        return null;
    }

    // save data, on server closed
    private void savePlayerData() {

    }

    // load data, on server started
    private void loadPlayerData() {

    }



    @SubscribeEvent public void onPlayerJoinLevel(EntityJoinLevelEvent event) {
        if ( ! (event.getEntity() instanceof ServerPlayer player) ) { return; }


    }
    @SubscribeEvent public void onPlayerLeaveLevel(EntityLeaveLevelEvent event) {
        if ( ! (event.getEntity() instanceof ServerPlayer player) ) { return; }

    }


    @SubscribeEvent public void onPlayerJoinServer(PlayerEvent.PlayerLoggedInEvent event) {

    }

    @SubscribeEvent public void onPlayerLeaveServer(PlayerEvent.PlayerLoggedOutEvent event) {

    }


}
