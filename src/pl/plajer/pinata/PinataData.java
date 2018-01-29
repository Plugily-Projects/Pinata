package pl.plajer.pinata;

import org.bukkit.Location;
import org.bukkit.entity.LeashHitch;
import org.bukkit.entity.Player;

/**
 * @author Plajer
 * <p>
 * Created at 28 lis 2017
 */
public class PinataData {

    private Player player;
    private Location fence;
    private LeashHitch leash;

    public PinataData(Player p, Location f, LeashHitch l) {
        player = p;
        fence = f;
        leash = l;
    }

    public Player getPlayer() {
        return player;
    }

    public Location getBuilder() {
        return fence;
    }

    public LeashHitch getLeash() {
        return leash;
    }

}
