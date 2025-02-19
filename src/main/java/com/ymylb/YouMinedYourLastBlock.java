package com.ymylb;

import com.ymylb.config.YouMinedYourLastBlockConfig;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The server-side entry point for the "You Mined Your Last Block" mod.
 * This class initializes server-side networking handles player block breaking logic to send to client
 */
public class YouMinedYourLastBlock implements ModInitializer {
	public static final String MOD_ID = "you-mined-your-last-block";
	public static final Identifier DIFFICULTY_SYNC = Identifier.of(MOD_ID, "difficulty_sync");
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/**
	 * Initializes the mod. This method is called when the mod is loaded and is used for registering events,
	 * syncing difficulty settings, and performing any necessary setup for the mod to function.
	 */
	@Override
	public void onInitialize() {
		LOGGER.info(MOD_ID + "has started");

		// load nbt configuration
		LOGGER.info(MOD_ID + "load nbt config");
		YouMinedYourLastBlockConfig.HANDLER.load();

		// register payloads
		LOGGER.info(MOD_ID + "register payload");
		PayloadTypeRegistry.playS2C().register(DifficultyPayload.ID, DifficultyPayload.CODEC);

		// check difficulty on server startup
		LOGGER.info(MOD_ID + "check initial difficulty state");
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			WorldRandomValueStorage storage = WorldRandomValueStorage.getServerState(server);
			if (storage.getIsDifficultyUnset()) {
				storage.setIsHardcore(server.getOverworld().getLevelProperties().isHardcore());
			}
		});

		// update persistent storage on player join events
		LOGGER.info(MOD_ID + "create join event for new players");
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			WorldRandomValueStorage storage = WorldRandomValueStorage.getServerState(server);
			boolean isHardcore = storage.getIsHardcore();
			boolean tooManyBlocks = storage.getTooManyBlocks();

			ServerPlayNetworking.send(handler.getPlayer(), new DifficultyPayload(isHardcore, tooManyBlocks));
		});

		// register class that performs block mining and consequences
		LOGGER.info(MOD_ID + "register block mining logic class");
		BreakBlockStatTracker.register();
	}
}