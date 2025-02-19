package com.ymylb;

import com.ymylb.config.YouMinedYourLastBlockConfig;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import net.minecraft.world.GameMode;

/**
 * Tracks the number of blocks mined by players and enforces consequences
 * when the mining limit is exceeded.
 */
public class BreakBlockStatTracker {

    /**
     * Registers the block break event listener. This listener tracks the total number
     * of blocks mined and applies penalties when the configured mining limit is reached.
     */
    public static void register() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            WorldRandomValueStorage serverState = WorldRandomValueStorage.getServerState(world.getServer());

            // Increment the total blocks mined counter
            serverState.setTotalBlocksMined(serverState.getTotalBlocksMined() + 1);

            // Check if the mining limit has been reached and if the mod is enabled
            if (serverState.getTotalBlocksMined() >= serverState.getBlockMinedLimit() && YouMinedYourLastBlockConfig.INSTANCE.getModEnabled()) {
                MinecraftServer server = world.getServer();
                server.execute(() -> {
                    for (ServerPlayerEntity playerEntity : server.getPlayerManager().getPlayerList()) {
                        // Apply hardcore mode and mark excessive mining
                        serverState.setIsHardcore(true);
                        serverState.setTooManyBlocks(true);

                        // Notify the client of the difficulty change
                        ServerPlayNetworking.send(playerEntity, new DifficultyPayload(true, true));

                        // Set player's health to zero (triggering death) and change gamemode to spectator
                        playerEntity.setHealth(0.0F);
                        playerEntity.changeGameMode(GameMode.SPECTATOR);
                    }
                });
            }
        });
    }
}
