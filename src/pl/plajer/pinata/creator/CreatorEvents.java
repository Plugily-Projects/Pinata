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
            Pinata pinata = plugin.getPinataManager().getPinataByName(e.getInventory().getName().replace("Editing pinata: ", ""));
            if(pinata == null) return;
            if(e.getCurrentItem().getItemMeta() == null || !e.getCurrentItem().getItemMeta().hasDisplayName()) return;
            FileConfiguration config = ConfigurationManager.getConfig("pinata_storage");
            switch(ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName())) {
                case "► Set pinata name":
                    if(e.getCurrentItem().getType() == Material.NAME_TAG && e.getCursor().getType() == Material.NAME_TAG) {
                        e.setCancelled(true);
                        if(!e.getCursor().hasItemMeta()) {
                            e.getWhoClicked().sendMessage(ChatColor.RED + "This item doesn't has a name!");
                            break;
                        }
                        if(!e.getCursor().getItemMeta().hasDisplayName()) {
                            e.getWhoClicked().sendMessage(ChatColor.RED + "This item doesn't has a name!");
                            break;
                        }
                        pinata.setName(e.getCursor().getItemMeta().getDisplayName());
                        config.set("storage." + pinata.getID() + ".display-name", e.getCursor().getItemMeta().getDisplayName());
                        //todo
                        e.getWhoClicked().sendMessage("Pinata display name set to " + e.getCursor().getItemMeta().getDisplayName());
                    }
                    break;
                case "► Set mob type":
                    e.setCancelled(true);
                    plugin.getCreatorChatEvents().getChatReactions().put((Player) e.getWhoClicked(), new ChatReaction(ChatReaction.ReactionType.SET_MOB_TYPE, pinata));
                    e.getWhoClicked().sendMessage("Type valid mob type in chat!");
                    e.getWhoClicked().closeInventory();
                    break;
                case "► Set pinata permission":
                    if(e.getCurrentItem().getType() == Material.NAME_TAG && e.getCursor().getType() == Material.NAME_TAG) {
                        e.setCancelled(true);
                        if(!e.getCursor().hasItemMeta()) {
                            e.getWhoClicked().sendMessage(ChatColor.RED + "This item doesn't has a name!");
                            break;
                        }
                        if(!e.getCursor().getItemMeta().hasDisplayName()) {
                            e.getWhoClicked().sendMessage(ChatColor.RED + "This item doesn't has a name!");
                            break;
                        }
                        pinata.setPermission(e.getCursor().getItemMeta().getDisplayName());
                        config.set("storage." + pinata.getID() + ".permission-string", e.getCursor().getItemMeta().getDisplayName());
                        //todo
                        e.getWhoClicked().sendMessage("Pinata access permission set to " + e.getCursor().getItemMeta().getDisplayName());
                    }
                    break;
                case "► Set damage type":
                    e.setCancelled(true);
                    e.getWhoClicked().sendMessage("soon");
                    e.getWhoClicked().closeInventory();
                    break;
                case "► Set drop type":
                    e.setCancelled(true);
                    e.getWhoClicked().sendMessage("soon");
                    e.getWhoClicked().closeInventory();
                    break;
                case "► Set health":
                    e.setCancelled(true);
                    plugin.getCreatorChatEvents().getChatReactions().put((Player) e.getWhoClicked(), new ChatReaction(ChatReaction.ReactionType.SET_HEALTH, pinata));
                    e.getWhoClicked().sendMessage("Type health amount of pinata");
                    e.getWhoClicked().closeInventory();
                    break;
                case "► Set crate alive time":
                    e.setCancelled(true);
                    plugin.getCreatorChatEvents().getChatReactions().put((Player) e.getWhoClicked(), new ChatReaction(ChatReaction.ReactionType.SET_CRATE_TIME, pinata));
                    e.getWhoClicked().sendMessage("Type crate time in chat");
                    e.getWhoClicked().closeInventory();
                    break;
                case "► Set drop view time":
                    e.setCancelled(true);
                    plugin.getCreatorChatEvents().getChatReactions().put((Player) e.getWhoClicked(), new ChatReaction(ChatReaction.ReactionType.SET_DROP_VIEW_TIME, pinata));
                    e.getWhoClicked().sendMessage("Type drop view time in chat");
                    e.getWhoClicked().closeInventory();
                    break;
                case "► Set blindness effect":
                    e.setCancelled(true);
                    plugin.getCreatorChatEvents().getChatReactions().put((Player) e.getWhoClicked(), new ChatReaction(ChatReaction.ReactionType.SET_BLINDNESS, pinata));
                    e.getWhoClicked().sendMessage("Type true or false to set blindness enabled/disabled");
                    e.getWhoClicked().closeInventory();
                    break;
                case "► Set blindness duration":
                    e.setCancelled(true);
                    plugin.getCreatorChatEvents().getChatReactions().put((Player) e.getWhoClicked(), new ChatReaction(ChatReaction.ReactionType.SET_BLINDNESS_DURATION, pinata));
                    e.getWhoClicked().sendMessage("Type blindness duration in chat");
                    e.getWhoClicked().closeInventory();
                    break;
                case "► Set full blindness effect":
                    e.setCancelled(true);
                    plugin.getCreatorChatEvents().getChatReactions().put((Player) e.getWhoClicked(), new ChatReaction(ChatReaction.ReactionType.SET_FULL_BLINDNESS, pinata));
                    e.getWhoClicked().sendMessage("Type true or false to set full blindness effect");
                    e.getWhoClicked().closeInventory();
                    break;
                case "► Edit pinata drops":
                    e.setCancelled(true);
                    e.getWhoClicked().sendMessage("soon");
                    e.getWhoClicked().closeInventory();
                    break;
            }
            ConfigurationManager.saveConfig(config, "pinata_storage");
        }
    }

}
