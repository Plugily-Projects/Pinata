package pl.plajer.pinata;

import org.bukkit.ChatColor;

public class Utils {
	
	public static String colorRawMessage(String message) {
		return ChatColor.translateAlternateColorCodes('&', Main.getInstance().getFileManager().getMessagesConfig().getString(message));
	}
	
	public static String colorMessage(String message) {
		return ChatColor.translateAlternateColorCodes('&', message);
	}
	
}
