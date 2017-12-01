package pl.plajer.pinata;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Utils {

	public static String colorRawMessage(String message) {
		return ChatColor.translateAlternateColorCodes('&', Main.getInstance().getFileManager().getMessagesConfig().getString(message));
	}

	public static String colorMessage(String message) {
		return ChatColor.translateAlternateColorCodes('&', message);
	}
	
	@SuppressWarnings("deprecation")
	public static void createPinatasGUI(String name, Player p) {
		int rows = 1;
		float trick = Main.getInstance().getPinataManager().getPinatalist().size() / 9;
		if(!((int) trick % 9 == 0)){
			rows++;
		}
		Inventory pinatasMenu = Bukkit.createInventory(null, rows*9, Utils.colorRawMessage(name));
		for(int i = 0; i < Main.getInstance().getPinataManager().getPinatalist().size(); i++){
			String pinata = Main.getInstance().getPinataManager().getPinatalist().get(i).toString();
			ItemStack item = new ItemStack(Material.WOOL, 1, DyeColor.valueOf(Main.getInstance().getFileManager().getPinataConfig().get("pinatas." + pinata + ".color").toString().toUpperCase()).getDyeData());
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(Utils.colorMessage("&6") + pinata);
			List<String> lore = new ArrayList<>();
			if(Main.getInstance().getFileManager().getPinataConfig().get("pinatas." + pinata + ".type").equals("private")){
				lore.add(colorRawMessage("Menus.List-Menu.Pinata-Types.Type-Private"));
			} else{
				lore.add(colorRawMessage("Menus.List-Menu.Pinata-Types.Type-Public"));
			}
			if(Integer.parseInt(Main.getInstance().getFileManager().getPinataConfig().get("pinatas." + pinata + ".cost").toString()) == -1){
				lore.add(colorRawMessage("Menus.List-Menu.Pinata-Cost-Not-For-Sale"));
			} else{
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
