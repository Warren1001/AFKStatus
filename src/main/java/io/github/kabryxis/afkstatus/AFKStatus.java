package io.github.kabryxis.afkstatus;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class AFKStatus extends JavaPlugin {
	
	private AFKStatusEventListener eventListener;
	
	@Override
	public void onEnable() {
		eventListener = new AFKStatusEventListener();
		getServer().getPluginManager().registerEvents(eventListener, this);
		getCommand("timeactive").setExecutor(new AFKStatusCommandExecutor(this));
	}
	
	@Override
	public void onDisable() {
		eventListener.clear();
	}
	
	public String getTimeSpentNotAfk(Player player) {
		return eventListener.getTimeSpentNotAfk(player);
	}
	
}
