package dev.sable.sablespawner.player;

import com.mojang.serialization.Codec;
import dev.sable.sablespawner.SableSpawner;
import net.neoforged.neoforge.attachment.AttachmentType;

public class PlayerDataAttachment {
    public static final AttachmentType<Long> IN_PROTECTION_TIME = AttachmentType.builder( PlayerDataAttachment::getGameTime )
            .serialize(Codec.LONG)
            .build();
    public static final AttachmentType<Integer> SCORE = AttachmentType.builder(() -> 0)
            .serialize(Codec.INT)
            .build();

    private static long getGameTime() { return SableSpawner.SERVER.overworld().getGameTime(); }
}
