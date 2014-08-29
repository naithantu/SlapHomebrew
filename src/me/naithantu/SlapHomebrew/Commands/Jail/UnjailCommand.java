package me.naithantu.SlapHomebrew.Commands.Jail;

import java.util.List;

import me.naithantu.SlapHomebrew.PlayerExtension.UUIDControl;
import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.ErrorMsg;
import me.naithantu.SlapHomebrew.Controllers.Jails;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class UnjailCommand extends AbstractCommand {
	
	public UnjailCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		testPermission("jail"); //Test perm
		if (args.length != 1) return false; //Check usage
		
		//Get jails controller
		Jails jails = plugin.getJails();

        //Get player
		UUIDControl.UUIDProfile offPlayer = getOfflinePlayer(args[0]);

        //Check if jailed
        if (!jails.isJailed(offPlayer.getUUID())) throw new CommandException(ErrorMsg.notInJail);

        //Unjail the player
        Boolean unjailed = jails.unjailPlayer(offPlayer);

        if (unjailed == null) {
            hMsg("Unjailed " + offPlayer.getCurrentName() + " (will be released on next login)");
        } else if (unjailed) {
            hMsg("Unjailed " + offPlayer.getCurrentName());
        } else {
            //Shouldn't be reached.
            throw new CommandException(ErrorMsg.notInJail);
        }

		return true;
	}

}
