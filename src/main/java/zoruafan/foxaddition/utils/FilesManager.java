package zoruafan.foxaddition.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import zoruafan.foxaddition.FoxAdditionAPI;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class FilesManager {
	protected JavaPlugin plugin = FoxAdditionAPI.INSTANCE.getPlugin();
	private FileConfiguration settings;
	private FileConfiguration lang;
	private FileConfiguration checks;
	private FileConfiguration anticheats;
	private final MiniMessage miniMessage = MiniMessage.miniMessage();
	private final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.legacyAmpersand();

	public FilesManager() {
		//this.setup("hooks");
        this.setup("checks");
        this.setup("language");
        this.setup("settings");
	}

    public void setup(String file) {
    	try {
	    	if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdir();
	    	final File filec = new File(plugin.getDataFolder(), file+".yml");
	        if (!filec.exists()) {
	            try {
	                final InputStream inputStream = plugin.getResource(file+".yml");
	                if (inputStream != null) {
	                    Files.copy(inputStream, filec.toPath(), StandardCopyOption.REPLACE_EXISTING);
	                    inputStream.close();
	                }
	            } catch (IOException e) {  plugin.getLogger().warning("[FILES] Failed to get "+file+".yml file."); }
	        }
	        if (file.equals("anticheats")) { anticheats = (FileConfiguration)YamlConfiguration.loadConfiguration(filec); }
	        else if (file.equals("checks")) { checks = (FileConfiguration)YamlConfiguration.loadConfiguration(filec); }
	        else if (file.equals("language")) { lang = (FileConfiguration)YamlConfiguration.loadConfiguration(filec); }
	        else if (file.equals("settings")) { settings = (FileConfiguration)YamlConfiguration.loadConfiguration(filec); }
    	} catch (NullPointerException ignored) {
    		plugin.getLogger().warning("[FILES] Failed to get "+file+".yml file.");
    	}
    }
    
    public boolean exists(String file) {
        File configFile = new File(plugin.getDataFolder(), file+".yml");
        return configFile.exists();
    }

    public void reload(String file) {
    	try {
	    	if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdir();
	    	final File filec = new File(plugin.getDataFolder(), file+".yml");
	        if (!filec.exists()) {
	            try {
	                final InputStream inputStream = plugin.getResource(file+".yml");
	                try { Files.copy(inputStream, filec.toPath(), StandardCopyOption.REPLACE_EXISTING); } catch (Exception e) { Files.copy(inputStream, filec.toPath()); }
	                inputStream.close();
	            } catch (IOException e) {
	                plugin.getLogger().warning("[FILES] Failed to get "+file+".yml file.");
	            }
	        }
	        if (file.equals("anticheats")) { anticheats = (FileConfiguration)YamlConfiguration.loadConfiguration(filec); }
	        else if (file.equals("checks")) { checks = (FileConfiguration)YamlConfiguration.loadConfiguration(filec); }
	        else if (file.equals("language")) { lang = (FileConfiguration)YamlConfiguration.loadConfiguration(filec); }
	        else if (file.equals("settings")) { settings = (FileConfiguration)YamlConfiguration.loadConfiguration(filec); }
	        else return;
    	} catch (NullPointerException ignored) {
    		plugin.getLogger().warning("[FILES] Failed to get "+file+".yml file.");
    	}
    }

    public FileConfiguration getAH() {
    	return anticheats;
    }
    public FileConfiguration getAC() {
    	return checks;
    }
    public FileConfiguration getST() {
        return settings;
    }
    public FileConfiguration getLA() {
        return lang;
    }
    
    public String getLang(final String key, final Player player) {
        if (lang == null) return "";
        String message = lang.getString("messages." + key, "");
        if (player != null) {
            final Plugin placeholderAPI = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI");
            if (placeholderAPI != null && placeholderAPI.isEnabled()) {
                message = applyPlaceholderAPI(player, message);
            }
        }
        // Support both MiniMessage and legacy color codes
        message = convertToLegacy(message).replace("\\n", "\n").replace("%nl%", "\n").replace("\\nl", "\n");
        return message;
    }
    
    private String convertToLegacy(String message) {
        // Check if message contains MiniMessage tags (< and >)
        if (message.contains("<") && message.contains(">")) {
            try {
                // Parse MiniMessage and convert to legacy format
                Component component = miniMessage.deserialize(message);
                return legacySerializer.serialize(component);
            } catch (Exception e) {
                // Fallback to legacy format if parsing fails
                return ChatColor.translateAlternateColorCodes('&', message);
            }
        }
        // Use legacy color code translation for & codes
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    private String applyPlaceholderAPI(final Player player, final String input) {
        try {
            final Class<?> placeholderAPI = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            final Method setPlaceholdersMethod = placeholderAPI.getMethod("setPlaceholders", Player.class, String.class);
            return (String)setPlaceholdersMethod.invoke(null, player, input);
        }
        catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | NullPointerException ex2) {
            return input;
        }
    }
    public String getPrefix() {
        if (lang == null) return "";
        try {
            return convertToLegacy(lang.getString("prefix"));
        } catch (Exception e) {
        	Bukkit.getLogger().severe("[FILES] Hey! The language.yml is broken, please, fix it or delete and restart.");
            return "FAILED!";
        }
    }
    public String MN(final String text, final Player player) {
        String message = getLang(text, player);
        final String prefix = getPrefix();
        final String sending = message.replace("{prefix}", prefix);
        if (player != null) {
            final Plugin placeholderAPI = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI");
            if (placeholderAPI != null && placeholderAPI.isEnabled()) {
                message = applyPlaceholderAPI(player, message);
            }
        }
        message = convertToLegacy(sending).replace("\\n", "\n").replace("%nl%", "\n").replace("\\nl", "\n");
        return message;
    }
}
