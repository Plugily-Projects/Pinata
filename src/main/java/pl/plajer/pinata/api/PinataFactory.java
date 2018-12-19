/*
 * Pinata plugin - spawn pinata mob and kill it to get drops
 * Copyright (C)2018 Plajer
 *
 *  This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package pl.plajer.pinata.api;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LeashHitch;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import pl.plajer.pinata.Main;
import pl.plajer.pinata.pinata.LivingPinata;
import pl.plajer.pinata.pinata.Pinata;
import pl.plajer.pinata.utils.Utils;
import pl.plajerlair.core.utils.XMaterial;

/**
 * Class with pinata creation methods.
 */
public class PinataFactory implements Listener {

  private static Main plugin = JavaPlugin.getPlugin(Main.class);

  /**
   * todo
   * Creates pinata at specified location for target player using already spawned entity, name of pinata required.
   *
   * @param fenceLocation location where to spawn pinata
   * @param player        player who will be owner of pinata
   * @param entity        entity that will be transformed to pinata
   * @param pinata        name of pinata from pinatas.yml
   * @return <b>true</b> if creation succeed, <b>false</b> if creation couldn't be completed
   */
  public static boolean createPinata(final Location fenceLocation, final Player player, final LivingEntity entity, final Pinata pinata) {
    PinataCreateEvent pce = new PinataCreateEvent(player, entity, pinata);
    Bukkit.getPluginManager().callEvent(pce);
    if (pce.isCancelled()) {
      entity.remove();
      if (fenceLocation.getBlock().getType().equals(XMaterial.OAK_FENCE.parseMaterial())) {
        fenceLocation.getBlock().setType(Material.AIR);
      }
      return false;
    }
    if (!(fenceLocation.getBlock().getType().equals(Material.AIR))) {
      player.sendMessage(Utils.colorMessage("Pinata.Create.Fail"));
      entity.remove();
      if (fenceLocation.getBlock().getType().equals(XMaterial.OAK_FENCE.parseMaterial())) {
        fenceLocation.getBlock().setType(Material.AIR);
      }
      return false;
    }
    player.sendMessage(Utils.colorMessage("Pinata.Create.Success").replace("%name%", pinata.getID()));
    plugin.getCommands().getUsers().add(player);
    //Max height check is to avoid problems with different server specifications
    Location safefence = new Location(player.getWorld(), 3, player.getWorld().getMaxHeight() - 1, 2);
    Location safestone = new Location(player.getWorld(), 4, player.getWorld().getMaxHeight() - 1, 2);
    Material blocksafe = safefence.getBlock().getType();
    safefence.getBlock().setType(XMaterial.OAK_FENCE.parseMaterial());
    safestone.getBlock().setType(Material.STONE);
    final LeashHitch hitch = (LeashHitch) safefence.getWorld().spawnEntity(safefence, EntityType.LEASH_HITCH);
    safestone.getBlock().setType(Material.AIR);
    fenceLocation.getBlock().setType(XMaterial.OAK_FENCE.parseMaterial());
    hitch.teleport(fenceLocation);
    safefence.getBlock().setType(blocksafe);
    plugin.getCommands().getPinata().put(entity, new LivingPinata(player, fenceLocation, hitch, pinata));
    entity.setCustomName(pinata.getName());
    if (entity instanceof Sheep) {
      ((Sheep) entity).setColor(pinata.getSheepColor());
    }
    entity.setLeashHolder(hitch);
    if (pinata.isBlindnessEnabled()) {
      player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, pinata.getBlindnessTime() * 20, 1));
      if (pinata.isFullBlindness()) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, pinata.getBlindnessTime() * 20, 1));
      }
    }
    //Scheduler to avoid graphical glitch
    Bukkit.getScheduler().runTaskLater(plugin, () -> entity.setLeashHolder(hitch), 20);
    return true;
  }

  /**
   * todo
   * Creates pinata at specified location using already spawned entity, name of pinata required.
   *
   * @param fenceLocation location where to spawn pinata
   * @param entity        entity that will be transformed to pinata
   * @return <b>true</b> if creation succeed, <b>false</b> if creation couldn't be completed
   */
  public static boolean createPinata(final Location fenceLocation, final LivingEntity entity, final Pinata pinata) {
    PinataCreateEvent pce = new PinataCreateEvent(entity, pinata);
    Bukkit.getPluginManager().callEvent(pce);
    if (pce.isCancelled()) {
      entity.remove();
      if (fenceLocation.getBlock().getType().equals(XMaterial.OAK_FENCE.parseMaterial())) {
        fenceLocation.getBlock().setType(Material.AIR);
      }
      return false;
    }
    if (!(fenceLocation.getBlock().getType().equals(Material.AIR))) {
      entity.remove();
      if (fenceLocation.getBlock().getType().equals(XMaterial.OAK_FENCE.parseMaterial())) {
        fenceLocation.getBlock().setType(Material.AIR);
      }
      return false;
    }
    //Max height check is to avoid problems with different server specifications
    Location safefence = new Location(fenceLocation.getWorld(), 3, fenceLocation.getWorld().getMaxHeight() - 1, 2);
    Location safestone = new Location(fenceLocation.getWorld(), 4, fenceLocation.getWorld().getMaxHeight() - 1, 2);
    Material blocksafe = safefence.getBlock().getType();
    safefence.getBlock().setType(XMaterial.OAK_FENCE.parseMaterial());
    safestone.getBlock().setType(Material.STONE);
    final LeashHitch hitch = (LeashHitch) safefence.getWorld().spawnEntity(safefence, EntityType.LEASH_HITCH);
    safestone.getBlock().setType(Material.AIR);
    fenceLocation.getBlock().setType(XMaterial.OAK_FENCE.parseMaterial());
    hitch.teleport(fenceLocation);
    safefence.getBlock().setType(blocksafe);
    plugin.getCommands().getPinata().put(entity, new LivingPinata(fenceLocation, hitch, pinata));
    entity.setCustomName(pinata.getName());
    if (entity instanceof Sheep) {
      ((Sheep) entity).setColor(pinata.getSheepColor());
    }
    entity.setLeashHolder(hitch);
    //Scheduler to avoid graphical glitch
    Bukkit.getScheduler().runTaskLater(plugin, () -> entity.setLeashHolder(hitch), 20);
    return true;
  }
}
