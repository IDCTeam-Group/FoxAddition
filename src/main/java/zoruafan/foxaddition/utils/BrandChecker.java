package zoruafan.foxaddition.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import zoruafan.foxaddition.FoxAdditionAPI;

public class BrandChecker extends FoxPlayer implements Listener, PluginMessageListener {
	FoxAdditionAPI api = FoxAdditionAPI.INSTANCE;
	private final JavaPlugin plugin = api.getPlugin();
	private final FileConfiguration file = api.getFiles().getST();
    String brand = "vanilla";
    boolean hasBrand = false;
    String p = "brandchecker.";

    public BrandChecker() { registerPluginChannelListener(); }	
    
    private void registerPluginChannelListener() {
        try { Bukkit.getServer().getMessenger().registerIncomingPluginChannel(plugin, "minecraft:brand", this); } catch (Exception e) {}
        try { Bukkit.getServer().getMessenger().registerIncomingPluginChannel(plugin, "MC|Brand", this); } catch (Exception e) {}
    }

    @Override
    public void onPluginMessageReceived(String channel, Player e, byte[] message) {
    	if(!file.getBoolean(p+".enable", true)) return;
        if ("minecraft:brand".equals(channel) || "MC|Brand".equals(channel)) {
        	String originalMessage = new String(message, StandardCharsets.UTF_8).replace(" (Velocity)", "");
            brand = originalMessage.trim();
            if (!isAllowed(brand)) {
                List<String> commands = file.getStringList(p+".commands");
                for (String command : commands) {
                	if(command.equals("[close]")) { closeConnection(e); return; }
                	String cmd_1 = command.replace("{player}", e.getName()).replace("{brand}", brand);
                	if (e != null) {
                		Plugin placeholderAPI = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI");
                		if (placeholderAPI != null && placeholderAPI.isEnabled()) cmd_1 = applyPlaceholderAPI(e, cmd_1);
                	}
                	final String cmd = cmd_1;
                	FoliaScheduler.getGlobalRegionScheduler().run(api.getPlugin(), (FA) -> plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), cmd));
                }
            }
        }
    }
    
    private boolean isAllowed(String brand) {
        List<String> l = file.getStringList(p+".list");
        String t = file.getString(p+".type", "whitelist");
        String bL = brand;
        boolean iM = false;
        for (String ps : l) {
            String regex = ps.replace("*", ".*");
            try {
                Pattern pattern = Pattern.compile(regex);
                if (pattern.matcher(bL).matches()) {
                    iM = true;
                    break;
                }
            } catch (Exception e) { continue; }
        }
        if (t.equalsIgnoreCase("whitelist")) { return iM; } else if (t.equalsIgnoreCase("blacklist")) { return !iM;
        } else {
            plugin.getLogger().severe("[BRANDCHECKER] Invalid option in 'type'. Use 'whitelist' or 'blacklist'.");
            return true;
        }
    }
    
    public String applyPlaceholderAPI(final Player player, final String input) {
        try {
            final Class<?> placeholderAPI = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            final Method setPlaceholdersMethod = placeholderAPI.getMethod("setPlaceholders", Player.class, String.class);
            return (String) setPlaceholdersMethod.invoke(null, player, input);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | NullPointerException e) { return input; }
    }
}
