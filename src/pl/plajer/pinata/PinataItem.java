package pl.plajer.pinata;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * @author Plajer
 * <p>
 * Created at 15.03.2018
 */
public class PinataItem {

    private ItemType itemType;
    private double dropChance;
    private ItemStack item;
    private String command;
    private double moneyValue;
    private String gunName;
    private String hologramName;
    private Material representedMaterial;
    private int amount;

    public PinataItem(ItemType itemType, double dropChance) {
        this.itemType = itemType;
        this.dropChance = dropChance;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public double getDropChance() {
        return dropChance;
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public double getMoneyValue() {
        return moneyValue;
    }

    public void setMoneyValue(double moneyValue) {
        this.moneyValue = moneyValue;
    }

    public String getGunName() {
        return gunName;
    }

    public void setGunName(String gunName) {
        this.gunName = gunName;
    }

    public String getHologramName() {
        return hologramName;
    }

    public void setHologramName(String hologramName) {
        this.hologramName = hologramName;
    }

    public Material getRepresentedMaterial() {
        return representedMaterial;
    }

    public void setRepresentedMaterial(Material representedMaterial) {
        this.representedMaterial = representedMaterial;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public enum ItemType {
        ITEM, COMMAND, GUN, MONEY;
    }
}
