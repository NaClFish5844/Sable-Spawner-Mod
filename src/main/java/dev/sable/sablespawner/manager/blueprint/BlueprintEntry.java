package dev.sable.sablespawner.manager.blueprint;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.Objects;

public record BlueprintEntry(
        @Nullable Object object
) {
    public String getType() {
        if ( object instanceof Path ) { return "Path"; }
        if ( object != null ) { return object.getClass().getSimpleName(); }
        return null;
    }

    public boolean isPathRef() {
        return Objects.equals(this.getType(), "Path");
    }
    public boolean isBuffered() {
        return !this.isPathRef();
    }

}
