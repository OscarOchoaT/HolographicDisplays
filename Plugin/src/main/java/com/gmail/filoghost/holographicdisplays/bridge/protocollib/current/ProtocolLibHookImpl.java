package com.gmail.filoghost.holographicdisplays.bridge.protocollib.current;

import com.comphenix.net.sf.cglib.proxy.Factory;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketAdapter.AdapterParameteters;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Serializer;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.bridge.protocollib.ProtocolLibHook;
import com.gmail.filoghost.holographicdisplays.bridge.protocollib.current.WrapperPlayServerSpawnEntity.ObjectTypes;
import com.gmail.filoghost.holographicdisplays.nms.interfaces.NMSManager;
import com.gmail.filoghost.holographicdisplays.nms.interfaces.entity.NMSArmorStand;
import com.gmail.filoghost.holographicdisplays.nms.interfaces.entity.NMSEntityBase;
import com.gmail.filoghost.holographicdisplays.object.CraftHologram;
import com.gmail.filoghost.holographicdisplays.object.line.*;
import com.gmail.filoghost.holographicdisplays.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * This is for the ProtocolLib versions containing the WrappedDataWatcher.WrappedDataWatcherObject class.
 * <p>
 * These versions are only used from 1.8, there is no need to handle 1.7 entities.
 */
public class ProtocolLibHookImpl implements ProtocolLibHook {

    private NMSManager nmsManager;

    private Serializer
            itemSerializer,
            intSerializer,
            byteSerializer,
            stringSerializer,
            booleanSerializer;

    private int itemstackMetadataWatcherIndex;

    @Override
    public boolean hook(Plugin plugin, NMSManager nmsManager) {

        String version = Bukkit.getPluginManager().getPlugin("ProtocolLib").getDescription().getVersion();
        if (version.startsWith("3.7-SNAPSHOT")) {
            Bukkit.getConsoleSender().sendMessage(
                    ChatColor.RED + "[Holographic Displays] Detected development version of ProtocolLib, support disabled. " +
                            "Related functions (the placeholders {player} {displayname} and the visibility API) will not work.\n" +
                            "The reason is that this version of ProtocolLib is unstable and partly broken. " +
                            "Please update ProtocolLib.");
            return false;
        }

        this.nmsManager = nmsManager;

        AdapterParameteters params = PacketAdapter
                .params()
                .plugin(plugin)
                .types(PacketType.Play.Server.SPAWN_ENTITY_LIVING,
                        PacketType.Play.Server.SPAWN_ENTITY,
                        PacketType.Play.Server.ENTITY_METADATA)
                .serverSide()
                .listenerPriority(ListenerPriority.NORMAL);

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(params) {

            @Override
            public void onPacketSending(PacketEvent event) {

                PacketContainer packet = event.getPacket();

                if (event.getPlayer() instanceof Factory) {
                    return; // Ignore temporary players (reference: https://github.com/dmulloy2/ProtocolLib/issues/349)
                }

                // Spawn entity packet
                if (packet.getType() == PacketType.Play.Server.SPAWN_ENTITY_LIVING) {

                    WrapperPlayServerSpawnEntityLiving spawnEntityPacket = new WrapperPlayServerSpawnEntityLiving(packet);
                    Entity entity = spawnEntityPacket.getEntity(event);

                    if (entity == null || !isHologramType(entity.getType())) {
                        return;
                    }

                    Hologram hologram = getHologram(entity);
                    if (hologram == null) {
                        return;
                    }

                    Player player = event.getPlayer();
                    if (!hologram.getVisibilityManager().isVisibleTo(player)) {
                        event.setCancelled(true);
                        return;
                    }

                    WrappedWatchableObject customNameWatchableObject = spawnEntityPacket.getMetadata().getWatchableObject(2);
                    if (customNameWatchableObject == null || !(customNameWatchableObject.getValue() instanceof String)) {
                        return;
                    }

                    String customName = (String) customNameWatchableObject.getValue();
                    if (customName.contains("{player}") || customName.contains("{displayname}")) {
                        customNameWatchableObject.setValue(customName.replace("{player}", player.getName()).replace("{displayname}", player.getDisplayName()));
                    }

                } else if (packet.getType() == PacketType.Play.Server.SPAWN_ENTITY) {

                    WrapperPlayServerSpawnEntity spawnEntityPacket = new WrapperPlayServerSpawnEntity(packet);
                    Entity entity = spawnEntityPacket.getEntity(event);

                    if (entity == null) {
                        return;
                    }

                    if (!isHologramType(entity.getType())) {
                        return;
                    }

                    Hologram hologram = getHologram(entity);
                    if (hologram == null) {
                        return;
                    }

                    Player player = event.getPlayer();
                    if (!hologram.getVisibilityManager().isVisibleTo(player)) {
                        event.setCancelled(true);
                        return;
                    }

                } else if (packet.getType() == PacketType.Play.Server.ENTITY_METADATA) {

                    WrapperPlayServerEntityMetadata entityMetadataPacket = new WrapperPlayServerEntityMetadata(packet);
                    Entity entity = entityMetadataPacket.getEntity(event);

                    if (entity == null) {
                        return;
                    }

                    if (!isHologramType(entity.getType())) {
                        return;
                    }

                    Hologram hologram = getHologram(entity);
                    if (hologram == null) {
                        return;
                    }

                    Player player = event.getPlayer();
                    if (!hologram.getVisibilityManager().isVisibleTo(player)) {
                        event.setCancelled(true);
                        return;
                    }

                    List<WrappedWatchableObject> dataWatcherValues = entityMetadataPacket.getEntityMetadata();
                    for (int i = 0; i < dataWatcherValues.size(); i++) {

                        WrappedWatchableObject watchableObject = dataWatcherValues.get(i);
                        if (watchableObject.getIndex() == 2) { // Custom name index

                            Object customNameObject = watchableObject.getValue();
                            if (!(customNameObject instanceof String)) {
                                return;
                            }

                            String customName = (String) customNameObject;
                            if (customName.contains("{player}") || customName.contains("{displayname}")) {
                                String replacement = customName.replace("{player}", player.getName()).replace("{displayname}", player.getDisplayName());

                                WrappedWatchableObject newWatchableObject;

                                newWatchableObject = new WrappedWatchableObject(watchableObject.getIndex(), replacement);


                                dataWatcherValues.set(i, newWatchableObject);
                                PacketContainer clone = packet.shallowClone();
                                clone.getWatchableCollectionModifier().write(0, dataWatcherValues);
                                event.setPacket(clone);
                                return;
                            }
                        }
                    }
                }
            }
        });

        return true;
    }


    @Override
    public void sendDestroyEntitiesPacket(Player player, CraftHologram hologram) {
        List<Integer> ids = Utils.newList();
        for (CraftHologramLine line : hologram.getLinesUnsafe()) {
            if (line.isSpawned()) {
                for (int id : line.getEntitiesIDs()) {
                    ids.add(id);
                }
            }
        }

        if (!ids.isEmpty()) {
            WrapperPlayServerEntityDestroy packet = new WrapperPlayServerEntityDestroy();
            packet.setEntities(ids);
            packet.sendPacket(player);
        }
    }


    @Override
    public void sendCreateEntitiesPacket(Player player, CraftHologram hologram) {
        for (CraftHologramLine line : hologram.getLinesUnsafe()) {
            if (line.isSpawned()) {

                if (line instanceof CraftTextLine) {
                    CraftTextLine textLine = (CraftTextLine) line;

                    if (textLine.isSpawned()) {
                        sendSpawnArmorStandPacket(player, (NMSArmorStand) textLine.getNmsNameble());
                    }

                } else if (line instanceof CraftItemLine) {
                    CraftItemLine itemLine = (CraftItemLine) line;

                    if (itemLine.isSpawned()) {
                        AbstractPacket itemPacket = new WrapperPlayServerSpawnEntity(itemLine.getNmsItem().getBukkitEntityNMS(), ObjectTypes.ITEM_STACK, 1);
                        itemPacket.sendPacket(player);

                        sendSpawnArmorStandPacket(player, (NMSArmorStand) itemLine.getNmsVehicle());
                        sendVehicleAttachPacket(player, itemLine.getNmsVehicle().getIdNMS(), itemLine.getNmsItem().getIdNMS());

                        WrapperPlayServerEntityMetadata itemDataPacket = new WrapperPlayServerEntityMetadata();
                        WrappedDataWatcher dataWatcher = new WrappedDataWatcher();

                        dataWatcher.setObject(itemstackMetadataWatcherIndex, itemLine.getNmsItem().getRawItemStack());
                        dataWatcher.setObject(1, 300);
                        dataWatcher.setObject(0, (byte) 0);


                        itemDataPacket.setEntityMetadata(dataWatcher.getWatchableObjects());
                        itemDataPacket.setEntityId(itemLine.getNmsItem().getIdNMS());
                        itemDataPacket.sendPacket(player);
                    }
                }

                // Unsafe cast, however both CraftTextLine and CraftItemLine are touchable.
                CraftTouchableLine touchableLine = (CraftTouchableLine) line;

                if (touchableLine.isSpawned() && touchableLine.getTouchSlime() != null) {

                    CraftTouchSlimeLine touchSlime = touchableLine.getTouchSlime();

                    if (touchSlime.isSpawned()) {
                        sendSpawnArmorStandPacket(player, (NMSArmorStand) touchSlime.getNmsVehicle());

                        AbstractPacket slimePacket = new WrapperPlayServerSpawnEntityLiving(touchSlime.getNmsSlime().getBukkitEntityNMS());
                        slimePacket.sendPacket(player);

                        sendVehicleAttachPacket(player, touchSlime.getNmsVehicle().getIdNMS(), touchSlime.getNmsSlime().getIdNMS());
                    }
                }
            }
        }
    }


    private void sendSpawnArmorStandPacket(Player receiver, NMSArmorStand armorStand) {

        WrapperPlayServerSpawnEntityLiving spawnPacket = new WrapperPlayServerSpawnEntityLiving(armorStand.getBukkitEntityNMS());
        spawnPacket.sendPacket(receiver);

    }


    private void sendVehicleAttachPacket(Player receiver, int vehicleId, int passengerId) {

        WrapperPlayServerAttachEntity attachPacket = new WrapperPlayServerAttachEntity();
        attachPacket.setVehicleId(vehicleId);
        attachPacket.setEntityId(passengerId);
        attachPacket.sendPacket(receiver);

    }


    private boolean isHologramType(EntityType type) {
        return type == EntityType.WITHER_SKULL || type == EntityType.DROPPED_ITEM || type == EntityType.SLIME;
    }


    private Hologram getHologram(Entity bukkitEntity) {
        NMSEntityBase entity = nmsManager.getNMSEntityBase(bukkitEntity);
        if (entity != null) {
            return entity.getHologramLine().getParent();
        }

        return null;
    }
}
