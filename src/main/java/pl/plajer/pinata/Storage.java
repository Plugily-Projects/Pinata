package pl.plajer.pinata;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import pl.plajer.pinata.pinata.LivingPinata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Plajer
 * <p>
 * Created at 13.02.2019
 *
 * @deprecated only temporary
 */
@Deprecated
public class Storage {

    private Map<Entity, LivingPinata> pinata = new HashMap<>();
    private List<Player> users = new ArrayList<>();

    public Map<Entity, LivingPinata> getPinata() {
        return pinata;
    }

    public List<Player> getUsers() {
        return users;
    }

    public void setUsers(List<Player> users) {
        this.users = users;
    }

}
