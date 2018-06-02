package pl.plajer.pinata;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.shampaggon.crackshot.CSUtility;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import pl.plajer.pinata.pinata.PinataItem;
import pl.plajer.pinata.pinataapi.PinataDeathEvent;
import pl.plajer.pinata.utils.UpdateChecker;
import pl.plajer.pinata.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

class PinataListeners implements Listener {

    private Main plugin;

    PinataListeners(Main plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        for(Entity en : Bukkit.getServer().getWorld(e.getPlayer().getWorld().getName()).getEntities()) {
            if(en instanceof Sheep) {
                if(plugin.getCommands().getPinata().containsKey(en)) {
                    if(plugin.getCommands().getPinata().get(en).getPlayer().equals(e.getPlayer())) {
                        plugin.getCommands().getPinata().get(en).getFence().getBlock().setType(Material.AIR);
                        plugin.getCommands().getPinata().get(en).getLeash().remove();
                        en.remove();
                        plugin.getCommands().getPinata().remove(en);
                        if(plugin.getCommands().getUsers().contains(e.getPlayer())) {
                            plugin.getCommands().getUsers().remove(e.getPlayer());
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if(!e.getPlayer().hasPermission("pinata.admin.notify")) {
            return;
        }
        String currentVersion = "v" + Bukkit.getPluginManager().getPlugin("Pinata").getDescription().getVersion();
        if(plugin.getConfig().getBoolean("update-notify")) {
            try {
                UpdateChecker.checkUpdate(currentVersion);
                String latestVersion = UpdateChecker.getLatestVersion();
                if(latestVersion != null) {
                    latestVersion = "v" + latestVersion;
                    e.getPlayer().sendMessage(Utils.colorMessage("Other.Plugin-Up-To-Date").replaceAll("%old%", currentVersion).replaceAll("%new%", latestVersion));
                }
            } catch(Exception ex) {
                e.getPlayer().sendMessage(Utils.colorMessage("Other.Plugin-Update-Check-Failed").replaceAll("%error%", ex.getMessage()));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPinataDamage(EntityDamageByEntityEvent e) {
        if(plugin.getPinataManager().getPinataList().contains(e.getEntity().getCustomName())) {
            if(plugin.getCommands().getPinata().get(e.getEntity()) != null) {
                if(plugin.getCommands().getPinata().get(e.getEntity()).getPlayer() == null) {
                    //the type MUST be public, because pinata creator is not assigned
                    e.getEntity().getLocation().getWorld().playEffect(e.getEntity().getLocation().add(0, 1, 0), Effect.MOBSPAWNER_FLAMES, 10);
                    e.setCancelled(false);
                    return;
                }
                if(ConfigurationManager.getConfig("pinatas").get("pinatas." + e.getEntity().getCustomName() + ".type").equals("public")) {
                    e.getEntity().getLocation().getWorld().playEffect(e.getEntity().getLocation().add(0, 1, 0), Effect.MOBSPAWNER_FLAMES, 10);
                    //override World Guard blocking
                    e.setCancelled(false);
                } else /* the type is private */ {
                    if(plugin.getCommands().getPinata().get(e.getEntity()).getPlayer().equals(e.getDamager())) {
                        if(plugin.getConfig().getBoolean("halloween-mode")) {
                            if(!Bukkit.getServer().getVersion().contains("1.8")) {
                                e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.ENTITY_GHAST_HURT, 1, 1);
                            }
                        }
                        e.getEntity().getLocation().getWorld().playEffect(e.getEntity().getLocation().add(0, 1, 0), Effect.MOBSPAWNER_FLAMES, 10);
                        e.setCancelled(false);
                    } else {
                        e.getDamager().sendMessage(Utils.colorMessage("Pinata.Not-Own"));
                        e.setCancelled(true);
                    }
                }
                if(plugin.getConfig().getDouble("damage-modifier") != 0.0) {
                    e.setDamage(plugin.getConfig().getDouble("damage-modifier"));
                }
            }
        }
    }

    @EventHandler
    public void onBatDamage(EntityDamageEvent e) {
        if(e.getEntityType().equals(EntityType.BAT) || (e.getEntity().getCustomName() != null && e.getEntity().getCustomName().equals(Utils.colorRawMessage("&6Halloween!")))) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPinataPunch(EntityDamageByEntityEvent e) {
        if(plugin.getCommands().getPinata().get(e.getEntity()) == null || !(e.getDamager() instanceof Player)) {
            return;
        }
        if(!ConfigurationManager.getConfig("pinatas").get("pinatas." + e.getEntity().getCustomName() + ".drop-type").toString().toLowerCase().equals("punch")) {
            return;
        }
        if(plugin.getCommands().getPinata().get(e.getEntity()).getPlayer() != null) {
            //MUST be public is player is not assigned
            if(!plugin.getCommands().getPinata().get(e.getEntity()).getPlayer().equals(e.getDamager()) && ConfigurationManager.getConfig("pinatas").get("pinatas." + e.getEntity().getCustomName() + ".type").equals("private")) {
                e.setCancelled(true);
                return;
            }
        }
        final int timer = ConfigurationManager.getConfig("pinatas").getInt("pinatas." + e.getEntity().getCustomName() + ".timer");
        Player p = (Player) e.getDamager();
        for(PinataItem item : plugin.getPinataManager().getPinataDrop().get(e.getEntity().getCustomName())) {
            if(ThreadLocalRandom.current().nextDouble(0.0, 100.0) < item.getDropChance()) {
                final Item dropItem = e.getEntity().getWorld().dropItemNaturally(e.getEntity().getLocation(), new ItemStack(item.getRepresentedMaterial()));
                dropItem.setPickupDelay(1000);
                if(plugin.isPluginEnabled("HolographicDisplays")) {
                    final Hologram hologram = HologramsAPI.createHologram(plugin, dropItem.getLocation().add(0.0, 1.5, 0.0));
                    hologram.appendTextLine(Utils.colorRawMessage(item.getHologramName().replaceAll("%player%", p.getName())) + " x" + item.getAmount());
                    new BukkitRunnable() {
                        int ticksRun;

                        @Override
                        public void run() {
                            ticksRun++;
                            hologram.teleport(dropItem.getLocation().add(0.0, 1.5, 0.0));
                            if(ticksRun > timer * 20) {
                                hologram.delete();
                                dropItem.remove();
                                cancel();
                            }
                        }
                    }.runTaskTimer(plugin, 1L, 1L);
                } else {
                    Bukkit.getScheduler().runTaskLater(plugin, dropItem::remove, timer * 20);
                }
                switch(item.getItemType()) {
                    case ITEM:
                        p.getInventory().addItem(item.getItem());
                        break;
                    case COMMAND:
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), item.getCommand().replaceAll("%player%", p.getName()));
                        break;
                    case GUN:
                        CSUtility shot = new CSUtility();
                        shot.giveWeapon(p, item.getGunName(), 1);
                        break;
                    case MONEY:
                        plugin.getEco().depositPlayer(Bukkit.getOfflinePlayer(p.getUniqueId()), item.getMoneyValue());
                        break;
                }
                p.sendMessage(Utils.colorMessage("Pinata.Drop.DropMsg").replaceAll("%item%", item.getHologramName()).replaceAll("%amount%", String.valueOf(item.getAmount())));
            }
        }
    }

    @EventHandler
    public void onPinataDeath(final EntityDeathEvent e) {
        if(plugin.getCommands().getPinata().get(e.getEntity()) == null) {
            return;
        }
        if(plugin.getCommands().getPinata().get(e.getEntity()).getPlayer() != null) {
            if(plugin.getCommands().getUsers().contains(plugin.getCommands().getPinata().get(e.getEntity()).getPlayer())) {
                List<Player> users = new ArrayList<>(plugin.getCommands().getUsers());
                users.remove(plugin.getCommands().getPinata().get(e.getEntity()).getPlayer());
                plugin.getCommands().setUsers(users);
                users.clear();
            }
        }
        if(plugin.getConfig().getBoolean("halloween-mode")) {
            e.getEntity().getWorld().strikeLightningEffect(e.getEntity().getLocation());
            Random r = new Random();
            if(Bukkit.getServer().getVersion().contains("1.8")) {
                if(r.nextBoolean()) {
                    e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.valueOf("WOLF_HOWL"), 1, 1);
                } else {
                    e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.valueOf("WITHER_DEATH"), 1, 1);
                }
            } else {
                if(r.nextBoolean()) {
                    e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.ENTITY_WOLF_HOWL, 1, 1);
                } else {
                    e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.ENTITY_WITHER_DEATH, 1, 1);
                }
            }
            final ArrayList<Entity> bats = new ArrayList<>();
            for(int i = 0; i < 5; i++) {
                final Entity bat = e.getEntity().getWorld().spawnEntity(e.getEntity().getLocation(), EntityType.BAT);
                bat.setCustomName(Utils.colorRawMessage("&6Halloween!"));
                bats.add(bat);
            }
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for(Entity bat : bats) {
                    bat.getWorld().playEffect(bat.getLocation(), Effect.SMOKE, 3);
                    bat.remove();
                }
                bats.clear();
            }, 30);
        }
        e.getEntity().setLeashHolder(null);
        e.getEntity().getLocation().getWorld().playEffect(e.getEntity().getLocation().add(0, 1, 0), Effect.POTION_BREAK, 10);
        e.getDrops().clear();
        e.setDroppedExp(0);
        plugin.getCommands().getPinata().get(e.getEntity()).getFence().getBlock().setType(Material.AIR);
        plugin.getCommands().getPinata().get(e.getEntity()).getLeash().remove();
        final ArrayList<Item> itemsToGive = new ArrayList<>();
        final Player p = e.getEntity().getKiller() instanceof Player ? e.getEntity().getKiller() : plugin.getCommands().getPinata().get(e.getEntity()).getPlayer();
        //drops won't show if killer is environment and pinata player is not assigned. This pinata will be always in our hearts [*]
        if(p == null) return;
        if(ConfigurationManager.getConfig("pinatas").getBoolean("pinatas." + e.getEntity().getCustomName() + ".blindness-effect")) {
            if(p.hasPotionEffect(PotionEffectType.BLINDNESS)) {
                p.removePotionEffect(PotionEffectType.BLINDNESS);
            }
            if(ConfigurationManager.getConfig("pinatas").getBoolean("pinatas." + e.getEntity().getCustomName() + ".full-blindness-effect")) {
                if(p.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
                    p.removePotionEffect(PotionEffectType.NIGHT_VISION);
                }
            }
        }
        final int timer = ConfigurationManager.getConfig("pinatas").getInt("pinatas." + e.getEntity().getCustomName() + ".timer");
        int i = 0;
        List<PinataItem> items = new ArrayList<>();
        for(PinataItem item : plugin.getPinataManager().getPinataDrop().get(e.getEntity().getCustomName())) {
            if(ThreadLocalRandom.current().nextDouble(0.0, 100.0) < item.getDropChance()) {
                final Item dropItem = e.getEntity().getWorld().dropItemNaturally(e.getEntity().getLocation(), new ItemStack(item.getRepresentedMaterial()));
                dropItem.setPickupDelay(1000);
                if(plugin.isPluginEnabled("HolographicDisplays")) {
                    final Hologram hologram = HologramsAPI.createHologram(plugin, dropItem.getLocation().add(0.0, 1.5, 0.0));
                    hologram.appendTextLine(Utils.colorRawMessage(item.getHologramName().replaceAll("%player%", p.getName())) + " x" + item.getAmount());
                    new BukkitRunnable() {
                        int ticksRun;

                        @Override
                        public void run() {
                            ticksRun++;
                            hologram.teleport(dropItem.getLocation().add(0.0, 1.5, 0.0));
                            if(ticksRun > timer * 20) {
                                hologram.delete();
                                dropItem.remove();
                                cancel();
                            }
                        }
                    }.runTaskTimer(plugin, 1L, 1L);
                } else {
                    Bukkit.getScheduler().runTaskLater(plugin, dropItem::remove, timer * 20);
                }
                switch(item.getItemType()) {
                    case ITEM:
                        p.getInventory().addItem(item.getItem());
                        break;
                    case COMMAND:
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), item.getCommand().replaceAll("%player%", p.getName()));
                        break;
                    case GUN:
                        CSUtility shot = new CSUtility();
                        shot.giveWeapon(p, item.getGunName(), 1);
                        break;
                    case MONEY:
                        plugin.getEco().depositPlayer(Bukkit.getOfflinePlayer(p.getUniqueId()), item.getMoneyValue());
                        break;
                }
                p.sendMessage(Utils.colorMessage("Pinata.Drop.DropMsg").replaceAll("%item%", item.getHologramName()).replaceAll("%amount%", String.valueOf(item.getAmount())));
            }
            items.add(item);
            i++;
        }
        PinataDeathEvent pde = new PinataDeathEvent(e.getEntity().getKiller(), e.getEntity(), e.getEntity().getCustomName(), items);
        Bukkit.getPluginManager().callEvent(pde);
        if(i == 0) {
            p.sendMessage(Utils.colorMessage("Pinata.Drop.No-Drops"));
        }
        plugin.getCommands().getPinata().remove(e.getEntity());
        itemsToGive.clear();
    }
}
