package pl.plajer.pinata.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.plajer.pinata.Main;
import pl.plajer.pinata.pinataapi.PinataFactory;

public class Utils {

    public static String colorRawMessage(String message) {
        return ChatColor.translateAlternateColorCodes('&', Main.getInstance().getFileManager().getMessagesConfig().getString(message));
    }

    public static String colorMessage(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static boolean createPinataAtPlayer(Player p, Location l, String pinataName) {
        Location loc = l.clone().add(0, 7, 0);
        LivingEntity entity = (LivingEntity) l.getWorld().spawnEntity(l.clone().add(0, 2, 0), EntityType.valueOf(Main.getInstance().getFileManager().getPinataConfig().getString("pinatas." + pinataName + ".mob-type").toUpperCase()));
        entity.setMaxHealth(Main.getInstance().getFileManager().getPinataConfig().getDouble("pinatas." + pinataName + ".health"));
        entity.setHealth(entity.getMaxHealth());
        return PinataFactory.createPinata(loc, p, entity, pinataName);
    }

    public static void createPinatasGUI(String name, Player p) {
        int rows = 1;
        float trick = Main.getInstance().getPinataManager().getPinataList().size() / 9;
        if(!((int) trick % 9 == 0)) {
            rows++;
        }
        Inventory pinatasMenu = Bukkit.createInventory(null, rows * 9, Utils.colorRawMessage(name));
        for(int i = 0; i < Main.getInstance().getPinataManager().getPinataList().size(); i++) {
            String pinata = Main.getInstance().getPinataManager().getPinataList().get(i);
            ItemStack item = new ItemStack(Material.WOOL, 1);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(Utils.colorMessage("&6") + pinata);
            List<String> lore = new ArrayList<>();
            if(Main.getInstance().getFileManager().getPinataConfig().get("pinatas." + pinata + ".type").equals("private")) {
                lore.add(colorRawMessage("Menus.List-Menu.Pinata-Types.Type-Private"));
            } else {
                lore.add(colorRawMessage("Menus.List-Menu.Pinata-Types.Type-Public"));
            }
            if(Integer.parseInt(Main.getInstance().getFileManager().getPinataConfig().get("pinatas." + pinata + ".cost").toString()) == -1) {
                lore.add(colorRawMessage("Menus.List-Menu.Pinata-Cost-Not-For-Sale"));
            } else {
                String cost = colorRawMessage("Menus.List-Menu.Pinata-Cost");
                lore.add(cost.replaceAll("%money%", Main.getInstance().getFileManager().getPinataConfig().get("pinatas." + pinata + ".cost").toString()) + "$");
                lore.add(colorRawMessage("Menus.List-Menu.Click-Selection.Right-Click"));
            }
            lore.add(colorRawMessage("Menus.List-Menu.Click-Selection.Left-Click"));
            meta.setLore(lore);
            item.setItemMeta(meta);
            pinatasMenu.setItem(i, item);
        }
        p.openInventory(pinatasMenu);
    }

}
