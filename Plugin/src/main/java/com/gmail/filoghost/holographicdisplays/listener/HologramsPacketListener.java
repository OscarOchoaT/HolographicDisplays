package com.gmail.filoghost.holographicdisplays.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.ScheduledPacket;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.gmail.filoghost.holograms.api.HolographicDisplaysAPI;
import com.gmail.filoghost.holographicdisplays.HolographicDisplays;
import com.gmail.filoghost.holographicdisplays.bridge.protocollib.patch.*;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Iterator;
import java.util.List;

public class HologramsPacketListener
        extends PacketAdapter {
    private static final double OFFSET_HORSE = 58.25D;
    private static final double OFFSET_OTHER = 1.2D;
    private static final Byte ENTITY_INVISIBLE = (byte) 32;


    public HologramsPacketListener(Plugin plugin) {
        super(plugin, ListenerPriority.HIGHEST, PacketType.Play.Server.SPAWN_ENTITY_LIVING, PacketType.Play.Server.SPAWN_ENTITY, PacketType.Play.Server.ATTACH_ENTITY, PacketType.Play.Server.ENTITY_METADATA, PacketType.Play.Server.ENTITY_TELEPORT);
    }


    public void onPacketSending(PacketEvent event) {
        if (!HolographicDisplays.hasNewProtocol(event.getPlayer())) {
            return;
        }


        if (event.isCancelled()) {
            return;
        }

        if (event.getPacketType() == PacketType.Play.Server.SPAWN_ENTITY_LIVING) {

            WrapperPlayServerSpawnEntityLiving spawnLivingPacket = new WrapperPlayServerSpawnEntityLiving(event.getPacket());

            Entity entity = spawnLivingPacket.getEntity(event);
            if (entity == null) {
                return;
            }

            if (spawnLivingPacket.getType() == EntityType.HORSE && HolographicDisplaysAPI.isHologramEntity(entity)) {

                spawnLivingPacket.setType(30);

                List<WrappedWatchableObject> metadata = spawnLivingPacket.getMetadata().getWatchableObjects();

                if (metadata != null) {
                    fixIndexes(metadata, event.getPlayer());
                    metadata.add(new WrappedWatchableObject(0, ENTITY_INVISIBLE));
                    spawnLivingPacket.setMetadata(new WrappedDataWatcher(metadata));
                }

            }
        } else if (event.getPacketType() == PacketType.Play.Server.SPAWN_ENTITY) {

            WrapperPlayServerSpawnEntity spawnPacket = new WrapperPlayServerSpawnEntity(event.getPacket());

            Entity entity = spawnPacket.getEntity(event);
            if (entity == null) {
                return;
            }

            if (spawnPacket.getType() == 66 && HolographicDisplaysAPI.isHologramEntity(entity)) {
                spawnPacket.setType(78);

                WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata();
                metadataPacket.setEntityId(spawnPacket.getEntityID());

                List<WrappedWatchableObject> metadata = metadataPacket.getEntityMetadata();
                metadata.add(new WrappedWatchableObject(0, ENTITY_INVISIBLE));
                metadataPacket.setEntityMetadata(metadata);


                event.schedule(ScheduledPacket.fromSilent(metadataPacket.getHandle(), event.getPlayer()));
            }

        } else if (event.getPacketType() == PacketType.Play.Server.ENTITY_METADATA) {

            WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata(event.getPacket());

            Entity entity = metadataPacket.getEntity(event);
            if (entity == null) {
                return;
            }

            if (entity.getType() == EntityType.HORSE && HolographicDisplaysAPI.isHologramEntity(entity)) {
                List<WrappedWatchableObject> metadata = metadataPacket.getEntityMetadata();
                fixIndexes(metadata, event.getPlayer());
                metadata.add(new WrappedWatchableObject(0, ENTITY_INVISIBLE));
                metadataPacket.setEntityMetadata(metadata);
            }

        } else if (event.getPacketType() == PacketType.Play.Server.ATTACH_ENTITY) {

            WrapperPlayServerAttachEntity attachPacket = new WrapperPlayServerAttachEntity(event.getPacket());

            Entity vehicle = attachPacket.getVehicle(event);
            Entity passenger = attachPacket.getEntity(event);

            if (vehicle != null && passenger != null && HolographicDisplaysAPI.isHologramEntity(vehicle)) {
                Location loc = vehicle.getLocation();
                WrapperPlayServerEntityTeleport teleportPacket = new WrapperPlayServerEntityTeleport();
                teleportPacket.setEntityID(attachPacket.getVehicleId());
                teleportPacket.setX(loc.getX());
                teleportPacket.setZ(loc.getZ());

                if (passenger.getType() == EntityType.HORSE) {
                    teleportPacket.setY(loc.getY() - 58.25D);
                } else if (passenger.getType() == EntityType.DROPPED_ITEM || passenger.getType() == EntityType.SLIME) {
                    teleportPacket.setY(loc.getY() - 1.2D);
                }

                event.schedule(ScheduledPacket.fromSilent(teleportPacket.getHandle(), event.getPlayer()));
            }

        } else if (event.getPacketType() == PacketType.Play.Server.ENTITY_TELEPORT) {

            WrapperPlayServerEntityTeleport teleportPacket = new WrapperPlayServerEntityTeleport(event.getPacket());

            Entity entity = teleportPacket.getEntity(event);
            if (entity == null) {
                return;
            }

            if (entity.getType() == EntityType.WITHER_SKULL && HolographicDisplaysAPI.isHologramEntity(entity)) {

                Entity passenger = entity.getPassenger();
                if (passenger == null) {
                    return;
                }

                if (passenger.getType() == EntityType.DROPPED_ITEM || passenger.getType() == EntityType.SLIME) {
                    teleportPacket.setY(entity.getLocation().getY() - 1.2D);
                } else if (passenger.getType() == EntityType.HORSE) {
                    teleportPacket.setEntityID(entity.getPassenger().getEntityId());
                    teleportPacket.setY(entity.getLocation().getY() - 58.25D);
                }
            }
        }
    }

    private void fixIndexes(List<WrappedWatchableObject> metadata, Player player) {
        Iterator<WrappedWatchableObject> iter = metadata.iterator();


        while (iter.hasNext()) {
            WrappedWatchableObject current = iter.next();

            if (current.getIndex() == 2) {
                if (current.getValue() != null && current.getValue().getClass() == String.class) {
                    String customName = (String) current.getValue();

                    if (customName.contains("{player}") || customName.contains("{displayname}")) {
                        customName = customName.replace("{player}", player.getName()).replace("{displayname}", player.getDisplayName());
                        current.setValue(customName);
                    }
                }
                continue;
            }
            if (current.getIndex() != 3) {

                iter.remove();
            }
        }
    }
}