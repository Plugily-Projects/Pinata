package pl.plajer.pinata.commands.arguments.admin;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import pl.plajer.pinata.Main;
import pl.plajer.pinata.api.PinataFactory;
import pl.plajer.pinata.commands.arguments.ArgumentsRegistry;
import pl.plajer.pinata.commands.arguments.data.CommandArgument;
import pl.plajer.pinata.commands.arguments.data.LabelData;
import pl.plajer.pinata.commands.arguments.data.LabeledCommandArgument;
import pl.plajer.pinata.pinata.Pinata;
import pl.plajer.pinata.utils.Utils;

public class CreateArgument {

    public CreateArgument(ArgumentsRegistry registry) {
      registry.mapArgument("pinata", new LabeledCommandArgument("create", "pinata.admin.create", CommandArgument.ExecutorType.PLAYER,
          new LabelData("/pinata create", "/pinata create", "&7Create a Pinata\n&6Permission: &7pinata.admin.create")) {
          @Override
          public void execute(CommandSender sender, String[] args) {
            if (args.length == 6) {
              //custom location is used
              try {
                Random r = new Random();
                World world = Bukkit.getWorld(args[1]);
                int x, y, z;
                if (args[2].contains("~")) {
                  String[] rand = args[2].split("~");
                  Integer randomNum = r.nextInt(Integer.parseInt(rand[1]) - Integer.parseInt(rand[0]));
                  x = Integer.parseInt(rand[0] + randomNum);
                } else {
                  x = Integer.parseInt(args[2]);
                }
                if (args[3].contains("~")) {
                  String[] rand = args[3].split("~");
                  Integer randomNum = r.nextInt(Integer.parseInt(rand[1]) - Integer.parseInt(rand[0]));
                  y = Integer.parseInt(rand[0] + randomNum);
                } else {
                  y = Integer.parseInt(args[3]);
                }
                if (args[4].contains("~")) {
                  String[] rand = args[4].split("~");
                  Integer randomNum = r.nextInt(Integer.parseInt(rand[1]) - Integer.parseInt(rand[0]));
                  z = Integer.parseInt(rand[0] + randomNum);
                } else {
                  z = Integer.parseInt(args[4]);
                }
                Pinata pinata = registry.getPlugin().getPinataManager().getPinataByName(args[5]);
                if (pinata == null) {
                  sender.sendMessage(Utils.colorMessage("Pinata.Not-Found"));
                  return;
                }
                Location l = new Location(world, x, y, z);
                LivingEntity entity = (LivingEntity) l.getWorld().spawnEntity(l.clone().add(0, 2, 0), pinata.getEntityType());
                entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(pinata.getHealth());
                entity.setHealth(entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
                PinataFactory.createPinata(l.clone().add(0, 7, 0), entity, pinata);
                sender.sendMessage(Utils.colorMessage("Pinata.Create.Success").replace("%name%", args[5]));
                return;
              } catch (Exception e) {
                sender.sendMessage(Utils.colorMessage("Pinata.Command.Custom-Location-Create-Error"));
                return;
              }
            }
            if (args.length == 1) {
              sender.sendMessage(Utils.colorMessage("Pinata.Specify-Name"));
              return;
            }
            Player user;
            if (sender instanceof ConsoleCommandSender) {
              if (args.length != 3) {
                sender.sendMessage(Utils.colorMessage("Pinata.Command.Console-Specify-Player"));
                return;
              }
              if (Bukkit.getPlayer(args[2]) != null) {
                user = Bukkit.getPlayer(args[2]);
                user.sendMessage(Utils.colorMessage("Pinata.Create.By-Console"));
              } else {
                sender.sendMessage(Utils.colorMessage("Pinata.Command.Player-Not-Found"));
                return;
              }
            } else {
              if (!sender.hasPermission("pinata.admin.create.others")) {
                return;
              }
              if (args.length != 3) {
                user = (Player) sender;
              } else {
                if (Bukkit.getPlayer(args[2]) != null) {
                  user = Bukkit.getPlayer(args[2]);
                } else {
                  sender.sendMessage(Utils.colorMessage("Pinata.Command.Player-Not-Found"));
                  return;
                }
              }
            }
            Pinata pinata = registry.getPlugin().getPinataManager().getPinataByName(args[1]);
            if (pinata == null) {
              sender.sendMessage(Utils.colorMessage("Pinata.Not-Found"));
              return;
            }
            Utils.createPinataAtPlayer(user, user.getLocation(), pinata);
          }
        });
    }

  }

