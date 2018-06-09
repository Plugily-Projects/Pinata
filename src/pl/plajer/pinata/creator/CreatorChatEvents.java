package pl.plajer.pinata.creator;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import pl.plajer.pinata.ConfigurationManager;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Plajer
 * <p>
 * Created at 02.06.2018
 */
public class CreatorChatEvents implements Listener {

    private Map<Player, ChatReaction> chatReactions = new HashMap<>();

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if(chatReactions.containsKey(e.getPlayer())) {
            ChatReaction reaction = chatReactions.get(e.getPlayer());
            FileConfiguration config = ConfigurationManager.getConfig("pinata_storage");
            switch(reaction.getReactionType()) {
                case SET_MOB_TYPE:
                    try {
                        EntityType type = EntityType.valueOf(e.getMessage());
                        reaction.getPinata().setEntityType(type);
                        config.set("storage." + reaction.getPinata().getID() + ".mob-entity-type", type.name());
                        e.getPlayer().sendMessage("Entity type of pinata " + reaction.getPinata().getID() + " has been set to " + type.name());
                        chatReactions.remove(e.getPlayer());
                    } catch(IllegalArgumentException ex) {
                        e.getPlayer().sendMessage("That entity doesn't exist! Use following types:");
                        StringBuilder mobs = new StringBuilder();
                        for(EntityType en : EntityType.values()) {
                            if(en == null) continue;
                            mobs.append(en.getName()).append(", ");
                        }
                        e.getPlayer().sendMessage(mobs.toString());
                        return;
                    }
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
                    break;
                case SET_DROP_VIEW_TIME:
                    break;
                case SET_BLINDNESS_DURATION:
                    break;
                case SET_FULL_BLINDNESS:
                    break;
            }
        }
    }

    public Map<Player, ChatReaction> getChatReactions() {
        return chatReactions;
    }
}
