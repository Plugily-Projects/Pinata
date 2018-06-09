package pl.plajer.pinata.creator;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import pl.plajer.pinata.Main;

/**
 * @author Plajer
 * <p>
 * Created at 02.06.2018
 */
public class CreatorEvents implements Listener {

    private Main plugin;

    public CreatorEvents(Main plugin){
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e){
        if(e.getInventory().getName() == null || e.getCurrentItem() == null) return;
        //todo
        /*for(Pinata pinata : plugin.getPinataManager().getPinataList()){

        }*/
        if(e.getInventory().getName().contains("Editing pinata: ")) {
            e.setCancelled(true);
            if(e.getCurrentItem().getItemMeta() == null || !e.getCurrentItem().getItemMeta().hasDisplayName()) return;
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

                        return;
                    }
                case "► Set mob type":
                    plugin.getCreatorChatEvents().getChatReactions().put((Player) e.getWhoClicked(), CreatorChatEvents.ChatReaction.SET_MOB_TYPE);
                case "► Set pinata permission":
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
        }
    }

}
