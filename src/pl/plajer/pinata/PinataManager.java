package pl.plajer.pinata;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.plajer.pinata.utils.Utils;

import java.util.*;
import java.util.logging.Level;

public class PinataManager {

    private List<String> pinataList = new ArrayList<>();
    private Map<String, HashSet<PinataItem>> pinataDrops = new HashMap<>();
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
    }

    public void loadPinatas() {
        ConfigurationSection pinata = plugin.getFileManager().getPinataConfig().getConfigurationSection("pinatas");
        if(pinata != null) {
            for(String key : pinata.getKeys(false)) {
                if(!plugin.getPinataManager().validatePinata(key)) {
                    System.out.println(Utils.colorRawMessage("Pinata.Validate.Fail").replaceAll("%name%", key));
                    continue;
                }
                System.out.println(Utils.colorRawMessage("Pinata.Validate.Success").replaceAll("%name%", key));
                pinataList.add(key);
                HashSet<PinataItem> items = new HashSet<>();
                for(String s : plugin.getFileManager().getPinataConfig().getStringList("pinatas." + key + ".drops")) {
                    PinataItem item = stringToItem(s);
                    items.add(item);
                }
                pinataDrops.put(key, items);
            }
        }
    }

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


    private boolean validatePinata(String pinata) {
        for(String error : validatorErrors.keySet()) {
            if(!plugin.getFileManager().getPinataConfig().isSet("pinatas." + pinata + error)) {
                plugin.getLogger().log(Level.SEVERE, Utils.colorRawMessage(validatorErrors.get(error)).replaceAll("%name%", pinata));
                return false;
            }
        }
        if(!plugin.getFileManager().getPinataConfig().isSet("pinatas." + pinata + ".cost") && plugin.isPluginEnabled("Vault")) {
            plugin.getLogger().log(Level.SEVERE, Utils.colorRawMessage("Validator.Vault-Abandoned").replaceAll("%name%", pinata));
            return false;
        }
        try {
            DyeColor.valueOf(plugin.getFileManager().getPinataConfig().get("pinatas." + pinata + ".color").toString().toUpperCase());
        } catch(Exception e) {
            plugin.getLogger().log(Level.SEVERE, Utils.colorRawMessage("Validator-Invalid-Color").replaceAll("%name%", pinata));
            return false;
        }
        try {
            EntityType e = EntityType.valueOf(plugin.getFileManager().getPinataConfig().get("pinatas." + pinata + ".mob-type").toString().toUpperCase());
            if(!e.isSpawnable()) {
                plugin.getLogger().log(Level.SEVERE, Utils.colorRawMessage("Validator-Invalid-Mob-Type").replaceAll("%name%", pinata));
                return false;
            }
        } catch(Exception e) {
            plugin.getLogger().log(Level.SEVERE, Utils.colorRawMessage("Validator-Invalid-Mob-Type").replaceAll("%name%", pinata));
            return false;
        }
        final List<String> drops = plugin.getFileManager().getPinataConfig().getStringList("pinatas." + pinata + ".drops");
        for(int i = 0; i < drops.size(); i++) {
            String itemvaild = drops.get(i);
            String[] partsvaild = itemvaild.split(";");
            if(!(partsvaild[0].equals("item") || partsvaild[0].equals("command") || partsvaild[0].equals("money") || partsvaild[0].equals("gun"))) {
                plugin.getLogger().log(Level.SEVERE, Utils.colorRawMessage("Validator.Invalid-Item-Type").replaceAll("%name%", pinata));
                return false;
            }
            if(partsvaild[0].equals("item")) {
                if(partsvaild.length < 5) {
                    plugin.getLogger().log(Level.SEVERE, Utils.colorRawMessage("Validator.Invalid-Configuration").replaceAll("%number%", String.valueOf(i + 1)).replaceAll("%name%", pinata));
                    return false;
                }
                if(Material.getMaterial(partsvaild[1].toUpperCase()) == null || partsvaild[2].equals("0")) {
                    plugin.getLogger().log(Level.SEVERE, Utils.colorRawMessage("Validator.Invalid-Item").replaceAll("%number%", String.valueOf(i + 1)).replaceAll("%name%", pinata));
                    return false;
                }
                if(Integer.parseInt(partsvaild[4]) == 0) {
                    plugin.getLogger().log(Level.WARNING, Utils.colorRawMessage("Validator.Invalid-Chance").replaceAll("%number%", String.valueOf(i + 1)).replaceAll("%name%", pinata));
                }
            } else if(partsvaild[0].equals("command")) {
                if(partsvaild.length < 3) {
                    plugin.getLogger().log(Level.SEVERE, Utils.colorRawMessage("Validator.Invalid-Configuration").replaceAll("%number%", String.valueOf(i + 1)).replaceAll("%name%", pinata));
                }
                if(Integer.parseInt(partsvaild[3]) == 0) {
                    plugin.getLogger().log(Level.WARNING, Utils.colorRawMessage("Validator.Invalid-Chance").replaceAll("%number%", String.valueOf(i + 1)).replaceAll("%name%", pinata));
                }
            } else if(partsvaild[0].equals("money")) {
                if(plugin.isPluginEnabled("Vault")) {
                    if(partsvaild.length < 3) {
                        plugin.getLogger().log(Level.SEVERE, Utils.colorRawMessage("Validator.Invalid-Configuration").replaceAll("%number%", String.valueOf(i + 1)).replaceAll("%name%", pinata));
                        return false;
                    }
                    if(Integer.parseInt(partsvaild[1]) == 0) {
                        plugin.getLogger().log(Level.WARNING, Utils.colorRawMessage("Validator.Invalid-Money-Drop").replaceAll("%number%", String.valueOf(i + 1)).replaceAll("%name%", pinata));
                    }
                    if(Integer.parseInt(partsvaild[3]) == 0) {
                        plugin.getLogger().log(Level.WARNING, Utils.colorRawMessage("Validator.Invalid-Chance").replaceAll("%number%", String.valueOf(i + 1)).replaceAll("%name%", pinata));
                    }
                } else {
                    plugin.getLogger().log(Level.SEVERE, Utils.colorRawMessage("Validator.Invalid-Item-Type").replaceAll("%number%", String.valueOf(i + 1)).replaceAll("%name%", pinata));
                    plugin.getLogger().log(Level.SEVERE, "Vault plugin not found!");
                    return false;
                }
            } else if(partsvaild[0].equals("gun")) {
                if(plugin.isPluginEnabled("CrackShot")) {
                    if(partsvaild.length < 3) {
                        plugin.getLogger().log(Level.SEVERE, Utils.colorRawMessage("Validator.Invalid-Configuration").replaceAll("%number%", String.valueOf(i + 1)).replaceAll("%name%", pinata));
                        return false;
                    }
                    if(Integer.parseInt(partsvaild[3]) == 0) {
                        plugin.getLogger().log(Level.WARNING, Utils.colorRawMessage("Validator.Invalid-Chance").replaceAll("%number%", String.valueOf(i + 1)).replaceAll("%name%", pinata));
                    }
                } else {
                    plugin.getLogger().log(Level.SEVERE, Utils.colorRawMessage("Validator.Invalid-Item-Type").replaceAll("%number%", String.valueOf(i + 1)).replaceAll("%name%", pinata));
                    plugin.getLogger().log(Level.SEVERE, "CrackShot plugin not found!");
                    return false;
                }
            }
        }
        return true;
    }

    public List<String> getPinataList() {
        return pinataList;
    }

    public Map<String, HashSet<PinataItem>> getPinataDrop() {
        return pinataDrops;
    }
}
