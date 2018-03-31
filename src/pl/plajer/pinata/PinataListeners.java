package pl.plajer.pinata;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import pl.plajer.pinata.dao.Pinata;
import pl.plajer.pinata.dao.PinataItem;
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
                        plugin.getCommands().getPinata().get(en).getBuilder().getBlock().setType(Material.AIR);
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
                    e.getPlayer().sendMessage(Utils.colorFileMessage("Other.Plugin-Up-To-Date").replaceAll("%old%", currentVersion).replaceAll("%new%", latestVersion));
                }
            } catch(Exception ex) {
                e.getPlayer().sendMessage(Utils.colorFileMessage("Other.Plugin-Update-Check-Failed").replaceAll("%error%", ex.getMessage()));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPinataDamage(EntityDamageByEntityEvent e) {
        for(Pinata pinata : plugin.getPinataManager().getPinataList()) {
            if(pinata.getName().equals(e.getEntity().getCustomName())) {
                if(plugin.getCommands().getPinata().get(e.getEntity()) != null) {
                    if(plugin.getCommands().getPinata().get(e.getEntity()).getPlayer() == null) {
                        //the type MUST be public, because pinata creator is not assigned
                        e.getEntity().getLocation().getWorld().playEffect(e.getEntity().getLocation().add(0, 1, 0), Effect.MOBSPAWNER_FLAMES, 10);
                        e.setCancelled(false);
                        return;
                    }
                    if(pinata.getPinataType() == Pinata.PinataType.PUBLIC) {
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
                            e.getDamager().sendMessage(Utils.colorFileMessage("Pinata.Not-Own"));
                            e.setCancelled(true);
                        }
                    }
                    if(plugin.getConfig().getDouble("damage-modifier") != 0.0) {
                        e.setDamage(plugin.getConfig().getDouble("damage-modifier"));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBatDamage(EntityDamageEvent e) {
        if(e.getEntityType().equals(EntityType.BAT) || (e.getEntity().getCustomName() != null && e.getEntity().getCustomName().equals(Utils.colorMessage("&6Halloween!")))) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPinataPunch(EntityDamageByEntityEvent e) {
        if(plugin.getCommands().getPinata().get(e.getEntity()) == null || !(e.getDamager() instanceof Player)) {
            return;
        }
        int i = 0;
        for(Pinata pinata : plugin.getPinataManager().getPinataList()) {
            if(pinata.getName().equals(e.getEntity().getCustomName())) {
                if(pinata.getDropType() == Pinata.DropType.DEATH) return;
                if(plugin.getCommands().getPinata().get(e.getEntity()).getPlayer() != null) {
                    //MUST be public is player is not assigned
                    if(!plugin.getCommands().getPinata().get(e.getEntity()).getPlayer().equals(e.getDamager()) && pinata.getPinataType() != Pinata.PinataType.PRIVATE) {
                        e.setCancelled(true);
                        return;
                    }
                }
                Player p = (Player) e.getDamager();
                for(PinataItem item : plugin.getPinataManager().getPinataList().get(i).getDrops()) {
                    if(ThreadLocalRandom.current().nextDouble(0.0, 100.0) < item.getDropChance()) {
                        final Item dropItem = e.getEntity().getWorld().dropItemNaturally(e.getEntity().getLocation(), new ItemStack(item.getItem()));
                        dropItem.setPickupDelay(1000);
                        if(plugin.isPluginEnabled("HolographicDisplays")) {
                            final Hologram hologram = HologramsAPI.createHologram(plugin, dropItem.getLocation().add(0.0, 1.5, 0.0));
                            hologram.appendTextLine(item.getItem().getType().name() + " x" + item.getItem().getAmount());
                            new BukkitRunnable() {
                                int ticksRun;

                                @Override
                                public void run() {
                                    ticksRun++;
                                    hologram.teleport(dropItem.getLocation().add(0.0, 1.5, 0.0));
                                    if(ticksRun > pinata.getDropViewTime() * 20) {
                                        hologram.delete();
                                        dropItem.remove();
                                        cancel();
                                    }
                                }
                            }.runTaskTimer(plugin, 1L, 1L);
                        } else {
                            Bukkit.getScheduler().runTaskLater(plugin, dropItem::remove, pinata.getDropViewTime() * 20);
                        }
                        p.sendMessage(Utils.colorFileMessage("Pinata.Drop.DropMsg").replaceAll("%item%", item.getItem().getType().name() + " x" + item.getItem().getAmount()));
                    }
                }
                i++;
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
                bat.setCustomName(Utils.colorMessage("&6Halloween!"));
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
        plugin.getCommands().getPinata().get(e.getEntity()).getBuilder().getBlock().setType(Material.AIR);
        plugin.getCommands().getPinata().get(e.getEntity()).getLeash().remove();
        final ArrayList<PinataItem> itemsToGive = new ArrayList<>();
        final Player p = e.getEntity().getKiller() instanceof Player ? e.getEntity().getKiller() : plugin.getCommands().getPinata().get(e.getEntity()).getPlayer();
        //drops won't show if killer is environment and pinata player is not assigned. This pinata will be always in our hearts [*]
        if(p == null) return;
        if(plugin.getConfig().getBoolean("blindness-effect")) {
            if(p.hasPotionEffect(PotionEffectType.BLINDNESS)) {
                p.removePotionEffect(PotionEffectType.BLINDNESS);
            }
            if(plugin.getConfig().getBoolean("full-blindness-effect")) {
                if(p.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
                    p.removePotionEffect(PotionEffectType.NIGHT_VISION);
                }
            }
        }
        int i = 0;
        for(Pinata pinata : plugin.getPinataManager().getPinataList()) {
            if(pinata.getName().equals(e.getEntity().getCustomName())) {
                for(PinataItem item : plugin.getPinataManager().getPinataList().get(i).getDrops()) {
                    if(ThreadLocalRandom.current().nextDouble(0.0, 100.0) < item.getDropChance()) {
                        final Item dropItem = e.getEntity().getWorld().dropItemNaturally(e.getEntity().getLocation(), new ItemStack(item.getItem()));
                        dropItem.setPickupDelay(1000);
                        if(plugin.isPluginEnabled("HolographicDisplays")) {
                            final Hologram hologram = HologramsAPI.createHologram(plugin, dropItem.getLocation().add(0.0, 1.5, 0.0));
                            hologram.appendTextLine(item.getItem().getType().name() + " x" + item.getItem().getAmount());
                            new BukkitRunnable() {
                                int ticksRun;

                                @Override
                                public void run() {
                                    ticksRun++;
                                    hologram.teleport(dropItem.getLocation().add(0.0, 1.5, 0.0));
                                    if(ticksRun > pinata.getDropViewTime() * 20) {
                                        hologram.delete();
                                        dropItem.remove();
                                        cancel();
                                    }
                                }
                            }.runTaskTimer(plugin, 1L, 1L);
                        } else {
                            Bukkit.getScheduler().runTaskLater(plugin, dropItem::remove, pinata.getDropViewTime() * 20);
                        }
                        if(item.getItem().hasItemMeta() && item.getItem().getItemMeta().hasDisplayName()) {
                            p.sendMessage(Utils.colorFileMessage("Pinata.Drop.DropMsg").replaceAll("%item%", item.getItem().getItemMeta().getDisplayName()).replaceAll("%amount%", String.valueOf(item.getItem().getAmount())));
                        } else {
                            p.sendMessage(Utils.colorFileMessage("Pinata.Drop.DropMsg").replaceAll("%item%", item.getItem().getType().name()).replaceAll("%amount%", String.valueOf(item.getItem().getAmount())));
                        }
                        p.getInventory().addItem(item.getItem());
                        itemsToGive.add(item);
                        i++;
                    }
                }
            }
        }
        PinataDeathEvent pde = new PinataDeathEvent(e.getEntity().getKiller(), e.getEntity(), e.getEntity().getCustomName(), itemsToGive);
        Bukkit.getPluginManager().callEvent(pde);
        if(i == 0) {
            p.sendMessage(Utils.colorFileMessage("Pinata.Drop.No-Drops"));
        }
        plugin.getCommands().getPinata().remove(e.getEntity());
        itemsToGive.clear();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if(e.getInventory() == null || e.getInventory().getName() == null) return;
        if(e.getInventory().getName().contains(Utils.colorMessage("&lEdit items of pinata "))) {
            if(e.getInventory().firstEmpty() == 0) {
                e.getPlayer().sendMessage(Utils.colorFileMessage("Menus.Editor-Menu.No-Items-No-Save"));
                return;
            }
            List<ItemStack> items = new ArrayList<>();
            for(ItemStack is : e.getInventory().getContents()) {
                if(is == null) continue;
                if(!is.hasItemMeta() || !is.getItemMeta().hasLore()) continue;
                if(!is.getItemMeta().getLore().get(is.getItemMeta().getLore().size() - 1).matches(".*\\d+.*")) continue;
                ItemMeta meta = is.getItemMeta();
                List<String> lore = meta.getLore();
                double chance = Double.valueOf(lore.get(lore.size() - 1));
                lore.remove(lore.size() - 1);
                lore.add(String.valueOf(chance));
                items.add(is);
            }
            if(items.size() == 0) {
                e.getPlayer().sendMessage(Utils.colorFileMessage("Menus.Editor-Menu.No-Items-No-Save"));
                return;
            }
            String pinata = e.getInventory().getName().replaceAll(Utils.colorMessage("&lEdit items of pinata "), "");
            plugin.getFileManager().getPinataConfig().set("Pinatas." + pinata + ".Drops", items);
            plugin.getFileManager().savePinataConfig();
            e.getPlayer().sendMessage(Utils.colorFileMessage("Menus.Editor-Menu.Items-Saved").replaceAll("%pinata%", pinata));
        }
    }
}
