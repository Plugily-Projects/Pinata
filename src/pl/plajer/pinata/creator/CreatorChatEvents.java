package pl.plajer.pinata.creator;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import pl.plajer.pinata.ConfigurationManager;
import pl.plajer.pinata.Main;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Plajer
 * <p>
 * Created at 02.06.2018
 */
public class CreatorChatEvents implements Listener {

    private Map<Player, ChatReaction> chatReactions = new HashMap<>();

    public CreatorChatEvents(Main plugin){
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if(chatReactions.containsKey(e.getPlayer())) {
            ChatReaction reaction = chatReactions.get(e.getPlayer());
            FileConfiguration config = ConfigurationManager.getConfig("pinata_storage");
            e.setCancelled(true);
            switch(reaction.getReactionType()) {
                case SET_MOB_TYPE:
                    try {
                        EntityType type = EntityType.valueOf(e.getMessage().toUpperCase());
                        reaction.getPinata().setEntityType(type);
                        config.set("storage." + reaction.getPinata().getID() + ".mob-entity-type", type.name());
                        e.getPlayer().sendMessage("Entity type of pinata " + reaction.getPinata().getID() + " has been set to " + type.name());
                        chatReactions.remove(e.getPlayer());
                    } catch(IllegalArgumentException ex) {
                        e.getPlayer().sendMessage("That entity doesn't exist! Use following types:");
                        StringBuilder mobs = new StringBuilder();
                        for(EntityType en : EntityType.values()) {
                            if(en == null || en.getName() == null) continue;
                            mobs.append(en.getName()).append(", ");
                        }
                        e.getPlayer().sendMessage(mobs.toString());
                    }
                    break;
                case SET_HEALTH:
                    if(!NumberUtils.isNumber(e.getMessage())) {
                        e.getPlayer().sendMessage("Following message is not a number!");
                        return;
                    }
                    reaction.getPinata().setHealth(Double.parseDouble(e.getMessage()));
                    config.set("storage." + reaction.getPinata().getID() + ".health-amount", Double.parseDouble(e.getMessage()));
                    e.getPlayer().sendMessage("Health of pinata " + reaction.getPinata().getID() + " has been set to " + e.getMessage());
                    chatReactions.remove(e.getPlayer());
                    break;
                case SET_CRATE_TIME:
                    if(!NumberUtils.isNumber(e.getMessage())) {
                        e.getPlayer().sendMessage("Following message is not a number!");
                        return;
                    }
                    reaction.getPinata().setCrateTime(Integer.parseInt(e.getMessage()));
                    config.set("storage." + reaction.getPinata().getID() + ".crate-display-time-alive", Integer.parseInt(e.getMessage()));
                    e.getPlayer().sendMessage("Crate time of pinata " + reaction.getPinata().getID() + " has been set to " + e.getMessage());
                    chatReactions.remove(e.getPlayer());
                    break;
                case SET_DROP_VIEW_TIME:
                    if(!NumberUtils.isNumber(e.getMessage())) {
                        e.getPlayer().sendMessage("Following message is not a number!");
                        return;
                    }
                    reaction.getPinata().setDropViewTime(Integer.parseInt(e.getMessage()));
                    config.set("storage." + reaction.getPinata().getID() + ".timer-display", Integer.parseInt(e.getMessage()));
                    e.getPlayer().sendMessage("Drop view time of pinata " + reaction.getPinata().getID() + " has been set to " + e.getMessage());
                    chatReactions.remove(e.getPlayer());
                    break;
                case SET_BLINDNESS_DURATION:
                    if(!NumberUtils.isNumber(e.getMessage())) {
                        e.getPlayer().sendMessage("Following message is not a number!");
                        return;
                    }
                    reaction.getPinata().setBlindnessTime(Integer.parseInt(e.getMessage()));
                    config.set("storage." + reaction.getPinata().getID() + ".blindness-duration", Integer.parseInt(e.getMessage()));
                    e.getPlayer().sendMessage("Blindness duration of pinata " + reaction.getPinata().getID() + " has been set to " + e.getMessage());
                    chatReactions.remove(e.getPlayer());
                    break;
                case SET_BLINDNESS:
                    if(BooleanUtils.toBooleanObject(e.getMessage()) == null) {
                        e.getPlayer().sendMessage("Following message is not a boolean!");
                        return;
                    }
                    reaction.getPinata().setBlindnessEnabled(BooleanUtils.toBoolean(e.getMessage()));
                    config.set("storage." + reaction.getPinata().getID() + ".blindness-activated", BooleanUtils.toBoolean(e.getMessage()));
                    e.getPlayer().sendMessage("Blindness of of pinata " + reaction.getPinata().getID() + " has been set to " + e.getMessage());
                    chatReactions.remove(e.getPlayer());
                    break;
                case SET_FULL_BLINDNESS:
                    if(BooleanUtils.toBooleanObject(e.getMessage()) == null) {
                        e.getPlayer().sendMessage("Following message is not a boolean!");
                        return;
                    }
                    reaction.getPinata().setFullBlindness(BooleanUtils.toBoolean(e.getMessage()));
                    config.set("storage." + reaction.getPinata().getID() + ".full-blindness-activated", BooleanUtils.toBoolean(e.getMessage()));
                    e.getPlayer().sendMessage("Full blindness of of pinata " + reaction.getPinata().getID() + " has been set to " + e.getMessage());
                    chatReactions.remove(e.getPlayer());
                    break;
            }
            ConfigurationManager.saveConfig(config, "pinata_storage");
        }
    }

    public Map<Player, ChatReaction> getChatReactions() {
        return chatReactions;
    }
}
