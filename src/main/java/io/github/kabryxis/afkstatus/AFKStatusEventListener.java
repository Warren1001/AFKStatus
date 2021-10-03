package io.github.kabryxis.afkstatus;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

public class AFKStatusEventListener implements Listener {
	
	private final Map<Player, Long> timeLoggedIn  = new HashMap<>();
	private final Map<Player, Long> timeLastMoved = new HashMap<>();
	private final Map<Player, Long> timeSpentAfk  = new HashMap<>();
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		timeLoggedIn.put(player, System.currentTimeMillis());
		playerMoved(player);
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		timeLoggedIn.remove(player);
		timeLastMoved.remove(player);
		timeSpentAfk.remove(player);
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		playerMoved(event.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		playerMoved(event.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getWhoClicked() instanceof Player) {
			playerMoved((Player)event.getWhoClicked());
		}
	}
	
	public void playerMoved(Player player) {
		
		long current        = System.currentTimeMillis();
		long timeLastNotAfk = timeLastMoved.getOrDefault(player, current);
		long timeAfk        = current - timeLastNotAfk;
		
		if (timeAfk > 1000) {
			timeSpentAfk.put(player, timeSpentAfk.getOrDefault(player, 0L) + timeAfk);
		}
		
		timeLastMoved.put(player, current);
		
	}
	
	public String getTimeSpentNotAfk(Player player) {
		
		// duplicate parts from #playerMoved because timeSpentAfk does not update until they move again, so need to factor in time currently afk
		// without marking them as unafk
		
		long current        = System.currentTimeMillis();
		long timeLastNotAfk = timeLastMoved.getOrDefault(player, current);
		long timeAfk        = current - timeLastNotAfk;
		long timeActive     = (current - timeLoggedIn.get(player)) - timeSpentAfk.getOrDefault(player, 0L);
		
		if (timeAfk > 1000) {
			timeActive -= timeAfk;
		}
		
		int seconds = (int)(timeActive / 1000) % 60;
		int minutes = (int)((timeActive / (1000 * 60)) % 60);
		int hours   = (int)((timeActive / (1000 * 60 * 60)) % 24);
		
		if (minutes == 0) {
			return String.format("%02d seconds", seconds);
		} else if (hours == 0) {
			return String.format("%02d minutes and %02d seconds", minutes, seconds);
		} else {
			return String.format("%d hours, %02d minutes, and %02d seconds", hours, minutes, seconds);
		}
		
	}
	
	public void clear() {
		timeLoggedIn.clear();
		timeLastMoved.clear();
		timeSpentAfk.clear();
	}
	
}
