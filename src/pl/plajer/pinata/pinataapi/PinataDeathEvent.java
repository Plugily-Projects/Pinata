package pl.plajer.pinata.pinataapi;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import pl.plajer.pinata.dao.PinataItem;

import java.util.List;

/**
 * Called when pinata is killed.
 */
public class PinataDeathEvent extends Event {

    private final Player killer;
    private final Entity pinata;
    private final String name;
    private final List<PinataItem> drops;
    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public PinataDeathEvent(Player killer, Entity pinata, String name, List<PinataItem> drops) {
        this.killer = killer;
        this.pinata = pinata;
        this.name = name;
        this.drops = drops;
    }

    /**
     * @return killer of pinata.
     */
    public Player getKiller() {
        return this.killer;
    }

    /**
     * @return killed pinata.
     */
    public Entity getPinata() {
        return this.pinata;
    }

    /**
     * @return killed pinata name.
     */
    public String getPinataName() {
        return this.name;
    }

    /**
     * Returns List of PinataItem objects.
     *
     * @return drops from pinata.
     * @since 2.1.3
     */
    public List<PinataItem> getDrops() {
        return this.drops;
    }
}
