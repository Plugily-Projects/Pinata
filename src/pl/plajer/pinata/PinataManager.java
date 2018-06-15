package pl.plajer.pinata;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.plajer.pinata.pinata.Pinata;
import pl.plajer.pinata.pinata.PinataItem;
import pl.plajer.pinata.utils.Utils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class PinataManager {

    private List<Pinata> pinataList = new ArrayList<>();
    private Main plugin;

    PinataManager(Main plugin) {
        this.plugin = plugin;
    }

    public void loadPinatas() {
        FileConfiguration config = ConfigurationManager.getConfig("pinata_storage");
        for(String key : config.getConfigurationSection("storage").getKeys(false)) {
            String accessKey = "storage." + key + ".";

            List<PinataItem> pinataItems = new ArrayList<>();
            for(int i = 0; i < config.getList("drops", new ArrayList<>()).size(); i++) {
                if(config.getList("drops").get(i) == null) continue;
                ItemStack item = (ItemStack) config.getList("drops").get(i);
                ItemMeta meta = item.getItemMeta();
                if(meta == null || !meta.hasLore()){
                    PinataItem pinataItem = new PinataItem(item, 100.0);
                    Bukkit.getLogger().warning("Item " + item.getType() + " from pinata " + key + " hasn't got chance set! Using 100% by default!");
                    pinataItems.add(pinataItem);
                }
                boolean found = false;
                for(String lore : item.getItemMeta().getLore()){
                    if(lore.contains("#!Chance:")){
                        found = true;
                        pinataItems.add(new PinataItem(item, Double.parseDouble(lore.replace("#!Chance:", ""))));
                        break;
                    }
                }
                if(found) continue;
                pinataItems.add(new PinataItem(item, 100.0));
                Bukkit.getLogger().warning("Item " + item.getType() + " from pinata " + key + " hasn't got chance set! Using 100% by default!");
            }

            String name = config.getString(accessKey + "display-name");
            EntityType eType = EntityType.valueOf(config.getString(accessKey + "mob-entity-type"));
            DyeColor color = DyeColor.valueOf(config.getString(accessKey + "color"));
            Pinata.PinataType pType = Pinata.PinataType.valueOf(config.getString(accessKey + "pinata-access-type"));
            Pinata.DropType dType = Pinata.DropType.valueOf(config.getString(accessKey + "items-drop-type"));
            double health = config.getDouble(accessKey + "health-amount");
            int cTime = config.getInt(accessKey + "crate-display-time-alive");
            double price = config.getDouble(accessKey + "crate-buy-cost");
            int viewTime = config.getInt(accessKey + "timer-display");
            String perm = config.getString(accessKey + "permission-string");
            boolean bEnabled = config.getBoolean(accessKey + "blindness-activated");
            int bTime = config.getInt(accessKey + "blindness-duration");
            boolean fullBlind = config.getBoolean(accessKey + "full-blindness-activated");

            Pinata pinata = new Pinata(key, name, eType, color, pType, dType, health, cTime, price, viewTime, perm, bEnabled, bTime, fullBlind, pinataItems);
            pinataList.add(pinata);
        }
    }

    @Nullable
    public Pinata getPinataByName(String name) {
        for(Pinata pinata : pinataList) {
            if(pinata.getID().equals(name)) return pinata;
        }
        return null;
    }

    public List<Pinata> getPinataList() {
        return pinataList;
    }

}
