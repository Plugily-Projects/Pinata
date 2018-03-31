package pl.plajer.pinata.dao;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * @author Plajer
 * <p>
 * Created at 15.03.2018
 */
@Data
@AllArgsConstructor
public class PinataItem {

    private ItemStack item;
    private double dropChance;

}
