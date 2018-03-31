package pl.plajer.pinata.dao;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.entity.LeashHitch;
import org.bukkit.entity.Player;

/**
 * @author Plajer
 * <p>
 * Created at 28 lis 2017
 */
@Data
@AllArgsConstructor
public class PinataData {

    private Player player;
    private Location builder;
    private LeashHitch leash;

    public PinataData(Location builder, LeashHitch leash) {
        this.builder = builder;
        this.leash = leash;
    }

}
