package pl.plajer.pinata.pinataapi;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LeashHitch;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import pl.plajer.pinata.Main;
import pl.plajer.pinata.PinataData;
import pl.plajer.pinata.Utils;

/**
 * Class with pinata creation methods.
 */
public class PinataFactory implements Listener {

	/**
	 * Creates pinata at specified location for target player using already spawned sheep, name of pinata required.
	 * 
	 * @param fenceLocation location where to spawn pinata
	 * @param player player who will be owner of pinata
	 * @param sheep sheep that will be transformed to pinata
	 * @param pinataName name of pinata from pinatas.yml
	 * @return <b>true</b> if creation succeed, <b>false</b> if creation couldn't be completed
	 */
	public static boolean createPinata(final Location fenceLocation, final Player player, final Sheep sheep, final String pinataName){
		PinataCreateEvent pce = new PinataCreateEvent(player, sheep, pinataName);
		Bukkit.getPluginManager().callEvent(pce);
		if(pce.isCancelled()) {
			sheep.remove();
			if(fenceLocation.getBlock().getType().equals(Material.FENCE)){
				fenceLocation.getBlock().setType(Material.AIR);
			}
			return false;
		}
		if(!(fenceLocation.getBlock().getType().equals(Material.AIR))){
			player.sendMessage(Utils.colorRawMessage("Pinata.Create.Fail"));
			pce.setCancelled(true);
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
		Main.getInstance().getCommands().getPinata().put(sheep, new PinataData(player, fenceLocation, hitch));
		sheep.setHealth(5);
		sheep.setCustomName(pinataName);
		sheep.setColor(DyeColor.valueOf(Main.getInstance().getFileManager().getPinataConfig().get("pinatas." + pinataName + ".color").toString().toUpperCase()));
		sheep.setLeashHolder(hitch);
		if(Main.getInstance().getConfig().getBoolean("blindness-effect")) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Main.getInstance().getConfig().getInt("blindness-duration") * 20, 1));
			if(Main.getInstance().getConfig().getBoolean("full-blindness-effect")) {
				player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Main.getInstance().getConfig().getInt("blindness-duration") * 20, 1));
			}
		}
		//Scheduler to avoid graphical glitch
		Bukkit.getScheduler().runTaskLater(Main.getInstance(), new Runnable(){
			@Override
			public void run(){
				sheep.setLeashHolder(hitch);
			}
		}, 18);
		return true;
	}

}
