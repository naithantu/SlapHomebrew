package me.naithantu.SlapHomebrew.Commands.Promotion;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.Controllers.Vip;
import nl.stoux.SlapPlayers.Model.Profile;
import nl.stoux.SlapPlayers.Util.DateUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class PromotionVIPCommand extends AbstractCommand {

	private String usage;
	
	public PromotionVIPCommand(CommandSender sender, String[] args) {
		super(sender, args);
		usage = "promotion vip <addvipdays | setVipDays | removeVip | setlifetimevip | getvipdays>";
	}

	@Override
	public boolean handle() throws CommandException {
		if (args.length == 1) {
			throw new UsageException(usage); //Usage
		}
		
		Vip vip = plugin.getVip(); //Get VIP
		Profile offPlayer; int days; //params
		
		switch (args[1].toLowerCase()) {
		case "add": case "addvip": case "addvipdays": //Add VIP days to a player
			testPermission("addvipdays"); //Test Perm
			if (args.length != 4) throw new UsageException("promotion vip addVipDays [Player] [Days]"); //Usage
			offPlayer = getOfflinePlayer(args[2]); //Get player
			days = parseIntPositive(args[3]); //Parse days
			vip.addVipDays(offPlayer.getUUIDString(), days); //Add VIP days
			hMsg("Added " + days + (days == 1 ? " day" : " days") + " VIP to player: " + offPlayer.getCurrentName()); //Message
			break;
			
		case "set": case "setvip": case "setvipdays": //Set number of VIP days
			testPermission("setvipdays"); //Test perm
			if (args.length != 4) throw new UsageException("promotion vip setVipDays [Player] [Days]"); //Usage
			offPlayer = getOfflinePlayer(args[2]); //Get player
			days = parseIntPositive(args[3]); //Parse days
			vip.setVipDays(offPlayer.getUUIDString(), days); //Add VIP days
			hMsg("Set " + offPlayer.getCurrentName() +"'s VIP to " + days + (days == 1 ? " day." : " days.")); //message
			break;
			
		case "remove": case "removevip": //Remove VIP
			testPermission("removevip"); //Test perm
			if (args.length != 3) throw new UsageException("promotion vip removeVip [Player]"); //Usage
			offPlayer = getOfflinePlayer(args[2]); //Get player
			vip.removeVIP(offPlayer.getUUIDString()); //Remove VIP
			hMsg("Removed " + offPlayer.getCurrentName() + "'s VIP.");
			break;
			
		case "lifetime": case "addlifetime": case "setlifetime": case "addlifetimevip": case "setlifetimevip": //Set lifetime VIP
			testPermission("setlifetimevip"); //Test perm
			if (args.length != 3) throw new UsageException("promotion vip setlifetimevip [Player]"); //Usage
			offPlayer = getOfflinePlayer(args[2]); //Get player
			vip.setLifetimeVIP(offPlayer.getUUIDString()); //Set lifetime
			hMsg("Set " + offPlayer.getCurrentName() + "'s VIP to Lifetime."); //Message
			break;
			
		case "days": case "getdays": case "getvipdays": //Get when a players VIP end
			testPermission("getvipdays"); //Usage
			if (args.length != 3) throw new UsageException("promotion vip getvipdays [Player]"); //Usage
			offPlayer = getOfflinePlayer(args[2]); //Get player
			String playername = offPlayer.getCurrentName();
			if (vip.isLifetimeVIP(offPlayer.getUUIDString())) {
				hMsg(playername + " is lifetime VIP!");
			} else {
				long vipEnds = vip.getVIPExpiration(offPlayer.getUUIDString());
				hMsg(playername + "'s VIP ends on: " + ChatColor.GREEN + DateUtil.format("dd MMM. yyyy HH:mm zzz", vipEnds));
			}
			break;
			
		default: //Usage
			throw new UsageException(usage);
		}
		return true;
	}
	
	@Override
	protected void testPermission(String perm) throws CommandException {
		super.testPermission("promotion.vip." + perm);
	}
	
	/**
	 * TabComplete on this command
	 * @param sender The sender of the command
	 * @param args given arguments
	 * @return List of options
	 */
	public static List<String> tabComplete(CommandSender sender, String[] args) {
		if (args.length == 2) {
			return filterResults(createNewList("addvipdays", "setvipdays", "removevip", "lifetime", "addlifetimevip", "setlifetimevip", "days", "getdays", "getvipdays"), args[1]);
		} else {
            return null;
        }
	}

}
