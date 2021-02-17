package com.gmail.filoghost.holographicdisplays.api.line;

import com.gmail.filoghost.holographicdisplays.api.handler.PickupHandler;

/**
 * A line of a Hologram that can be picked up.
 */
public interface CollectableLine extends HologramLine {

    /**
     * Returns the current PickupHandler of this line.
     *
     * @return the current PickupHandler, can be null.
     */
    public PickupHandler getPickupHandler();

    /**
     * Sets the PickupHandler for this line.
     *
     * @param pickupHandler the new PickupHandler, can be null.
     */
    public void setPickupHandler(PickupHandler pickupHandler);

}
