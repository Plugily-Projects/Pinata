package pl.plajer.pinata.creator;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

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
        //todo
    }

    public enum ChatReaction {
        SET_MOB_TYPE, SET_HEALTH, SET_CRATE_TIME, SET_DROP_VIEW_TIME, SET_BLINDNESS_DURATION, SET_FULL_BLINDNESS
    }

    public Map<Player, ChatReaction> getChatReactions() {
        return chatReactions;
    }
}
