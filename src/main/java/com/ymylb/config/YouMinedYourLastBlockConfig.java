package com.ymylb.config;

import com.google.gson.GsonBuilder;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

/**
 * Configuration class for the "You Mined Your Last Block" mod.
 * This class handles loading and saving mod configuration settings.
 */
public class YouMinedYourLastBlockConfig {
	/**
	 * Singleton instance of the configuration class.
	 */
	public static final YouMinedYourLastBlockConfig INSTANCE = new YouMinedYourLastBlockConfig();

	/**
	 * Handler for managing the configuration.
	 */
	public static ConfigClassHandler<YouMinedYourLastBlockConfig> HANDLER = ConfigClassHandler.createBuilder(YouMinedYourLastBlockConfig.class)
			.id(Identifier.of("ymylb", "config"))
					.serializer(config -> GsonConfigSerializerBuilder.create(config)
							.setPath(FabricLoader.getInstance().getConfigDir().resolve("ymylb_config.json"))
							.appendGsonBuilder(GsonBuilder::setPrettyPrinting)
							.build())
					.build();

	/**
	 * Whether the mod is enabled.
	 */
	@SerialEntry
	private Boolean modEnabled = true;

	/**
	 * The random number upper bound for generating mined blocks limit.
	 */
	@SerialEntry
	private int blockLimit = 1000;

	/**
	 * Retrieves the singleton instance of the configuration class.
	 *
	 * @return The configuration instance.
	 */
	public static YouMinedYourLastBlockConfig getInstance() {
		return INSTANCE;
	}

	/**
	 * Gets whether the mod is enabled.
	 *
	 * @return {@code true} if the mod is enabled, {@code false} otherwise.
	 */
	public boolean getModEnabled() {
		return HANDLER.instance().modEnabled;
	}

	/**
	 * Sets whether the mod is enabled and saves the configuration.
	 *
	 * @param modEnabled {@code true} to enable the mod, {@code false} to disable it.
	 */
	public void setModEnabled(Boolean modEnabled) {
		HANDLER.instance().modEnabled = modEnabled;
		HANDLER.save();
	}

	/**
	 * Gets the block limit.
	 *
	 * @return The block limit.
	 */
	public int getBlockLimit() {
		return HANDLER.instance().blockLimit;
	}

	/**
	 * Sets the block limit and saves the configuration.
	 *
	 * @param blockLimit The new block limit.
	 */
	public void setBlockLimit(int blockLimit) {
		HANDLER.instance().blockLimit = blockLimit;
		HANDLER.save();
	}
}