package pl.plajer.pinata.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.plajer.pinata.Main;
import pl.plajer.pinata.dao.Pinata;
import pl.plajer.pinata.pinataapi.PinataFactory;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static String colorFileMessage(String message) {
        return ChatColor.translateAlternateColorCodes('&', Main.getInstance().getFileManager().getLanguageMessage(message));
    }

    public static String colorMessage(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static int serializeInt(Integer i) {
        if((i % 9) == 0) return i;
        else return (int) ((Math.ceil(i / 9) * 9) + 9);
    }

    public static boolean createPinataAtPlayer(Player p, Location l, Pinata pinata) {
        Location loc = l.clone().add(0, 7, 0);
        LivingEntity entity = (LivingEntity) l.getWorld().spawnEntity(l.clone().add(0, 2, 0), pinata.getEntityType());
        entity.setMaxHealth(pinata.getHealth());
        entity.setHealth(entity.getMaxHealth());
        return PinataFactory.createPinata(loc, p, entity, pinata.getID());
    }

    public static void createPinatasGUI(String name, Player p) {
        int rows = serializeInt(Main.getInstance().getPinataManager().getPinataList().size());
        Inventory pinatasMenu = Bukkit.createInventory(null, rows, Utils.colorFileMessage(name));
        for(Pinata pinata : Main.getInstance().getPinataManager().getPinataList()){
            ItemStack item = new ItemStack(Material.WOOL, 1);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(Utils.colorMessage("&6") + pinata.getID());
            List<String> lore = new ArrayList<>();
            if(pinata.getPinataType() == Pinata.PinataType.PRIVATE) lore.add(colorFileMessage("Menus.List-Menu.Pinata-Types.Type-Private"));
            else lore.add(colorFileMessage("Menus.List-Menu.Pinata-Types.Type-Public"));
            if(pinata.getPrice() == -1){
                lore.add(colorFileMessage("Menus.List-Menu.Pinata-Cost-Not-For-Sale"));
            } else{
                String cost = colorFileMessage("Menus.List-Menu.Pinata-Cost");
                lore.add(cost.replaceAll("%money%", String.valueOf(pinata.getPrice())) + "$");
                lore.add(colorFileMessage("Menus.List-Menu.Click-Selection.Right-Click"));
            }
            lore.add(colorFileMessage("Menus.List-Menu.Click-Selection.Left-Click"));
            meta.setLore(lore);
            item.setItemMeta(meta);
            pinatasMenu.addItem(item);
        }
        p.openInventory(pinatasMenu);
    }

}
