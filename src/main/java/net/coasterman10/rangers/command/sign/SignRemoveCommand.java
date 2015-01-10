package net.coasterman10.rangers.command.sign;

import net.coasterman10.rangers.command.Subcommand;
import net.coasterman10.rangers.listeners.SignManager;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SignRemoveCommand implements Subcommand {
    private final SignManager signManager;

    public SignRemoveCommand(SignManager signManager) {
        this.signManager = signManager;
    }

    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String getDescription() {
        return "Deletes signs for arenas";
    }

    @Override
    public String getArguments() {
        return "";
    }

    @Override
    public String getPermission() {
        return "rangers.arena.build";
    }

    @Override
    public boolean canConsoleUse() {
        return false;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length > 0) {
            return false;
        } else {
            @SuppressWarnings("deprecation")
            Location sign = ((Player) sender).getTargetBlock(null, 50).getLocation();
            if (sign == null
                    || (sign.getBlock().getType() != Material.WALL_SIGN && sign.getBlock().getType() != Material.SIGN_POST)) {
                sender.sendMessage(ChatColor.RED + "You must be targeting a sign.");
            } else {
                signManager.removeSign(sign);
            }
            return true;
        }
    }
}
