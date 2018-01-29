package pl.plajer.pinata.pinataapi;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when pinata is created.
 */
public class PinataCreateEvent extends Event implements Cancellable {

    private final Player creator;
    private final Entity pinata;
    private final String name;
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean isCancelled;

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public boolean isCancelled() {
        return this.isCancelled;
    }

    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    public PinataCreateEvent(Player creator, Entity pinata, String pinataName) {
        this.creator = creator;
        this.pinata = pinata;
        this.name = pinataName;
    }

    /**
     * @return creator of pinata.
     */
    public Player getCreator() {
        return this.creator;
    }

    /**
     * @return event pinata.
     */
    public Entity getPinata() {
        return this.pinata;
    }

    /**
     * @return event pinata name.
     */
    public String getPinataName() {
        return this.name;
    }

}
