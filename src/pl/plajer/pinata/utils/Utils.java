package pl.plajer.pinata.utils;

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
import org.bukkit.plugin.java.JavaPlugin;
import pl.plajer.pinata.ConfigurationManager;
import pl.plajer.pinata.Main;
import pl.plajer.pinata.pinata.Pinata;
import pl.plajer.pinata.pinataapi.PinataFactory;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    private static Main plugin = JavaPlugin.getPlugin(Main.class);

    public static String colorMessage(String message) {
        return ChatColor.translateAlternateColorCodes('&', ConfigurationManager.getLanguageMessage(message));
    }

    public static String colorRawMessage(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static int serializeInt(Integer i) {
        if((i % 9) == 0) return i;
        else return (int) ((Math.ceil(i / 9) * 9) + 9);
    }

    public static boolean createPinataAtPlayer(Player p, Location l, String pinataName) {
        Location loc = l.clone().add(0, 7, 0);
        LivingEntity entity = (LivingEntity) l.getWorld().spawnEntity(l.clone().add(0, 2, 0), EntityType.valueOf(ConfigurationManager.getConfig("pinatas").getString("pinatas." + pinataName + ".mob-type").toUpperCase()));
        entity.setMaxHealth(ConfigurationManager.getConfig("pinatas").getDouble("pinatas." + pinataName + ".health"));
        entity.setHealth(entity.getMaxHealth());
        return PinataFactory.createPinata(loc, p, entity, pinataName);
    }

    public static void createPinatasGUI(String name, Player p) {
        int rows = serializeInt(plugin.getPinataManager().getPinataList().size());
        Inventory pinatasMenu = Bukkit.createInventory(null, rows, Utils.colorMessage(name));
        for(int i = 0; i < plugin.getPinataManager().getPinataList().size(); i++) {
            Pinata pinata = plugin.getPinataManager().getPinataList().get(i);
            ItemStack item = new ItemStack(Material.WOOL, 1);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(Utils.colorRawMessage("&6") + pinata.getID());
            List<String> lore = new ArrayList<>();
            if(ConfigurationManager.getConfig("pinatas").get("pinatas." + pinata.getID() + ".type").equals("private")) {
                lore.add(colorMessage("Menus.List-Menu.Pinata-Types.Type-Private"));
            } else {
                lore.add(colorMessage("Menus.List-Menu.Pinata-Types.Type-Public"));
            }
            if(Integer.parseInt(ConfigurationManager.getConfig("pinatas").get("pinatas." + pinata.getID() + ".cost").toString()) == -1) {
                lore.add(colorMessage("Menus.List-Menu.Pinata-Cost-Not-For-Sale"));
            } else {
                String cost = colorMessage("Menus.List-Menu.Pinata-Cost");
                lore.add(cost.replaceAll("%money%", ConfigurationManager.getConfig("pinatas").get("pinatas." + pinata.getID() + ".cost").toString()) + "$");
                lore.add(colorMessage("Menus.List-Menu.Click-Selection.Right-Click"));
            }
            lore.add(colorMessage("Menus.List-Menu.Click-Selection.Left-Click"));
            meta.setLore(lore);
            item.setItemMeta(meta);
            pinatasMenu.setItem(i, item);
        }
        p.openInventory(pinatasMenu);
    }

}
