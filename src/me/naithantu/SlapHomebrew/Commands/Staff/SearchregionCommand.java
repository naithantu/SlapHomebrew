package me.naithantu.SlapHomebrew.Commands.Staff;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogging.RegionLogger;
import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Util.Util;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SearchregionCommand extends AbstractCommand {
	
	public SearchregionCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	public boolean handle() throws CommandException {
		testPermission("searchregion"); //Test perm
		if (args.length == 0)  return false; //Check usage
		
		checkDoingCommand();
		
		final String region = args[0].toLowerCase();
		final String world;
		final boolean legacy;
		
		if (args.length == 1) { //Only Region given
			world = getPlayer().getWorld().getName();
			legacy = false;
		} else if (args[args.length -1].equalsIgnoreCase("old")) { //Legacy
			legacy = true;
			world = null; //Compile weeh
		} else { //Region & World given
			legacy = false;
			world = args[1];
		}
		
		addDoingCommand();
		
		if (!legacy) {
			RegionLogger.getRegionChanges(sender, region, world);
		} else {
			Util.runASync(new Runnable() {
				
				private BufferedReader bf;

				@Override
				public void run() {
					try {
						bf = new BufferedReader(new FileReader(plugin.getDataFolder() + File.separator + "legacyWGs.yml"));
						String line;
						while ((line = bf.readLine()) != null) { //Read lines
							String[] split = line.split(":", 2); //Split on [Regionname]:[Changes]
							if (split[0].equalsIgnoreCase(region)) { //Match regionname
								String[] changes = split[1].split("<==>"); //Split on crazy naith thing
								Util.msg(sender, "Changes for region " + ChatColor.GREEN + region + ChatColor.WHITE + " (Legacy File)."); //Msg 
								for (String change : changes) { //Message changes
									sender.sendMessage(ChatColor.GOLD + "  \u2517\u25B6 " + ChatColor.WHITE + change);
								}
								removeDoingCommand();
								return;
							}
						}
						bf.close();
						Util.badMsg(sender, "No region found with this name in the legacy file.");
					} catch (IOException e) {
						Util.badMsg(sender, "Something went wrong with reading the file.");
					} finally {
						removeDoingCommand();
					}
				}
			});
		}
		return true;
	}
	
	/**
	 * TabComplete on this command
	 * @param sender The sender of the command
	 * @param args given arguments
	 * @return List of options
	 */
	public static List<String> tabComplete(CommandSender sender, String[] args) {
		if (!Util.testPermission(sender, "searchregion")) return createEmptyList(); //No perm
		
		if (sender instanceof Player) {
			if (args.length == 1) { //If first argument
				if (args[0].length() > 3) { //If atleast some letters given
					return filterResults( //Filter results
						new ArrayList<String>( //Create new list with all regions in that world
							SlapHomebrew.getInstance().getworldGuard().getRegionManager(((Player) sender).getWorld()).getRegions().keySet()
						),
						args[0]
					);
				}
			}
		}
		return null;
	}
}
