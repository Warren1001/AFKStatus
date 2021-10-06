package io.github.kabryxis.afkstatus;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public final class AFKStatus extends JavaPlugin {
	
	private AFKStatusEventListener eventListener;
	
	@Override
	public void onEnable() {
		eventListener = new AFKStatusEventListener(this);
		getServer().getPluginManager().registerEvents(eventListener, this);
		getCommand("timeactive").setExecutor(new AFKStatusCommandExecutor(this));
	}
	
	@Override
	public void onDisable() {
		eventListener.save();
		eventListener.clear();
	}
	
	public String getTimeSpentNotAfk(UUID uuid) {
		return eventListener.getTimeSpentNotAfk(uuid);
	}
	
}
