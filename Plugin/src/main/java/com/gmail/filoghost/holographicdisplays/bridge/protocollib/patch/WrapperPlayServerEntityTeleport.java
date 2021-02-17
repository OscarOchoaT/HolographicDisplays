package com.gmail.filoghost.holographicdisplays.bridge.protocollib.patch;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.World;
import org.bukkit.entity.Entity;

public class WrapperPlayServerEntityTeleport
        extends AbstractPacket {
    public static final PacketType TYPE = PacketType.Play.Server.ENTITY_TELEPORT;

    public WrapperPlayServerEntityTeleport() {
        super(new PacketContainer(TYPE), TYPE);
        this.handle.getModifier().writeDefaults();
    }

    public WrapperPlayServerEntityTeleport(PacketContainer packet) {
        /*  36 */
        super(packet, TYPE);
    }

    public int getEntityID() {
        return this.handle.getIntegers().read(0);
    }

    public void setEntityID(int value) {
        this.handle.getIntegers().write(0, value);
    }

    public Entity getEntity(World world) {
        return this.handle.getEntityModifier(world).read(0);
    }

    public Entity getEntity(PacketEvent event) {
        return getEntity(event.getPlayer().getWorld());
    }

    public double getX() {
        return this.handle.getIntegers().read(1) / 32.0D;
    }

    public void setX(double value) {
        this.handle.getIntegers().write(1, (int) Math.floor(value * 32.0D));
    }

    public double getY() {
        return this.handle.getIntegers().read(2) / 32.0D;
    }

    public void setY(double value) {
        this.handle.getIntegers().write(2, (int) Math.floor(value * 32.0D));
    }

    public double getZ() {
        return this.handle.getIntegers().read(3) / 32.0D;
    }

    public void setZ(double value) {
        this.handle.getIntegers().write(3, (int) Math.floor(value * 32.0D));
    }

    public float getYaw() {
        return this.handle.getBytes().read(0) * 360.0F / 256.0F;
    }

    public void setYaw(float value) {
        this.handle.getBytes().write(0, (byte) (int) (value * 256.0F / 360.0F));
    }

    public float getPitch() {
        return this.handle.getBytes().read(1) * 360.0F / 256.0F;
    }

    public void setPitch(float value) {
        this.handle.getBytes().write(1, (byte) (int) (value * 256.0F / 360.0F));
    }
}