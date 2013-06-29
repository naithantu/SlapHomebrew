package me.naithantu.SlapHomebrew.Commands;

import me.naithantu.SlapHomebrew.AwayFromKeyboard;
import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.command.CommandSender;

public class AfkCommand extends AbstractCommand {
	
	private AwayFromKeyboard afk;
	
	public AfkCommand(CommandSender sender, String[] args, SlapHomebrew plugin, AwayFromKeyboard afk) {
		super(sender, args, plugin);
		this.afk = afk;
	}

	public boolean handle() {
		if (!afk.isAfk(sender.getName())) {
			//Player currently not AFK -> Go AFK
			if (args.length == 0) {
				//No reason
				afk.goAfk(sender.getName(), "AFK");
			} else if (args.length > 0) {
				//With reason
				String reason = null;
				for (String arg : args) {
					if (reason == null) {
						reason = arg;
					} else {
						reason = reason + " " + arg;
					}
				}
				afk.goAfk(sender.getName(), reason);
			}
		} else {
			//Player AFK -> Leave AFK
			afk.leaveAfk(sender.getName());
		}
		return true;
	}
}