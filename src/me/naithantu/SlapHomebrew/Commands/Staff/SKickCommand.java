package me.naithantu.SlapHomebrew.Commands.Staff;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.Util.Util;

public class SKickCommand extends AbstractCommand {

	public SKickCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	@Override
	public boolean handle() throws CommandException {
		testPermission("skick"); //Test perm
		if (args.length < 1) throw new UsageException("sKick [player] <Reason>"); //Usage
		
		Player player = getOnlinePlayer(args[0], false); //Get the player to be kicked
		String reason = (args.length == 1 ? "You have been kicked!" : Util.buildString(args, " ", 1)); //Build reason (if reason given)
		
		player.kickPlayer(reason); //Kick player
		hMsg(player.getName() + " has been kicked"); 
		return true;
	}
	
}
