package me.naithantu.SlapHomebrew.Commands.Staff.ImprovedRegion;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.IRGException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class DefineCommand extends AbstractImprovedRegionCommand {
	
	public DefineCommand(SlapHomebrew plugin, Player p, String[] args) {
		super(plugin, p, args);
	}

	@Override
	protected void action() throws CommandException {
		if (args.length == 1) throw new UsageException("irg define <Region ID> [owner1] [owner2]..");
		
		validateRegionID(args[1]); //Check if a valid region
		
		if (rm.hasRegion(args[1])) { //Check if the region already exists
			throw new IRGException("This region already exists. To change the shape, use: " + ChatColor.YELLOW + "/irg redefine " + args[1]);
		}
		
		ProtectedRegion region = createRegionFromSelection(args[1]); //Create the region

		if (args.length > 2) { //Owners specified
			DefaultDomain domain = new DefaultDomain();
			for (int x = 2; x < args.length; x++) { //Get players
				domain.addPlayer(getOfflinePlayer(args[x]).getName());
			}
			region.setOwners(domain); //Set owners
		}
		
		rm.addRegion(region); //Add the region
		saveChanges(); //Save changes
		
		//Give warning about height
        int height = region.getMaximumPoint().getBlockY() - region.getMinimumPoint().getBlockY();
        if (height <= 2) {
            p.sendMessage(ChatColor.GRAY + "Warning: The height of the region was " + (height + 1) + " block(s).");
        }
        
        //Send succesfull message
        hMsg("A new region has been made! Named: " + region.getId());
	}

}