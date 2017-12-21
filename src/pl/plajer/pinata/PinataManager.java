package pl.plajer.pinata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

public class PinataManager {

	private List<String> pinatalist = new ArrayList<>();
	private Map<String, ArrayList<String>> pinatadrops = new HashMap<>();
	private Main plugin;

	public PinataManager(Main plugin) {
		this.plugin = plugin;
	}

	public void loadPinatas(){
		ConfigurationSection pinata = plugin.getFileManager().getPinataConfig().getConfigurationSection("pinatas");
		if (pinata != null) {
			for(String key : pinata.getKeys(false)) {
				if(!plugin.getPinataManager().validatePinata(key)){
					System.out.println(Utils.colorRawMessage("Pinata.Validate.Fail").replaceAll("%name%", key));
					continue;
				}
				System.out.println(Utils.colorRawMessage("Pinata.Validate.Success").replaceAll("%name%", key));
				pinatalist.add(key);
				ArrayList<String> list = new ArrayList<>();
				final List<String> drops = plugin.getFileManager().getPinataConfig().getStringList("pinatas." + key + ".drops");
				for(int i = 0; i < drops.size(); i++)
					list.add(drops.get(i));
				pinatadrops.put(key, list);
			}
		}
	}

	public boolean validatePinata(String pinata){
		if(!plugin.getFileManager().getPinataConfig().isSet("pinatas." + pinata + ".permission")){
			plugin.getLogger().log(Level.SEVERE, Utils.colorRawMessage("Validator.Invalid-Permission").replaceAll("%name%", pinata));
			return false;
		}
		if(!plugin.getFileManager().getPinataConfig().isSet("pinatas." + pinata + ".cost") && plugin.getVaultUse()){
			plugin.getLogger().log(Level.SEVERE, Utils.colorRawMessage("Validator.Vault-Abandoned").replaceAll("%name%", pinata));
			return false;
		}
		if(!plugin.getFileManager().getPinataConfig().isSet("pinatas." + pinata + ".timer")){
			plugin.getLogger().log(Level.SEVERE, Utils.colorRawMessage("Validator.Invalid-Timer").replaceAll("%name%", pinata));
			return false;
		}
		if(!plugin.getFileManager().getPinataConfig().isSet("pinatas." + pinata + ".type")){
			plugin.getLogger().log(Level.SEVERE, Utils.colorRawMessage("Validator.Invalid-Type").replaceAll("%name%", pinata));
			return false;
		}
		if(!plugin.getFileManager().getPinataConfig().isSet("pinatas." + pinata + ".crate-time")){
			plugin.getLogger().log(Level.SEVERE, Utils.colorRawMessage("Validator.Invalid-Crate-Time").replaceAll("%name%", pinata));
			return false;
		}
		try{
			DyeColor.valueOf(plugin.getFileManager().getPinataConfig().get("pinatas." + pinata + ".color").toString().toUpperCase());
		} catch (Exception e){
			plugin.getLogger().log(Level.SEVERE, Utils.colorRawMessage("Validator-Invalid-Color").replaceAll("%name%", pinata));
			return false;
		}
		final List<String> drops = plugin.getFileManager().getPinataConfig().getStringList("pinatas." + pinata + ".drops");
		for(int i = 0; i < drops.size(); i++){
			String itemvaild = drops.get(i);
			String[] partsvaild = itemvaild.split(";");
			if(!(partsvaild[0].equals("item") || partsvaild[0].equals("command") || partsvaild[0].equals("money") || partsvaild[0].equals("gun"))){
				plugin.getLogger().log(Level.SEVERE, Utils.colorRawMessage("Validator.Invalid-Item-Type").replaceAll("%name%", pinata));
				return false;
			}
			if(partsvaild[0].equals("item")){
				if(partsvaild.length < 5){
					plugin.getLogger().log(Level.SEVERE, Utils.colorRawMessage("Validator.Invalid-Configuration").replaceAll("%number%", String.valueOf(i + 1)).replaceAll("%name%", pinata));
					return false;
				}
				if(Material.getMaterial(partsvaild[1].toUpperCase()) == null || partsvaild[2].equals("0")){
					plugin.getLogger().log(Level.SEVERE, Utils.colorRawMessage("Validator.Invalid-Item").replaceAll("%number%", String.valueOf(i + 1)).replaceAll("%name%", pinata));
					return false;
				}
				if(Integer.parseInt(partsvaild[4]) == 0){
					plugin.getLogger().log(Level.WARNING, Utils.colorRawMessage("Validator.Invalid-Chance").replaceAll("%number%", String.valueOf(i + 1)).replaceAll("%name%", pinata));
				}
			} else if(partsvaild[0].equals("command")){
				if(partsvaild.length < 3){
					plugin.getLogger().log(Level.SEVERE, Utils.colorRawMessage("Validator.Invalid-Configuration").replaceAll("%number%", String.valueOf(i + 1)).replaceAll("%name%", pinata));
				}
				if(Integer.parseInt(partsvaild[3]) == 0){
					plugin.getLogger().log(Level.WARNING, Utils.colorRawMessage("Validator.Invalid-Chance").replaceAll("%number%", String.valueOf(i + 1)).replaceAll("%name%", pinata));
				}
			} else if(partsvaild[0].equals("money")){
				if(plugin.getVaultUse()){
					if(partsvaild.length < 3){
						plugin.getLogger().log(Level.SEVERE, Utils.colorRawMessage("Validator.Invalid-Configuration").replaceAll("%number%", String.valueOf(i + 1)).replaceAll("%name%", pinata));
						return false;
					}
					if(Integer.parseInt(partsvaild[1]) == 0){
						plugin.getLogger().log(Level.WARNING, Utils.colorRawMessage("Validator.Invalid-Money-Drop").replaceAll("%number%", String.valueOf(i + 1)).replaceAll("%name%", pinata));
					}
					if(Integer.parseInt(partsvaild[3]) == 0){
						plugin.getLogger().log(Level.WARNING, Utils.colorRawMessage("Validator.Invalid-Chance").replaceAll("%number%", String.valueOf(i + 1)).replaceAll("%name%", pinata));
					}
				} else{
					plugin.getLogger().log(Level.SEVERE, Utils.colorRawMessage("Validator.Invalid-Item-Type").replaceAll("%number%", String.valueOf(i + 1)).replaceAll("%name%", pinata));
					plugin.getLogger().log(Level.SEVERE, "Vault plugin not found!");
					return false;
				}
			} else if(partsvaild[0].equals("gun")){
				if(plugin.getCrackshotUse()){
					if(partsvaild.length < 3){
						plugin.getLogger().log(Level.SEVERE, Utils.colorRawMessage("Validator.Invalid-Configuration").replaceAll("%number%", String.valueOf(i + 1)).replaceAll("%name%", pinata));
						return false;
					}
					if(Integer.parseInt(partsvaild[3]) == 0){
						plugin.getLogger().log(Level.WARNING, Utils.colorRawMessage("Validator.Invalid-Chance").replaceAll("%number%", String.valueOf(i + 1)).replaceAll("%name%", pinata));
					}
				} else{
					plugin.getLogger().log(Level.SEVERE, Utils.colorRawMessage("Validator.Invalid-Item-Type").replaceAll("%number%", String.valueOf(i + 1)).replaceAll("%name%", pinata));
					plugin.getLogger().log(Level.SEVERE, "CrackShot plugin not found!");
					return false;
				}
			}
		}
		return true;
	}

	public List<String> getPinatalist() {
		return pinatalist;
	}

	public Map<String, ArrayList<String>> getPinataDrop() {
		return pinatadrops;
	}
}
