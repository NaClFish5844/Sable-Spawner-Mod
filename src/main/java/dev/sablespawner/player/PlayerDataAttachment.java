package dev.sablespawner.player;

import com.mojang.serialization.Codec;
import dev.sablespawner.SableSpawner;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class PlayerDataAttachment {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, SableSpawner.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Long>> OUT_PROTECTION_TIME =
            ATTACHMENT_TYPES.register("out_protection_time",
                    () -> AttachmentType.builder(PlayerDataAttachment::getGameTime)
                            .serialize(Codec.LONG)
                            .copyOnDeath()
                            .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> SCORE =
            ATTACHMENT_TYPES.register("score",
                    () -> AttachmentType.builder(() -> 0)
                            .serialize(Codec.INT)
                            .copyOnDeath()
                            .build());

    private PlayerDataAttachment() {}

    private static long getGameTime() { return SableSpawner.SERVER.overworld().getGameTime(); }
}
