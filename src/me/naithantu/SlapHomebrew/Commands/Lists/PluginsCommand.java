package me.naithantu.SlapHomebrew.Commands.Lists;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.ErrorMsg;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

public class PluginsCommand extends AbstractCommand {
	
	//TODO Remove static references
	private static ArrayList<String> plugins;
	private static int nrOfPlugins;
	
	public PluginsCommand(CommandSender sender, String[] args) {
		super(sender, args);
		if (plugins == null) {
			//Fill ArrayList with plugins
			plugins = new ArrayList<>();
			plugins.add("ArenaPVP");
			plugins.add("ButtonWarp");
			plugins.add("Buycraft");
			plugins.add("CraftBook");
			plugins.add("Dynmap");
			plugins.add("Essentials");
			plugins.add("ExpVault");
			plugins.add("LogBlock");
			plugins.add("LWC");
			plugins.add("MCBans");
			plugins.add("MobBountyReloaded");
			plugins.add("Multiverse");
			plugins.add("NoCheatPlus");
			plugins.add("NTheEndAgain");
			plugins.add("PermissionsEx");
			plugins.add("ReportRTS");
			plugins.add("ShowCase");
			plugins.add("SlapHomebrew");
			plugins.add("StouxGames");
			plugins.add("Vault");
			plugins.add("WorldBorder");
			plugins.add("WorldEdit");
			plugins.add("WorldGuard");
			nrOfPlugins = plugins.size();
		}
	}

	@Override
	public boolean handle() throws CommandException {
		if (sender.hasPermission("bukkit.command.plugins")) {
			String pS = "Plugins (" + nrOfPlugins + "): ";
			boolean first = true;
			for (String plugin : plugins) {
				if (first) {
					first = false;
					pS = pS + ChatColor.GREEN + plugin;
				} else {
					pS = pS + ChatColor.WHITE + ", " + ChatColor.GREEN + plugin;
				}
			}
			msg(pS);
		} else {
			throw new CommandException(ErrorMsg.noPermission);
		}
		return true;
	}
	
}
