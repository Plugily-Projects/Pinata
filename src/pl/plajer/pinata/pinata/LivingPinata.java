package pl.plajer.pinata.pinata;

import org.bukkit.Location;
import org.bukkit.entity.LeashHitch;
import org.bukkit.entity.Player;

/**
 * @author Plajer
 * <p>
 * Created at 28 lis 2017
 */
public class LivingPinata {

    private Player player;
    private Location fenceLocation;
    private LeashHitch leash;
    private Pinata data;

    public LivingPinata(Player player, Location fenceLocation, LeashHitch leash, Pinata data) {
        this.player = player;
        this.fenceLocation = fenceLocation;
        this.leash = leash;
        this.data = data;
    }

    public LivingPinata(Location f, LeashHitch l, Pinata data) {
        fenceLocation = f;
        leash = l;
        this.data = data;
    }

    public Player getPlayer() {
        return player;
    }

    public Location getFence() {
        return fenceLocation;
    }

    public LeashHitch getLeash() {
        return leash;
    }

    public Pinata getData() {
        return data;
    }
}
