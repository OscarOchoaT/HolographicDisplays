package com.gmail.filoghost.holographicdisplays.bridge.protocollib.patch;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.List;

public class WrapperPlayServerEntityMetadata
        extends AbstractPacket {
    public static final PacketType TYPE = PacketType.Play.Server.ENTITY_METADATA;

    public WrapperPlayServerEntityMetadata() {
        super(new PacketContainer(TYPE), TYPE);
        this.handle.getModifier().writeDefaults();
    }

    public WrapperPlayServerEntityMetadata(PacketContainer packet) {
        /* 40 */
        super(packet, TYPE);
    }

    public int getEntityId() {
        return this.handle.getIntegers().read(0);
    }

    public void setEntityId(int value) {
        this.handle.getIntegers().write(0, value);
    }

    public Entity getEntity(World world) {
        return this.handle.getEntityModifier(world).read(0);
    }

    public Entity getEntity(PacketEvent event) {
        return getEntity(event.getPlayer().getWorld());
    }

    public List<WrappedWatchableObject> getEntityMetadata() {
        return this.handle.getWatchableCollectionModifier().read(0);
    }

    public void setEntityMetadata(List<WrappedWatchableObject> value) {
        this.handle.getWatchableCollectionModifier().write(0, value);
    }
}