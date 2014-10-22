package net.coasterman10.rangers.command.arena;

import net.coasterman10.rangers.Rangers;
import net.coasterman10.rangers.arena.ArenaManager;
import net.coasterman10.rangers.command.Subcommand;
import net.coasterman10.rangers.game.Game;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ArenaSignCommand implements Subcommand {

    private Rangers plugin;
    private ArenaManager manager;

    public ArenaSignCommand(Rangers r, ArenaManager m) {
        this.plugin = r;
        this.manager = m;
    }

    @Override
    public String getName() {
        return "sign";
    }

    @Override
    public String getDescription() {
        return "Sets the sign that you are looking at to the designated arena, and the sign 1 block below that to the status if the --generateStatus argument is used.";
    }

    @Override
    public String getArguments() {
        return ChatColor.GREEN + "<id> [--perm] [--generateStatus] [-d/--delete]";
    }

    @Override
    public boolean canConsoleUse() {
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1 || args.length > 3/*change to max args*/)
            return false;
        if (!manager.getArenas().contains(manager.getArena(args[0]))) {
            sender.sendMessage(ChatColor.RED + "The specified arena (" + args[0] + ") cannot be found!");
            return true;
        }
        List<String> argList = Arrays.asList(args);
        if (sender instanceof Player) {
            FileConfiguration fileConfig = plugin.getConfig();
            if (argList.contains("-d") || argList.contains("--delete")) {
                fileConfig.set("signs." + args[0], null);
            }
            @SuppressWarnings("deprecation")
            Location joinSign = ((Player) sender).getTargetBlock(null, 50).getLocation();
            Game g = new Game(plugin, plugin.settings);        //
            g.setArena(plugin.arenaManager.getArena(args[0])); // Register these now so we don't have to reload
            plugin.signManager.addJoinSign(g, joinSign);
            if (argList.contains("--generateStatus")) {
                Location statusSign = joinSign.subtract(0, 1, 0);
                plugin.signManager.addStatusSign(g, statusSign);
            }
            sender.sendMessage(ChatColor.GREEN + "Sign created for arena " + args[0]);
            if (argList.contains("--perm") && !plugin.getConfig().contains("signs." + args[0])) {//Save this sign to the configuration
                fileConfig.set("signs." + args[0] + ".arena", args[0]);
                fileConfig.set("signs." + args[0] + ".join.x", joinSign.getBlockX());
                fileConfig.set("signs." + args[0] + ".join.y", joinSign.getBlockY());
                fileConfig.set("signs." + args[0] + ".join.z", joinSign.getBlockZ());
                if (argList.contains("--generateStatus")) {
                    Location statusSign = joinSign.subtract(0, 1, 0);
                    fileConfig.set("signs." + args[0] + ".status.x", statusSign.getBlockX());
                    fileConfig.set("signs." + args[0] + ".status.y", statusSign.getBlockY());
                    fileConfig.set("signs." + args[0] + ".status.z", statusSign.getBlockZ());
                }
            }
            try {
                fileConfig.save(new File(plugin.getDataFolder(), "config.yml"));
                sender.sendMessage(ChatColor.GREEN + "Sucessfully saved sign configuration for arena " + args[0]);
            } catch (IOException e) {
                e.printStackTrace();
                sender.sendMessage(ChatColor.DARK_RED + "Error saving configuration:");
                sender.sendMessage(ChatColor.RED + e.getMessage());
            }

        }
        return true;
    }
}
