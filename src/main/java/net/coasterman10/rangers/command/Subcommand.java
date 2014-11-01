package net.coasterman10.rangers.command;

import org.bukkit.command.CommandSender;

public interface Subcommand {
    /**
     * @return The label of this subcommand.
     */
    public String getName();

    /**
     * @return The description of this subcommand.
     */
    public String getDescription();

    /**
     * Returns the arguments for the subcommand. The format is as follows:
     * <ul>
     * <li><b>Mandatory Arguments</b> &lt;argument&gt;</li>
     * <li><b>Optional Arguments</b> [argument]
     * <li><b>Multiple-Choice Arguments</b> &lt;argument1|argument2|...&gt; <b>or</b> [argument1|argument2|...]
     * </ul>
     * 
     * @return The arguments expected for the subcommand.
     */
    public String getArguments();

    /**
     * @return The permission necessary to execute this subcommand, or null if no permissions are necessary.
     */
    public String getPermission();

    /**
     * @return False if the command may only be executed by a Player.
     */
    public boolean canConsoleUse();

    /**
     * Executes the subcommand. If canConsoleUse() returns false, this is guaranteed to execute only if the sender is a
     * Player.
     * 
     * @param sender The CommandSender that invoked this subcommand
     * @param args The arguments provided by the CommandSender to this subcommand, <i>not including the label</i>.
     * @return False if the arguments did not satisfy the usage for the command, otherwise true.
     */
    public boolean execute(CommandSender sender, String[] args);
}
