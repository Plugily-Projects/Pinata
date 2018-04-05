package pl.plajer.pinata;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.sun.deploy.util.UpdateCheck;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
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
import pl.plajer.pinata.dao.PinataExtendedData;
import pl.plajer.pinata.dao.PinataData;
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
                if(en.hasMetadata("PinataEntity")) {
                    //TODO test player quit need to remove PinataCreated metadata?
                    PinataData data = (PinataData) en.getMetadata("PinataData").get(0).value();
                    data.getLeash().remove();
                    data.getBuilder().getBlock().setType(Material.AIR);
                    en.remove();
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
            switch(UpdateChecker.checkUpdate()){
                case STABLE:
                    e.getPlayer().sendMessage(Utils.colorFileMessage("Other.Plugin-Up-To-Date").replaceAll("%old%", currentVersion).replaceAll("%new%", UpdateChecker.getLatestVersion()));
                    break;
                case BETA:
                    e.getPlayer().sendMessage(Utils.colorFileMessage("Other.Plugin-Up-To-Date").replaceAll("%old%", currentVersion).replaceAll("%new%", UpdateChecker.getLatestVersion()));
                    //todo beta
                    break;
                case ERROR:
                    e.getPlayer().sendMessage(Utils.colorFileMessage("Other.Plugin-Update-Check-Failed"));
                    break;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPinataDamage(EntityDamageByEntityEvent e) {
        if(!e.getEntity().hasMetadata("PinataEntity")) return;
        PinataData data = (PinataData) e.getEntity().getMetadata("PinataData").get(0).value();
        for(PinataExtendedData pinata : plugin.getPinataManager().getPinataList()) {
            if(pinata.getName().equals(e.getEntity().getCustomName())) {
                if(data.getPlayer() == null) {
                    //the type MUST be public, because pinata creator is not assigned
                    e.getEntity().getLocation().getWorld().playEffect(e.getEntity().getLocation().add(0, 1, 0), Effect.MOBSPAWNER_FLAMES, 10);
                    e.setCancelled(false);
                    return;
                }
                if(pinata.getPinataType() == PinataExtendedData.PinataType.PUBLIC) {
                    e.getEntity().getLocation().getWorld().playEffect(e.getEntity().getLocation().add(0, 1, 0), Effect.MOBSPAWNER_FLAMES, 10);
                    //override World Guard blocking
                    e.setCancelled(false);
                } else /* the type is private */ {
                    if(data.getPlayer().equals(e.getDamager())) {
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

    @EventHandler
    public void onBatDamage(EntityDamageEvent e) {
        if(e.getEntityType().equals(EntityType.BAT) || (e.getEntity().getCustomName() != null && e.getEntity().getCustomName().equals(Utils.colorMessage("&6Halloween!")))) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPinataPunch(final EntityDamageByEntityEvent e) {
        if(!e.getEntity().hasMetadata("PinataEntity") || !(e.getDamager() instanceof Player)) return;
        PinataData data = (PinataData) e.getEntity().getMetadata("PinataData").get(0).value();
        PinataExtendedData extendedData = (PinataExtendedData) e.getEntity().getMetadata("PinataExtendedData").get(0).value();
        if(extendedData.getName().equals(e.getEntity().getCustomName())) {
            if(extendedData.getDropType() == PinataExtendedData.DropType.DEATH) return;
            if(data.getPlayer() != null) {
                //MUST be public is player is not assigned
                if(!data.getPlayer().equals(e.getDamager()) && extendedData.getPinataType() != PinataExtendedData.PinataType.PRIVATE) {
                    e.setCancelled(true);
                    return;
                }
            }
            Player p = (Player) e.getDamager();
            for(PinataItem item : extendedData.getDrops()) {
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
                                if(ticksRun > extendedData.getDropViewTime() * 20) {
                                    hologram.delete();
                                    dropItem.remove();
                                    cancel();
                                }
                            }
                        }.runTaskTimer(plugin, 1L, 1L);
                    } else {
                        Bukkit.getScheduler().runTaskLater(plugin, dropItem::remove, extendedData.getDropViewTime() * 20);
                    }
                    p.sendMessage(Utils.colorFileMessage("Pinata.Drop.DropMsg").replaceAll("%item%", item.getItem().getType().name() + " x" + item.getItem().getAmount()));
                }
            }
        }
    }

    @EventHandler
    public void onPinataDeath(final EntityDeathEvent e) {
        if(!e.getEntity().hasMetadata("PinataEntity")) return;
        PinataData data = (PinataData) e.getEntity().getMetadata("PinataData").get(0).value();
        PinataExtendedData extendedData = (PinataExtendedData) e.getEntity().getMetadata("PinataExtendedData").get(0).value();
        if(data.getPlayer() != null) {
            if(data.getPlayer().hasMetadata("PinataCreated")){
                data.getPlayer().removeMetadata("PinataCreated", plugin);
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
        data.getBuilder().getBlock().setType(Material.AIR);
        data.getLeash().remove();
        final ArrayList<PinataItem> itemsToGive = new ArrayList<>();
        final Player p = e.getEntity().getKiller() instanceof Player ? e.getEntity().getKiller() : data.getPlayer();
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
        if(extendedData.getName().equals(e.getEntity().getCustomName())) {
            for(PinataItem item : extendedData.getDrops()) {
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
                                if(ticksRun > extendedData.getDropViewTime() * 20) {
                                    hologram.delete();
                                    dropItem.remove();
                                    cancel();
                                }
                            }
                        }.runTaskTimer(plugin, 1L, 1L);
                    } else {
                        Bukkit.getScheduler().runTaskLater(plugin, dropItem::remove, extendedData.getDropViewTime() * 20);
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
        PinataDeathEvent pde = new PinataDeathEvent(e.getEntity().getKiller(), e.getEntity(), e.getEntity().getCustomName(), itemsToGive);
        Bukkit.getPluginManager().callEvent(pde);
        if(i == 0) {
            p.sendMessage(Utils.colorFileMessage("Pinata.Drop.No-Drops"));
        }
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
            FileConfiguration config = plugin.getFileManager().getFile("pinatas");
            config.set("Pinatas." + pinata + ".Drops", items);
            plugin.getFileManager().saveFile(config, "pinatas");
            e.getPlayer().sendMessage(Utils.colorFileMessage("Menus.Editor-Menu.Items-Saved").replaceAll("%pinata%", pinata));
        }
    }
}
