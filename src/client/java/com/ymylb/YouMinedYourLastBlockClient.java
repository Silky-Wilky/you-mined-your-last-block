package com.ymylb;

import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

/**
 * The client-side entry point for the "You Mined Your Last Block" mod.
 * This class initializes client-side networking and handles difficulty-related payloads.
 */
public class YouMinedYourLastBlockClient implements ClientModInitializer {

	// Stores player-specific data, including death conditions
	public static PlayerData playerData = new PlayerData();

	/**
	 * Initializes the client-side components of the mod.
	 * Registers network payloads and sets up a global receiver
	 * to handle difficulty updates from the server.
	 */
	@Override
	public void onInitializeClient() {
		PayloadTypeRegistry.playC2S().register(DifficultyPayload.ID, DifficultyPayload.CODEC);

		ClientPlayNetworking.registerGlobalReceiver(DifficultyPayload.ID, (payload, context) -> {
			context.client().execute(() -> {
				playerData.setHardcoreDeath(payload.difficulty());
				playerData.setTooManyBlocks(payload.tooManyBlocks());
			});
		});
	}
}