package pl.plajer.pinata.creator;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import pl.plajer.pinata.ConfigurationManager;
import pl.plajer.pinata.Main;
import pl.plajer.pinata.pinata.Pinata;

/**
 * @author Plajer
 * <p>
 * Created at 02.06.2018
 */
public class CreatorEvents implements Listener {

    private Main plugin;

    public CreatorEvents(Main plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if(e.getInventory().getName() == null || e.getCurrentItem() == null) return;
        if(e.getInventory().getName().contains("Editing pinata: ")) {
            String pInvName = e.getInventory().getName().replace("Editing pinata: ", "");
            Pinata pinata = null;
            for(Pinata p : plugin.getPinataManager().getPinataList()) {
                if(p.getID().equals(pInvName)) {
                    pinata = p;
                }
            }
            e.setCancelled(true);
            if(pinata == null) return;
            if(e.getCurrentItem().getItemMeta() == null || !e.getCurrentItem().getItemMeta().hasDisplayName()) return;
            FileConfiguration config = ConfigurationManager.getConfig("pinata_storage");
            switch(ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName())) {
                case "► Set pinata name":
                    if(e.getCurrentItem().getType() == Material.NAME_TAG && e.getCursor().getType() == Material.NAME_TAG) {
                        e.setCancelled(true);
                        if(!e.getCursor().hasItemMeta()) {
                            e.getWhoClicked().sendMessage(ChatColor.RED + "This item doesn't has a name!");
                            return;
                        }
                        if(!e.getCursor().getItemMeta().hasDisplayName()) {
                            e.getWhoClicked().sendMessage(ChatColor.RED + "This item doesn't has a name!");
                            return;
                        }
                        pinata.setName(e.getCurrentItem().getItemMeta().getDisplayName());
                        config.set("storage." + pinata.getID() + ".display-name", e.getCurrentItem().getItemMeta().getDisplayName());
                        //todo
                        e.getWhoClicked().sendMessage("Pinata display name set to " + e.getCurrentItem().getItemMeta().getDisplayName());
                        return;
                    }
                case "► Set mob type":
                    plugin.getCreatorChatEvents().getChatReactions().put((Player) e.getWhoClicked(), CreatorChatEvents.ChatReaction.SET_MOB_TYPE);
                case "► Set pinata permission":
                    if(e.getCurrentItem().getType() == Material.NAME_TAG && e.getCursor().getType() == Material.NAME_TAG) {
                        e.setCancelled(true);
                        if(!e.getCursor().hasItemMeta()) {
                            e.getWhoClicked().sendMessage(ChatColor.RED + "This item doesn't has a name!");
                            return;
                        }
                        if(!e.getCursor().getItemMeta().hasDisplayName()) {
                            e.getWhoClicked().sendMessage(ChatColor.RED + "This item doesn't has a name!");
                            return;
                        }
                        pinata.setPermission(e.getCurrentItem().getItemMeta().getDisplayName());
                        config.set("storage." + pinata.getID() + ".permission-string", e.getCurrentItem().getItemMeta().getDisplayName());
                        //todo
                        e.getWhoClicked().sendMessage("Pinata access permission set to " + e.getCurrentItem().getItemMeta().getDisplayName());
                        return;
                    }
                case "► Set damage type":
                case "► Set drop type":
                case "► Set health":
                    plugin.getCreatorChatEvents().getChatReactions().put((Player) e.getWhoClicked(), CreatorChatEvents.ChatReaction.SET_HEALTH);
                case "► Set crate alive time":
                    plugin.getCreatorChatEvents().getChatReactions().put((Player) e.getWhoClicked(), CreatorChatEvents.ChatReaction.SET_CRATE_TIME);
                case "► Set drop view time":
                    plugin.getCreatorChatEvents().getChatReactions().put((Player) e.getWhoClicked(), CreatorChatEvents.ChatReaction.SET_DROP_VIEW_TIME);
                case "► Set blindness duration":
                    plugin.getCreatorChatEvents().getChatReactions().put((Player) e.getWhoClicked(), CreatorChatEvents.ChatReaction.SET_BLINDNESS_DURATION);
                case "► Set blindness effect":
                    plugin.getCreatorChatEvents().getChatReactions().put((Player) e.getWhoClicked(), CreatorChatEvents.ChatReaction.SET_FULL_BLINDNESS);
                case "► Edit pinata drops":
            }
            e.getWhoClicked().closeInventory();
            ConfigurationManager.saveConfig(config, "pinata_storage");
        }
    }

}
