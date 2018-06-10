package pl.plajer.pinata;

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

            //todo item reader
            PinataItem item = new PinataItem(PinataItem.ItemType.ITEM, 100.0);
            item.setRepresentedMaterial(Material.PAPER);
            item.setItem(new ItemStack(Material.PAPER, 1));
            item.setAmount(1);
            item.setHologramName("item");

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

            Pinata pinata = new Pinata(key, name, eType, color, pType, dType, health, cTime, price, viewTime, perm, bEnabled, bTime, fullBlind, Collections.singletonList(item));
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

    @Deprecated
    private PinataItem stringToItem(String string) {
        String[] splited = string.split(";");
        //- item;<name of item>;<amount>;<name of item and hologram>/<lore>/<next lore>...;<chance of drop>
        //- command;<command string>;<name of hologram>;<chance of drop>
        //- money;<amount of money>;<name of hologram>;<chance of drop>
        //- gun;<name of valid gun>;<name of hologram>;<change of drop>
        PinataItem item;
        if(splited[0].equalsIgnoreCase("item")) {
            item = new PinataItem(PinataItem.ItemType.valueOf(splited[0].toUpperCase()), Double.valueOf(splited[4]));
        } else {
            item = new PinataItem(PinataItem.ItemType.valueOf(splited[0].toUpperCase()), Double.valueOf(splited[3]));
        }
        switch(PinataItem.ItemType.valueOf(splited[0].toUpperCase())) {
            case ITEM:
                ItemStack stack = new ItemStack(Material.valueOf(splited[1].toUpperCase()), Integer.valueOf(splited[2]));
                ItemMeta meta = stack.getItemMeta();
                String[] properties = splited[3].split("/");
                meta.setDisplayName(Utils.colorRawMessage(properties[0]));
                List<String> lore = new ArrayList<>();
                for(String s : Arrays.asList(properties)) {
                    lore.add(Utils.colorRawMessage(s));
                }
                lore.remove(0);
                meta.setLore(lore);
                stack.setItemMeta(meta);
                item.setItem(stack);
                item.setHologramName(stack.getItemMeta().getDisplayName());
                item.setRepresentedMaterial(stack.getType());
                item.setAmount(stack.getAmount());
                break;
            case COMMAND:
                item.setCommand(splited[1]);
                item.setHologramName(Utils.colorRawMessage(splited[2]));
                item.setRepresentedMaterial(Material.valueOf(plugin.getConfig().getString("command-item").toUpperCase()));
                item.setAmount(1);
                break;
            case GUN:
                item.setGunName(splited[1]);
                item.setHologramName(Utils.colorRawMessage(splited[2]));
                item.setRepresentedMaterial(Material.valueOf(plugin.getConfig().getString("gun-item").toUpperCase()));
                item.setAmount(1);
                break;
            case MONEY:
                item.setMoneyValue(Double.valueOf(splited[1]));
                item.setHologramName(splited[2]);
                item.setRepresentedMaterial(Material.valueOf(plugin.getConfig().getString("money-item").toUpperCase()));
                item.setAmount(1);
                break;
            default:
                break;
        }
        return item;
    }

    public List<Pinata> getPinataList() {
        return pinataList;
    }

}
