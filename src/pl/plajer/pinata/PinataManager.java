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
    private Map<Pinata, HashSet<PinataItem>> pinataDrops = new HashMap<>();
    private Map<String, String> validatorErrors = new HashMap<>();
    private Main plugin;

    PinataManager(Main plugin) {
        this.plugin = plugin;
        validatorErrors.put(".permission", "Validator.Invalid-Permission");
        validatorErrors.put(".timer", "Validator.Invalid-Timer");
        validatorErrors.put(".type", "Validator.Invalid-Type");
        validatorErrors.put(".crate-time", "Validator.Invalid-Crate-Time");
        validatorErrors.put(".health", "Validator.Invalid-Health");
        validatorErrors.put(".drop-type", "Validator.Invalid-Drop-Type");
        validatorErrors.put(".blindness-effect", "Validator.Invalid-Blindness");
        validatorErrors.put(".blindness-duration", "Validator.Invalid-Blindness");
        validatorErrors.put(".full-blindness-effect", "Validator.Invalid-Blindness");
    }

    public void loadPinatas3() {
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
    public void loadPinatas() {
        for(String key : ConfigurationManager.getConfig("pinatas").getConfigurationSection("pinatas").getKeys(false)) {
            if(!plugin.getPinataManager().validatePinata(key)) {
                System.out.println(Utils.colorMessage("Pinata.Validate.Fail").replaceAll("%name%", key));
                continue;
            }
            System.out.println(Utils.colorMessage("Pinata.Validate.Success").replaceAll("%name%", key));
            // pinataList.add(key);
            HashSet<PinataItem> items = new HashSet<>();
            for(String s : ConfigurationManager.getConfig("pinatas").getStringList("pinatas." + key + ".drops")) {
                PinataItem item = stringToItem(s);
                items.add(item);
            }
            // pinataDrops.put(key, items);
        }
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

    @Deprecated
    private boolean validatePinata(String pinata) {
        FileConfiguration config = ConfigurationManager.getConfig("pinatas");
        for(String error : validatorErrors.keySet()) {
            if(!config.isSet("pinatas." + pinata + error)) {
                plugin.getLogger().log(Level.SEVERE, Utils.colorMessage(validatorErrors.get(error)).replaceAll("%name%", pinata));
                return false;
            }
        }
        if(!config.isSet("pinatas." + pinata + ".cost") && plugin.isPluginEnabled("Vault")) {
            plugin.getLogger().log(Level.SEVERE, Utils.colorMessage("Validator.Vault-Abandoned").replaceAll("%name%", pinata));
            return false;
        }
        try {
            DyeColor.valueOf(config.get("pinatas." + pinata + ".color").toString().toUpperCase());
        } catch(Exception e) {
            plugin.getLogger().log(Level.SEVERE, Utils.colorMessage("Validator-Invalid-Color").replaceAll("%name%", pinata));
            return false;
        }
        try {
            EntityType e = EntityType.valueOf(config.get("pinatas." + pinata + ".mob-type").toString().toUpperCase());
            if(!e.isSpawnable()) {
                plugin.getLogger().log(Level.SEVERE, Utils.colorMessage("Validator-Invalid-Mob-Type").replaceAll("%name%", pinata));
                return false;
            }
        } catch(Exception e) {
            plugin.getLogger().log(Level.SEVERE, Utils.colorMessage("Validator-Invalid-Mob-Type").replaceAll("%name%", pinata));
            return false;
        }
        final List<String> drops = config.getStringList("pinatas." + pinata + ".drops");
        for(int i = 0; i < drops.size(); i++) {
            String itemvaild = drops.get(i);
            String[] partsvaild = itemvaild.split(";");
            if(!(partsvaild[0].equals("item") || partsvaild[0].equals("command") || partsvaild[0].equals("money") || partsvaild[0].equals("gun"))) {
                plugin.getLogger().log(Level.SEVERE, Utils.colorMessage("Validator.Invalid-Item-Type").replaceAll("%name%", pinata));
                return false;
            }
            if(partsvaild[0].equals("item")) {
                if(partsvaild.length < 5) {
                    plugin.getLogger().log(Level.SEVERE, Utils.colorMessage("Validator.Invalid-Configuration").replaceAll("%number%", String.valueOf(i + 1)).replaceAll("%name%", pinata));
                    return false;
                }
                if(Material.getMaterial(partsvaild[1].toUpperCase()) == null || partsvaild[2].equals("0")) {
                    plugin.getLogger().log(Level.SEVERE, Utils.colorMessage("Validator.Invalid-Item").replaceAll("%number%", String.valueOf(i + 1)).replaceAll("%name%", pinata));
                    return false;
                }
                if(Double.parseDouble(partsvaild[4]) == 0) {
                    plugin.getLogger().log(Level.WARNING, Utils.colorMessage("Validator.Invalid-Chance").replaceAll("%number%", String.valueOf(i + 1)).replaceAll("%name%", pinata));
                }
            } else if(partsvaild[0].equals("command")) {
                if(partsvaild.length < 3) {
                    plugin.getLogger().log(Level.SEVERE, Utils.colorMessage("Validator.Invalid-Configuration").replaceAll("%number%", String.valueOf(i + 1)).replaceAll("%name%", pinata));
                }
                if(Double.parseDouble(partsvaild[3]) == 0) {
                    plugin.getLogger().log(Level.WARNING, Utils.colorMessage("Validator.Invalid-Chance").replaceAll("%number%", String.valueOf(i + 1)).replaceAll("%name%", pinata));
                }
            }
        }
        return true;
    }

    public List<Pinata> getPinataList() {
        return pinataList;
    }

}
