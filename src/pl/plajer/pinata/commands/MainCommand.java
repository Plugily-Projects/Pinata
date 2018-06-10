package pl.plajer.pinata.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import pl.plajer.pinata.Main;
import pl.plajer.pinata.creator.CreatorMenu;
import pl.plajer.pinata.pinata.LivingPinata;
import pl.plajer.pinata.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainCommand implements CommandExecutor {

    private Map<Entity, LivingPinata> pinata = new HashMap<>();
    private List<Player> users = new ArrayList<>();
    private ArgumentsManager argumentsManager;
    private Main plugin;

    public MainCommand(Main plugin, boolean register) {
        if(register) {
            this.plugin = plugin;
            argumentsManager = new ArgumentsManager(plugin);
            plugin.getCommand("pinata").setExecutor(this);
        }
    }

    public boolean isSenderPlayer(CommandSender sender) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(Utils.colorMessage("Pinata.Command.Only-Player"));
            return false;
        }
        return true;
    }

    public boolean hasPermission(CommandSender sender, String permission) {
        if(!sender.hasPermission(permission)) {
            sender.sendMessage(Utils.colorMessage("Pinata.Command.No-Permission"));
            return false;
        }
        return true;
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
        if(!hasPermission(sender, "pinata.command")) return true;
        if(args.length == 0) {
            sender.sendMessage(Utils.colorMessage("Pinata.Command.Help-Command.Header"));
            sender.sendMessage(Utils.colorMessage("Pinata.Command.Help-Command.Description"));
            return true;
        }
        if(args[0].equalsIgnoreCase("list")) {
            if(!isSenderPlayer(sender)) return true;
            if(!hasPermission(sender, "pinata.command.list")) return true;
            Utils.createPinatasGUI("Menus.List-Menu.Inventory-Name", (Player) sender);
        } else if(args[0].equalsIgnoreCase("preview")) {
            argumentsManager.openPreviewMenu(sender, args);
        } else if(args[0].equalsIgnoreCase("buy")) {
            argumentsManager.buyPinata(sender, args);
        } else if(args[0].equalsIgnoreCase("reloadconfig")) {
            argumentsManager.reloadConfig(sender);
        } else if(args[0].equalsIgnoreCase("create")) {
            argumentsManager.createPinata(sender, args);
        } else if(args[0].equalsIgnoreCase("setcrate")) {
            argumentsManager.setCrate(sender, args);
        } else if(args[0].equalsIgnoreCase("cratelist")) {
            argumentsManager.printCrateList(sender);
        } else if(args[0].equalsIgnoreCase("createnew")) {
            if(args.length == 1) {
                sender.sendMessage("Please type pinata name!");
            } else {
                argumentsManager.createNewPinata(sender, args[1]);
            }
        } else if(args[0].equalsIgnoreCase("edit")) {
            if(args.length == 1) {
                sender.sendMessage("Please type pinata name!");
            } else {
                if(plugin.getPinataManager().getPinataByName(args[1]) == null){
                    sender.sendMessage("Pinata doesn't exist!");
                    return true;
                }
                new CreatorMenu(args[1]).openInventory((Player) sender);
            }
        } else {
            sender.sendMessage(Utils.colorMessage("Pinata.Command.Help-Command.Header"));
            sender.sendMessage(Utils.colorMessage("Pinata.Command.Help-Command.Description"));
        }
        return true;
    }

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
