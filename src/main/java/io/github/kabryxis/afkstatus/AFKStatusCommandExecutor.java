package io.github.kabryxis.afkstatus;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AFKStatusCommandExecutor implements CommandExecutor {
	
	private final AFKStatus plugin;
	
	public AFKStatusCommandExecutor(AFKStatus plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		Player target;
		
		if (args.length == 1) {
			target = plugin.getServer().getPlayer(args[0]);
		} else if (sender instanceof Player) {
			target = (Player)sender;
		} else {
			sender.sendMessage(String.format("Usage: /%s [player]", label));
			return true;
		}
		
		sender.sendMessage(String.format("Time %s has spent active: %s", target.getDisplayName(), plugin.getTimeSpentNotAfk(target.getUniqueId())));
		
		return true;
	}
	
}
