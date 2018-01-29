package pl.plajer.pinata.pinataapi;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import pl.plajer.pinata.Main;
import pl.plajer.pinata.PinataData;
import pl.plajer.pinata.utils.Utils;

/**
 * Class with pinata creation methods.
 */
public class PinataFactory implements Listener {

    /**
     * Creates pinata at specified location for target player using already spawned entity, name of pinata required.
     *
     * @param fenceLocation location where to spawn pinata
     * @param player        player who will be owner of pinata
     * @param entity        entity that will be transformed to pinata
     * @param pinataName    name of pinata from pinatas.yml
     * @return <b>true</b> if creation succeed, <b>false</b> if creation couldn't be completed
     */
    public static boolean createPinata(final Location fenceLocation, final Player player, final LivingEntity entity, final String pinataName) {
        PinataCreateEvent pce = new PinataCreateEvent(player, entity, pinataName);
        Bukkit.getPluginManager().callEvent(pce);
        if(pce.isCancelled()) {
            entity.remove();
            if(fenceLocation.getBlock().getType().equals(Material.FENCE)) {
                fenceLocation.getBlock().setType(Material.AIR);
            }
            return false;
        }
        if(!(fenceLocation.getBlock().getType().equals(Material.AIR))) {
            player.sendMessage(Utils.colorRawMessage("Pinata.Create.Fail"));
            entity.remove();
            if(fenceLocation.getBlock().getType().equals(Material.FENCE)) {
                fenceLocation.getBlock().setType(Material.AIR);
            }
            return false;
        }
        player.sendMessage(Utils.colorRawMessage("Pinata.Create.Success").replaceAll("%name%", pinataName));
        Main.getInstance().getCommands().getUsers().add(player);
        //Max height check is to avoid problems with different server specifications
        Location safefence = new Location(player.getWorld(), 3, player.getWorld().getMaxHeight() - 1, 2);
        Location safestone = new Location(player.getWorld(), 4, player.getWorld().getMaxHeight() - 1, 2);
        Material blocksafe = safefence.getBlock().getType();
        safefence.getBlock().setType(Material.FENCE);
        safestone.getBlock().setType(Material.STONE);
        final LeashHitch hitch = (LeashHitch) safefence.getWorld().spawnEntity(safefence, EntityType.LEASH_HITCH);
        safestone.getBlock().setType(Material.AIR);
        fenceLocation.getBlock().setType(Material.FENCE);
        hitch.teleport(fenceLocation);
        safefence.getBlock().setType(blocksafe);
        Main.getInstance().getCommands().getPinata().put(entity, new PinataData(player, fenceLocation, hitch));
        entity.setCustomName(pinataName);
        if(entity instanceof Sheep) {
            ((Sheep) entity).setColor(DyeColor.valueOf(Main.getInstance().getFileManager().getPinataConfig().get("pinatas." + pinataName + ".color").toString().toUpperCase()));
        }
        entity.setLeashHolder(hitch);
        if(Main.getInstance().getConfig().getBoolean("blindness-effect")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Main.getInstance().getConfig().getInt("blindness-duration") * 20, 1));
            if(Main.getInstance().getConfig().getBoolean("full-blindness-effect")) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Main.getInstance().getConfig().getInt("blindness-duration") * 20, 1));
            }
        }
        //Scheduler to avoid graphical glitch
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> entity.setLeashHolder(hitch), 20);
        return true;
    }

}
