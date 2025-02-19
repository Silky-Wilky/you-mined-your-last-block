package com.ymylb;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import static com.ymylb.YouMinedYourLastBlock.DIFFICULTY_SYNC;

/**
 * Represents a custom network payload for syncing difficulty settings
 * and block mining limits between the client and server.
 */
public record DifficultyPayload(boolean difficulty, boolean tooManyBlocks) implements CustomPayload {
    public static final Id<DifficultyPayload> ID = new Id<>(DIFFICULTY_SYNC);
    public static final PacketCodec<RegistryByteBuf, DifficultyPayload> CODEC = new PacketCodec<>() {

        /**
         * Encodes the DifficultyPayload into a byte buffer.
         *
         * @param buf The buffer to write data to.
         * @param payload The payload instance containing difficulty settings.
         */
        @Override
        public void encode(RegistryByteBuf buf, DifficultyPayload payload) {
            buf.writeBoolean(payload.difficulty);
            buf.writeBoolean(payload.tooManyBlocks);
        }

        /**
         * Decodes a DifficultyPayload from a byte buffer.
         *
         * @param buf The buffer to read data from.
         * @return A new DifficultyPayload instance with extracted values.
         */
        @Override
        public DifficultyPayload decode(RegistryByteBuf buf) {
            boolean difficulty = buf.readBoolean();
            boolean tooManyBlocks = buf.readBoolean();
            return new DifficultyPayload(difficulty, tooManyBlocks);
        }
    };

    /**
     * Gets the unique identifier for this custom payload.
     *
     * @return The ID of this payload.
     */
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
