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

    private static final HandlerList HANDLERS = new HandlerList();
    private final Entity pinata;
    private final String name;
    private Player creator = null;
    private boolean isCancelled;

    public PinataCreateEvent(Player creator, Entity pinata, String pinataName) {
        this.creator = creator;
        this.pinata = pinata;
        this.name = pinataName;
    }

    public PinataCreateEvent(Entity pinata, String pinataName) {
        this.pinata = pinata;
        this.name = pinataName;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public boolean isCancelled() {
        return this.isCancelled;
    }

    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    /**
     * @return creator of pinata.
     * @throws NullPointerException when pinata is created without player participation.
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
