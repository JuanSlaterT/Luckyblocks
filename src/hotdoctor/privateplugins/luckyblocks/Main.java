package hotdoctor.privateplugins.luckyblocks;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import hotdoctor.privateplugins.luckyblocks.listeners.Listeners;
import hotdoctor.privateplugins.luckyblocks.manager.WandManager;
import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin{
	private String rutaConfig;
	private YamlConfiguration configuration;
	private WandManager wandManager;
	private File configyml;
	public void onEnable() {
		this.configyml = new File(this.getDataFolder(), "config.yml");
		registerConfiguration();
		this.configuration = YamlConfiguration.loadConfiguration(configyml);
		File file = new File(this.getDataFolder() + File.separator + "schematics");
		file.mkdir();
		wandManager = new WandManager(this);
		for(String id : this.getConfig().getConfigurationSection("wands").getKeys(false)) {
			
			this.getWandManger().registerMagicWand(id);
		}
		Bukkit.getPluginManager().registerEvents(new Listeners(this), this);
	}
	
	public void onDisable() {
		
	}
	
	public WandManager getWandManger() {
		return wandManager;
	}
	
	private void registerConfiguration() {
		rutaConfig = configyml.getAbsolutePath();
		if (!(configyml.exists())) {
			setConfigurationDefaults();

		}
	}

	private void setConfigurationDefaults() {
		this.saveResource("config.yml", true);
	}

	@Override
	public FileConfiguration getConfig() {
		return this.getConfiguration();
	}

	public YamlConfiguration getConfiguration() {
		return configuration;
	}
	
	public String color(String message) {
		return ChatColor.translateAlternateColorCodes('&', message);
	}
	
}
