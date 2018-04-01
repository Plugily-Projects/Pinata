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
        ConfigurationSection section = plugin.getFileManager().getPinataConfig().getConfigurationSection("Pinatas");
        FileConfiguration config = plugin.getFileManager().getPinataConfig();
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

    /*@Deprecated
    public void loadPinatas() {
        ConfigurationSection pinata = plugin.getFileManager().getPinataConfig().getConfigurationSection("pinatas");
        if(pinata != null) {
            for(String key : pinata.getKeys(false)) {
                if(!plugin.getPinataManager().validatePinata(key)) {
                    System.out.println(Utils.colorFileMessage("Pinata.Validate.Fail").replaceAll("%name%", key));
                    continue;
                }
                System.out.println(Utils.colorFileMessage("Pinata.Validate.Success").replaceAll("%name%", key));
                pinataList.add(key);
                List<PinataItem> items = new LinkedList<>();
                for(String s : plugin.getFileManager().getPinataConfig().getStringList("pinatas." + key + ".drops")) {
                    PinataItem item = stringToItem(s);
                    items.add(item);
                }
                pinataDrop.put(key, items);
            }
        }
    }*/

    /*@Deprecated
    private PinataItem stringToItem(String string) {
        String[] splited = string.split(";");
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
                meta.setDisplayName(Utils.colorMessage(properties[0]));
                List<String> lore = new ArrayList<>();
                for(String s : Arrays.asList(properties)) {
                    lore.add(Utils.colorMessage(s));
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
                item.setHologramName(Utils.colorMessage(splited[2]));
                item.setRepresentedMaterial(Material.valueOf(plugin.getConfig().getString("command-item").toUpperCase()));
                item.setAmount(1);
                break;
            case GUN:
                item.setGunName(splited[1]);
                item.setHologramName(Utils.colorMessage(splited[2]));
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
        for(String error : validatorErrors.keySet()) {
            if(!plugin.getFileManager().getPinataConfig().isSet("pinatas." + pinata + error)) {
                plugin.getLogger().log(Level.SEVERE, Utils.colorFileMessage(validatorErrors.get(error)).replaceAll("%name%", pinata));
                return false;
            }
        }
        if(!plugin.getFileManager().getPinataConfig().isSet("pinatas." + pinata + ".cost") && plugin.isPluginEnabled("Vault")) {
            plugin.getLogger().log(Level.SEVERE, Utils.colorFileMessage("Validator.Vault-Abandoned").replaceAll("%name%", pinata));
            return false;
        }
        try {
            DyeColor.valueOf(plugin.getFileManager().getPinataConfig().get("pinatas." + pinata + ".color").toString().toUpperCase());
        } catch(Exception e) {
            plugin.getLogger().log(Level.SEVERE, Utils.colorFileMessage("Validator-Invalid-Color").replaceAll("%name%", pinata));
            return false;
        }
        try {
            EntityType e = EntityType.valueOf(plugin.getFileManager().getPinataConfig().get("pinatas." + pinata + ".mob-type").toString().toUpperCase());
            if(!e.isSpawnable()) {
                plugin.getLogger().log(Level.SEVERE, Utils.colorFileMessage("Validator-Invalid-Mob-Type").replaceAll("%name%", pinata));
                return false;
            }
        } catch(Exception e) {
            plugin.getLogger().log(Level.SEVERE, Utils.colorFileMessage("Validator-Invalid-Mob-Type").replaceAll("%name%", pinata));
            return false;
        }
        final List<String> drops = plugin.getFileManager().getPinataConfig().getStringList("pinatas." + pinata + ".drops");
        for(int i = 0; i < drops.size(); i++) {
            String itemvaild = drops.get(i);
            String[] partsvaild = itemvaild.split(";");
            if(!(partsvaild[0].equals("item") || partsvaild[0].equals("command") || partsvaild[0].equals("money") || partsvaild[0].equals("gun"))) {
                plugin.getLogger().log(Level.SEVERE, Utils.colorFileMessage("Validator.Invalid-Item-Type").replaceAll("%name%", pinata));
                return false;
            }
            if(partsvaild[0].equals("item")) {
                if(partsvaild.length < 5) {
                    plugin.getLogger().log(Level.SEVERE, Utils.colorFileMessage("Validator.Invalid-Configuration").replaceAll("%number%", String.valueOf(i + 1)).replaceAll("%name%", pinata));
                    return false;
                }
                if(Material.getMaterial(partsvaild[1].toUpperCase()) == null || partsvaild[2].equals("0")) {
                    plugin.getLogger().log(Level.SEVERE, Utils.colorFileMessage("Validator.Invalid-Item").replaceAll("%number%", String.valueOf(i + 1)).replaceAll("%name%", pinata));
                    return false;
                }
                if(Double.parseDouble(partsvaild[4]) == 0) {
                    plugin.getLogger().log(Level.WARNING, Utils.colorFileMessage("Validator.Invalid-Chance").replaceAll("%number%", String.valueOf(i + 1)).replaceAll("%name%", pinata));
                }
            } else if(partsvaild[0].equals("command")) {
                if(partsvaild.length < 3) {
                    plugin.getLogger().log(Level.SEVERE, Utils.colorFileMessage("Validator.Invalid-Configuration").replaceAll("%number%", String.valueOf(i + 1)).replaceAll("%name%", pinata));
                }
                if(Double.parseDouble(partsvaild[3]) == 0) {
                    plugin.getLogger().log(Level.WARNING, Utils.colorFileMessage("Validator.Invalid-Chance").replaceAll("%number%", String.valueOf(i + 1)).replaceAll("%name%", pinata));
                }
            } else if(partsvaild[0].equals("money")) {
                if(plugin.isPluginEnabled("Vault")) {
                    if(partsvaild.length < 3) {
                        plugin.getLogger().log(Level.SEVERE, Utils.colorFileMessage("Validator.Invalid-Configuration").replaceAll("%number%", String.valueOf(i + 1)).replaceAll("%name%", pinata));
                        return false;
                    }
                    if(Double.parseDouble(partsvaild[1]) == 0) {
                        plugin.getLogger().log(Level.WARNING, Utils.colorFileMessage("Validator.Invalid-Money-Drop").replaceAll("%number%", String.valueOf(i + 1)).replaceAll("%name%", pinata));
                    }
                    if(Double.parseDouble(partsvaild[3]) == 0) {
                        plugin.getLogger().log(Level.WARNING, Utils.colorFileMessage("Validator.Invalid-Chance").replaceAll("%number%", String.valueOf(i + 1)).replaceAll("%name%", pinata));
                    }
                } else {
                    plugin.getLogger().log(Level.SEVERE, Utils.colorFileMessage("Validator.Invalid-Item-Type").replaceAll("%number%", String.valueOf(i + 1)).replaceAll("%name%", pinata));
                    plugin.getLogger().log(Level.SEVERE, "Vault plugin not found!");
                    return false;
                }
            } else if(partsvaild[0].equals("gun")) {
                if(plugin.isPluginEnabled("CrackShot")) {
                    if(partsvaild.length < 3) {
                        plugin.getLogger().log(Level.SEVERE, Utils.colorFileMessage("Validator.Invalid-Configuration").replaceAll("%number%", String.valueOf(i + 1)).replaceAll("%name%", pinata));
                        return false;
                    }
                    if(Double.parseDouble(partsvaild[3]) == 0) {
                        plugin.getLogger().log(Level.WARNING, Utils.colorFileMessage("Validator.Invalid-Chance").replaceAll("%number%", String.valueOf(i + 1)).replaceAll("%name%", pinata));
                    }
                } else {
                    plugin.getLogger().log(Level.SEVERE, Utils.colorFileMessage("Validator.Invalid-Item-Type").replaceAll("%number%", String.valueOf(i + 1)).replaceAll("%name%", pinata));
                    plugin.getLogger().log(Level.SEVERE, "CrackShot plugin not found!");
                    return false;
                }
            }
        }
        return true;
    }*/
}
