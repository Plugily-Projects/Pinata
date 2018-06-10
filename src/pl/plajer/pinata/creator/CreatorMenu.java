package pl.plajer.pinata.creator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import pl.plajer.pinata.ConfigurationManager;
import pl.plajer.pinata.Main;
import pl.plajer.pinata.utils.ItemBuilder;

/**
 * @author Plajer
 * <p>
 * Created at 02.06.2018
 */
public class CreatorMenu {

    private static String pinataName;
    private Inventory inventory;
    private Main plugin = JavaPlugin.getPlugin(Main.class);

    public CreatorMenu(String pinata) {
        this.inventory = Bukkit.createInventory(null, 9 * 2, "Editing pinata: " + pinata);
        pinataName = pinata;

        addItem(new ItemBuilder(new ItemStack(Material.NAME_TAG))
                .name(ChatColor.GOLD + "► Set" + ChatColor.GREEN + " pinata name")
                .lore(ChatColor.GRAY + "Replace this name tag with named name tag.")
                .lore(ChatColor.GRAY + "It will be set as pinata name.")
                .lore(ChatColor.RED + "" + ChatColor.BOLD + "Drop name tag here don't move")
                .lore(ChatColor.RED + "" + ChatColor.BOLD + "it and replace with new!!!")
                .lore(isOptionDone("display-name"))
                .build());
        addItem(new ItemBuilder(new ItemStack(Material.LAPIS_BLOCK))
                .name(ChatColor.GOLD + "► Set" + ChatColor.WHITE + " mob type")
                .lore(ChatColor.GRAY + "Click to set mob type of pinata")
                .lore(isOptionDone("mob-entity-type"))
                .build());
        addItem(new ItemBuilder(new ItemStack(Material.NAME_TAG))
                .name(ChatColor.GOLD + "► Set" + ChatColor.YELLOW + " pinata permission")
                .lore(ChatColor.GRAY + "Replace this name tag with named name tag.")
                .lore(ChatColor.GRAY + "It will be set as pinata permission.")
                .lore(ChatColor.RED + "" + ChatColor.BOLD + "Drop name tag here don't move")
                .lore(ChatColor.RED + "" + ChatColor.BOLD + "it and replace with new!!!")
                .lore(isOptionDone("permission-string"))
                .build());
        addItem(new ItemBuilder(new ItemStack(Material.RAW_FISH))
                .name(ChatColor.GOLD + "► Set" + ChatColor.DARK_GREEN + " damage type")
                .lore(ChatColor.GRAY + "Click to set damage type of pinata")
                .lore(ChatColor.GRAY + "(can be damaged by everyone or by creator)")
                .lore(isOptionDone("pinata-access-type"))
                .build());
        addItem(new ItemBuilder(new ItemStack(Material.REDSTONE))
                .name(ChatColor.GOLD + "► Set" + ChatColor.RED + " drop type")
                .lore(ChatColor.GRAY + "Click to set drop type of pinata")
                .lore(ChatColor.GRAY + "(drop items when punched or when dead)")
                .lore(isOptionDone("items-drop-type"))
                .build());
        addItem(new ItemBuilder(new ItemStack(Material.RED_ROSE))
                .name(ChatColor.GOLD + "► Set" + ChatColor.DARK_RED + " health")
                .lore(ChatColor.GRAY + "Set max health of pinata.")
                .lore(ChatColor.GRAY + "More health = more damage to kill.")
                .lore(ChatColor.GRAY + "Useful if you have drop type: punch")
                .lore(isOptionDone("health-amount"))
                .build());
        addItem(new ItemBuilder(new ItemStack(Material.STRING))
                .name(ChatColor.GOLD + "► Set" + ChatColor.YELLOW + " crate alive time")
                .lore(ChatColor.GRAY + "Set how many seconds pinata will be")
                .lore(ChatColor.GRAY + "alive after spawning in crate.")
                .lore(isOptionDone("crate-display-time-alive"))
                .build());
        addItem(new ItemBuilder(new ItemStack(Material.DOUBLE_PLANT))
                .name(ChatColor.GOLD + "► Set" + ChatColor.DARK_PURPLE + " drop view time")
                .lore(ChatColor.GRAY + "Set how many seconds drops of pinata")
                .lore(ChatColor.GRAY + "will be visible on the ground.")
                .lore(isOptionDone("timer-display"))
                .build());
        addItem(new ItemBuilder(new ItemStack(Material.COAL))
                .name(ChatColor.GOLD + "► Set" + ChatColor.GRAY + " blindness effect")
                .lore(ChatColor.GRAY + "Should creator have blindness effect?")
                .lore(ChatColor.RED + "You must enable it otherwise full blindness won't work!")
                .lore(isOptionDone("blindness-activated"))
                .build());
        addItem(new ItemBuilder(new ItemStack(Material.ENDER_PEARL))
                .name(ChatColor.GOLD + "► Set" + ChatColor.LIGHT_PURPLE + " blindness duration")
                .lore(ChatColor.GRAY + "Set for how many seconds creator should have")
                .lore(ChatColor.GRAY + "blindness effect.")
                .lore(ChatColor.GRAY + "Set 0 to disable.")
                .lore(isOptionDone("blindness-duration"))
                .build());
        addItem(new ItemBuilder(new ItemStack(Material.EYE_OF_ENDER))
                .name(ChatColor.GOLD + "► Set" + ChatColor.GRAY + " full blindness effect")
                .lore(ChatColor.GRAY + "Should creator have full blindness effect?")
                .lore(ChatColor.GRAY + "This is a mix of night vision and blindness.")
                .lore(isOptionDone("full-blindness-activated"))
                .build());
        addItem(new ItemBuilder(new ItemStack(Material.CHEST))
                .name(ChatColor.GOLD + "► Edit pinata drops")
                .lore(ChatColor.GRAY + "Edit pinata drops here")
                .build());
    }

    private static String isOptionDone(String path) {
        return ChatColor.GOLD + "" + ChatColor.BOLD + "Currently: " + ChatColor.GRAY + ConfigurationManager.getConfig("pinata_storage").getString("storage." + pinataName + "." + path);
    }

    public void addItem(ItemStack itemStack) {
        inventory.addItem(itemStack);
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void openInventory(Player player) {
        player.openInventory(inventory);
    }

}
