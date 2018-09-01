package pl.plajer.pinata.pinata;

import org.bukkit.inventory.ItemStack;

/**
 * @author Plajer
 * <p>
 * Created at 15.03.2018
 */
public class PinataItem {

  private double dropChance;
  private ItemStack item;
  //private String command;

  public PinataItem(ItemStack item, double dropChance) {
    this.item = item;
    this.dropChance = dropChance;
  }

  public double getDropChance() {
    return dropChance;
  }

  public ItemStack getItem() {
    return item;
  }

    /*public String getCommand() {
        return command;
    }*/

    /*public void setCommand(String command) {
        this.command = command;
    }*/
}
