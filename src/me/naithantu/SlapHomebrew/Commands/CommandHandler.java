package me.naithantu.SlapHomebrew.Commands;

import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CommandHandler {
	SlapHomebrew plugin;

	public CommandHandler(SlapHomebrew plugin) {
		this.plugin = plugin;
	}

	public boolean handle(CommandSender sender, Command cmd, String[] args) {
		String command = cmd.getName().toLowerCase();
		AbstractCommand commandObj = null;
		if (command.equals("backdeath")) {
			commandObj = new BackdeathCommand(sender, args, plugin);
		} else if (command.equals("blockfaq")) {
			commandObj = new BlockfaqCommand(sender, args, plugin);
		} else if (command.equals("boat")) {
			commandObj = new BoatCommand(sender, args, plugin);
		} else if (command.equals("bumpdone")) {
			commandObj = new BumpdoneCommand(sender, args, plugin);
		} else if (command.equals("cakedefence")) {
			commandObj = new CakedefenceCommand(sender, args, plugin);
		} else if (command.equals("group")) {
			commandObj = new GroupCommand(sender, args, plugin);
		} else if (command.equals("leavecake")) {
			commandObj = new LeavecakeCommand(sender, args, plugin);
		} else if (command.equals("message")) {
			commandObj = new MessageCommand(sender, args, plugin);
		} else if (command.equals("minecart")) {
			commandObj = new MinecartCommand(sender, args, plugin);
		} else if (command.equals("mobcheck")) {
			commandObj = new MobcheckCommand(sender, args, plugin);
		} else if (command.equals("note")) {
			commandObj = new NoteCommand(sender, args, plugin);
		} else if (command.equals("potion")) {
			commandObj = new PotionCommand(sender, args, plugin);
		} else if (command.equals("ride")) {
			commandObj = new RideCommand(sender, args, plugin);
		} else if (command.equals("roll")) {
			commandObj = new RollCommand(sender, args, plugin);
		} else if (command.equals("searchregion")) {
			commandObj = new SearchregionCommand(sender, args, plugin);
		} else if (command.equals("sgm")) {
			commandObj = new SgmCommand(sender, args, plugin);
		} else if (command.equals("te")) {
			commandObj = new TeCommand(sender, args, plugin);
		} else if (command.equals("tpallow")) {
			commandObj = new TpallowCommand(sender, args, plugin);
		} else if (command.equals("tpblock")) {
			commandObj = new TpBlockCommand(sender, args, plugin);
		} else if (command.equals("vip")) {
			//TODO
			//Remove the plugin.get stuff, just pass it through the constructor.
			commandObj = new VipCommand(sender, args, plugin, plugin.getVipStorage(), plugin.getVip());
		} else if (command.equals("warpcakedefence")) {
			commandObj = new WarpcakedefenceCommand(sender, args, plugin);
		} else if (command.equals("warppvp")) {
			commandObj = new WarppvpCommand(sender, args, plugin);
		} else if (command.equals("slap")) {
			commandObj = new SlapCommand(sender, args, plugin);
		} else if (command.equals("plot")) {
			if (args.length == 0) {
				//TODO Remove pmark, ptp, pdone and pcheck
			}
		}

		/*
		 * if (commandLabel.equalsIgnoreCase("plot")) { if (args.length > 0) {
		 * String arg = args[0]; if (arg.equalsIgnoreCase("mark")) {
		 * markCommand(player, args); } if (arg.equalsIgnoreCase("check")) {
		 * checkCommand(player, args); } if (arg.equalsIgnoreCase("tp") ||
		 * arg.equalsIgnoreCase("tpid")) { tpCommand(player, args); } if
		 * (arg.equalsIgnoreCase("done")) { doneCommand(player, args); } } }
		 * 
		 * if (commandLabel.equalsIgnoreCase("pmark")) { markCommand(player,
		 * args); } if (commandLabel.equalsIgnoreCase("pcheck")) {
		 * checkCommand(player, args); } if
		 * (commandLabel.equalsIgnoreCase("ptp")) { tpCommand(player, args); }
		 * if (commandLabel.equalsIgnoreCase("pdone")) { doneCommand(player,
		 * args); }
		 */

		if (commandObj != null) {
			boolean handled = commandObj.handle();
			if (!handled) {
				commandObj.badMsg(sender, cmd.getUsage());
			}
		}
		return true;
	}
}