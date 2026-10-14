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
    String p = "brandchecker.";
    private static final int MAX_BRAND_LENGTH = 64;

    public BrandChecker() { registerPluginChannelListener(); }	
    
    private void registerPluginChannelListener() {
        try { Bukkit.getServer().getMessenger().registerIncomingPluginChannel(plugin, "minecraft:brand", this); } catch (Exception e) {}
        try { Bukkit.getServer().getMessenger().registerIncomingPluginChannel(plugin, "MC|Brand", this); } catch (Exception e) {}
    }

    @Override
    public void onPluginMessageReceived(String channel, Player e, byte[] message) {
    	if(!file.getBoolean(p+".enable", true)) return;
        if ("minecraft:brand".equals(channel) || "MC|Brand".equals(channel)) {
        	String originalMessage = readBrand(message).replace(" (Velocity)", "");
            final String brand = originalMessage.trim();
            if (!isAllowed(brand)) {
                List<String> commands = file.getStringList(p+".commands");
                if (!api.getPlugin().isEnabled()) return;
                // The brand is client-controlled, so it must never reach the console as raw text.
                final String safeBrand = sanitize(brand);
                for (String command : commands) {
                	if(command.equals("[close]")) { closeConnection(e); return; }
                	String cmd_1 = command.replace("{player}", e.getName());
                	if (e != null) {
                		Plugin placeholderAPI = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI");
                		if (placeholderAPI != null && placeholderAPI.isEnabled()) cmd_1 = applyPlaceholderAPI(e, cmd_1);
                	}
                	// Insert the brand after PlaceholderAPI, so placeholders inside the brand are not parsed.
                	final String cmd = cmd_1.replace("{brand}", safeBrand);
                	FoliaScheduler.getGlobalRegionScheduler().run(api.getPlugin(), (FA) -> plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), cmd));
                }
            }
        }
    }
    
    // The payload is a VarInt length followed by the UTF-8 brand. Reading it as a whole string
    // left the length byte in front of brands with 32 or more characters.
    static String readBrand(byte[] data) {
        int length = 0;
        for (int i = 0; i < data.length && i < 5; i++) {
            int b = data[i] & 0xFF;
            length |= (b & 0x7F) << (7 * i);
            if ((b & 0x80) == 0) {
                int start = i + 1;
                if (length >= 0 && length <= data.length - start) return new String(data, start, length, StandardCharsets.UTF_8);
                break;
            }
        }
        return new String(data, StandardCharsets.UTF_8);
    }
    
    static String sanitize(String brand) {
        String clean = brand.replaceAll("[^A-Za-z0-9._:\\-]", "_");
        return clean.length() > MAX_BRAND_LENGTH ? clean.substring(0, MAX_BRAND_LENGTH) : clean;
    }

    // '*' is the only wildcard; everything else matches literally, upper or lower case.
    static boolean matchesPattern(String pattern, String brand) {
        try {
            return Pattern.compile(Pattern.quote(pattern).replace("*", "\\E.*\\Q"), Pattern.CASE_INSENSITIVE).matcher(brand).matches();
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean isAllowed(String brand) {
        List<String> l = file.getStringList(p+".list");
        String t = file.getString(p+".type", "whitelist");
        String bL = brand;
        boolean iM = false;
        for (String ps : l) {
            if (matchesPattern(ps, bL)) {
                iM = true;
                break;
            }
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
