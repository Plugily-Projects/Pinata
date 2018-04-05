package pl.plajer.pinata;

import lombok.Getter;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.plajer.pinata.dao.PinataExtendedData;
import pl.plajer.pinata.dao.PinataItem;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PinataManager {

    @Getter
    private List<PinataExtendedData> pinataList = new ArrayList<>();
    private Main plugin;

    PinataManager(Main plugin) {
        this.plugin = plugin;
    }

    public void loadPinatas3() {
        ConfigurationSection section = plugin.getFileManager().getFile("pinatas").getConfigurationSection("Pinatas");
        FileConfiguration config = plugin.getFileManager().getFile("pinatas");
        for(String key : section.getKeys(false)) {
            String path = "Pinatas." + key + ".";
            int dropViewTime = config.getInt(path + "Timer");
            String permission = config.getString(path + "Permission");
            double price = config.getDouble(path + "Cost");
            PinataExtendedData.PinataType type = PinataExtendedData.PinataType.valueOf(config.getString(path + "Type").toUpperCase());
            int crateTime = config.getInt(path + "Crate-Time");
            double health = config.getDouble(path + "Health");
            EntityType entity = EntityType.valueOf(config.getString(path + "Mob-Type").toUpperCase());
            PinataExtendedData.DropType dropType = PinataExtendedData.DropType.valueOf(config.getString(path + "Drop-Type").toUpperCase());
            List<PinataItem> drops = new LinkedList<>();
            for(int i = 0; i < config.getList(path + "Drops").size(); i++) {
                ItemStack item = (ItemStack) config.getList(path + "Drops").get(i);
                if(item.getItemMeta() == null || item.getItemMeta().getLore() == null || item.getItemMeta().getLore().size() == 0 || !NumberUtils.isNumber(item.getItemMeta().getLore().get(item.getItemMeta().getLore().size() - 1)))
                    continue;
                double chance = Double.valueOf(item.getItemMeta().getLore().get(item.getItemMeta().getLore().size() - 1));
                ItemMeta meta = item.getItemMeta();
                List<String> lore = meta.getLore();
                lore.remove(lore.size() - 1);
                meta.setLore(lore);
                item.setItemMeta(meta);
                drops.add(new PinataItem(item, chance));
            }
            //TODO name != key
            pinataList.add(new PinataExtendedData(key, key, entity, type, dropType, health, crateTime, price, dropViewTime, permission, drops));
        }
    }
}
