package io.github.kabryxis.afkstatus;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AFKStatusEventListener implements Listener {
	
	private final Map<UUID, Long> timeLoggedIn    = new HashMap<>();
	private final Map<UUID, Long> timeLastMoved   = new HashMap<>();
	private final Map<UUID, Long> timeSpentAfk    = new HashMap<>();
	private final Map<UUID, Long> timeSpentActive = new HashMap<>();
	
	private final File              activeStatusFile;
	private final FileConfiguration data;
	
	public AFKStatusEventListener(AFKStatus plugin) {
		activeStatusFile = new File(plugin.getDataFolder(), "data.yml");
		data = YamlConfiguration.loadConfiguration(activeStatusFile);
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		UUID uuid       = event.getPlayer().getUniqueId();
		long timeActive = data.getLong(uuid.toString(), 0L);
		if (timeActive != 0L) {
			timeSpentActive.put(uuid, timeActive);
		}
		timeLoggedIn.put(uuid, System.currentTimeMillis());
		playerMoved(uuid);
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		UUID uuid       = event.getPlayer().getUniqueId();
		long timeActive = calculateTimeSpentNotAfk(uuid);
		if (timeActive != 0L) {
			data.set(uuid.toString(), timeActive);
			save();
		}
		timeLoggedIn.remove(uuid);
		timeLastMoved.remove(uuid);
		timeSpentAfk.remove(uuid);
		timeSpentActive.remove(uuid);
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		playerMoved(event.getPlayer().getUniqueId());
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		playerMoved(event.getPlayer().getUniqueId());
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getWhoClicked() instanceof Player) {
			playerMoved(event.getWhoClicked().getUniqueId());
		}
	}
	
	public void playerMoved(UUID uuid) {
		
		long current        = System.currentTimeMillis();
		long timeLastNotAfk = timeLastMoved.getOrDefault(uuid, current);
		long timeAfk        = current - timeLastNotAfk;
		
		if (timeAfk > 1000) {
			timeSpentAfk.put(uuid, timeSpentAfk.getOrDefault(uuid, 0L) + timeAfk);
		}
		
		timeLastMoved.put(uuid, current);
		
	}
	
	public long calculateTimeSpentNotAfk(UUID uuid) {
		long current        = System.currentTimeMillis();
		long timeLastNotAfk = timeLastMoved.getOrDefault(uuid, current);
		long timeAfk        = current - timeLastNotAfk;
		long timeActive     = current - timeLoggedIn.get(uuid) - timeSpentAfk.getOrDefault(uuid, 0L) + timeSpentActive.getOrDefault(uuid, 0L);
		
		if (timeAfk > 1000) {
			timeActive -= timeAfk;
		}
		
		return timeActive;
	}
	
	public String getTimeSpentNotAfk(UUID uuid) {
		
		// duplicate parts from #playerMoved because timeSpentAfk does not update until they move again, so need to factor in time currently afk
		// without marking them as unafk
		
		long timeActive = calculateTimeSpentNotAfk(uuid);
		
		int seconds = (int)(timeActive / 1000) % 60;
		int minutes = (int)((timeActive / (1000 * 60)) % 60);
		int hours   = (int)((timeActive / (1000 * 60 * 60)) % 24);
		int days    = (int)((timeActive / (1000 * 60 * 60 * 24)) % 30);
		
		if (minutes == 0) {
			return String.format("%d seconds", seconds);
		} else if (hours == 0) {
			return String.format("%d minutes and %d seconds", minutes, seconds);
		} else if (days == 0) {
			return String.format("%d hours, %d minutes, and %d seconds", hours, minutes, seconds);
		} else {
			return String.format("%d days, %d hours, %d minutes, and %d seconds", days, hours, minutes, seconds);
		}
		
	}
	
	public void save() {
		try {
			data.save(activeStatusFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void clear() {
		timeLoggedIn.clear();
		timeLastMoved.clear();
		timeSpentAfk.clear();
		timeSpentActive.clear();
	}
	
}
