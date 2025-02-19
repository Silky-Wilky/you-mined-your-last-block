package com.ymylb;

import com.ymylb.config.YouMinedYourLastBlockConfig;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

import java.util.Random;

/**
 * Handles the persistent storage of world-related values, including the number of blocks mined
 * and difficulty settings, within the Minecraft server.
 */
public class WorldRandomValueStorage extends PersistentState {

    private int blockMinedLimit;
    private int totalBlocksMined = 0;
    private boolean isHardcore;
    private boolean isDifficultyUnset;
    private boolean tooManyBlocks = false;

    private static Type<WorldRandomValueStorage> type = new Type<> (
            WorldRandomValueStorage::new,
            WorldRandomValueStorage::createFromNbt,
            null
    );

    /**
     * Initializes the storage with a randomized block mining limit based on the mod configuration.
     */
    public WorldRandomValueStorage() {
        this.blockMinedLimit = new Random().nextInt(YouMinedYourLastBlockConfig.getInstance().getBlockLimit());
        this.isDifficultyUnset = true;
    }

    /**
     * Writes the persistent state data to an NBT compound.
     *
     * @param nbt         The NBT compound to write data to.
     * @param registries  The registry lookup.
     * @return The updated NBT compound.
     */
    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        nbt.putInt("blockMindedLimit", blockMinedLimit);
        nbt.putInt("totalBlocksMined", totalBlocksMined);
        nbt.putBoolean("isDifficultyUnset", isDifficultyUnset);
        nbt.putBoolean("tooManyBlocks", tooManyBlocks);

        if (!isDifficultyUnset) {
            nbt.putBoolean("isHardcoreDeath", isHardcore);
        }

        return nbt;
    }

    /**
     * Creates a new WorldRandomValueStorage instance from NBT data.
     *
     * @param tag        The NBT compound containing saved data.
     * @param registries The registry lookup.
     * @return A new instance of WorldRandomValueStorage populated with saved data.
     */
    public static WorldRandomValueStorage createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registries) {
        WorldRandomValueStorage state = new WorldRandomValueStorage();
        state.blockMinedLimit = tag.getInt("blockMindedLimit");
        state.totalBlocksMined = tag.getInt("totalBlocksMined");
        state.isDifficultyUnset = tag.getBoolean("isDifficultyUnset");
        state.tooManyBlocks = tag.getBoolean("tooManyBlocks");

        if (!state.isDifficultyUnset) {
            state.isHardcore = tag.getBoolean("isHardcoreDeath");
        }

        return state;
    }

    /**
     * Retrieves the server's persistent state for block mining statistics and difficulty settings.
     *
     * @param server The Minecraft server instance.
     * @return The WorldRandomValueStorage instance for the server.
     */
    public static WorldRandomValueStorage getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(ServerWorld.OVERWORLD)
                .getPersistentStateManager();

        WorldRandomValueStorage state = persistentStateManager.getOrCreate(type, YouMinedYourLastBlock.MOD_ID);

        state.markDirty();

        return state;
    }

    /**
     * @return The total number of blocks mined.
     */
    public int getTotalBlocksMined() {
        return this.totalBlocksMined;
    }

    /**
     * Sets the total number of blocks mined and marks the state as dirty.
     *
     * @param totalBlocksMined The updated total block count.
     */
    public void setTotalBlocksMined(int totalBlocksMined) {
        this.totalBlocksMined = totalBlocksMined;
        this.markDirty();
    };

    /**
     * @return The block mining limit for the world.
     */
    public int getBlockMinedLimit() {
        return this.blockMinedLimit;
    }

    /**
     * @return Whether the world is in hardcore mode.
     */
    public boolean getIsHardcore() {
        return this.isHardcore;
    }

    /**
     * Sets the hardcore mode status and updates difficulty state.
     *
     * @param isHardcore Whether the game should be in hardcore mode.
     */
    public void setIsHardcore(boolean isHardcore) {
        this.isHardcore = isHardcore;
        this.isDifficultyUnset = false;
        this.markDirty();
    }

    /**
     * @return Whether the difficulty is unset.
     */
    public boolean getIsDifficultyUnset() {
        return this.isDifficultyUnset;
    }

    /**
     * @return Whether too many blocks have been mined.
     */
    public boolean getTooManyBlocks() {
        return this.tooManyBlocks;
    }

    /**
     * Sets the "too many blocks" flag.
     *
     * @param tooManyBlocks Whether too many blocks have been mined.
     */
    public void setTooManyBlocks(boolean tooManyBlocks) {
        this.tooManyBlocks = tooManyBlocks;
        this.markDirty();
    }
}
