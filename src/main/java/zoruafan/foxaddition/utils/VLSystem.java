package zoruafan.foxaddition.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import zoruafan.foxaddition.FoxAdditionAPI;

public class VLSystem implements Listener {
	FoxAdditionAPI api = FoxAdditionAPI.INSTANCE;
	private final JavaPlugin plugin = api.getPlugin();
    private final FilesManager filesManager = api.getFiles();
    private Map<Player, Set<String>> EC_WL = new HashMap<>();
    private Map<Player, Set<String>> EC_MC = new HashMap<>();
    private Map<Player, Set<String>> EC_BP = new HashMap<>();
    private Timer mechanics;
    private Timer world;
    private Timer badpackets;
    
    public VLSystem() {
    	mechanics = new Timer();
        if(api.getFiles().getAC().getBoolean("mechanics.decay.enable", true)) { long decayI = 1000L * api.getFiles().getAC().getInt("mechanics.decay.interval", 5);
        mechanics.scheduleAtFixedRate(new TimerTask() {
			@Override
            public void run() { for (Player e : Bukkit.getOnlinePlayers()) { decayVLS(e, "mechanics"); } } }, decayI, decayI);
        }
        
        world = new Timer();
        if(api.getFiles().getAC().getBoolean("world.decay.enable", true)) { long decayI = 1000L * api.getFiles().getAC().getInt("world.decay.interval", 4);
        world.scheduleAtFixedRate(new TimerTask() {
			@Override
            public void run() { for (Player e : Bukkit.getOnlinePlayers()) { decayVLS(e, "world"); } } }, decayI, decayI);
        }

        badpackets = new Timer();
        if(api.getFiles().getAC().getBoolean("badpackets.decay.enable", true)) { long decayI = 1000L * api.getFiles().getAC().getInt("badpackets.decay.interval", 5);
        badpackets.scheduleAtFixedRate(new TimerTask() {
			@Override
            public void run() { for (Player e : Bukkit.getOnlinePlayers()) { decayVLS(e, "badpackets"); } } }, decayI, decayI);
        }    
    }
    
    public void flag(Player e, String mo) {
    	String m = mo.toLowerCase();
    	int vls = api.getVLS(e, m);
        List<String> commandsToExecute = loadVLSCommands(e, m+".commands", vls);
        executeCommands(e, commandsToExecute);
        if ("world".equals(m)) { this.EC_WL.putAll(getCommands("world")); }
        else if ("mechanics".equals(m)) { this.EC_MC.putAll(getCommands("mechanics")); }
        else if ("badpackets".equals(m)) { this.EC_BP.putAll(getCommands("badpackets")); }
    }
    
    public void executeCommands(Player e, List<String> commands) {
        if (!e.isOnline()) return;
        for (String originalCommand : commands) {
            String command = originalCommand;
            if (e != null) {
                Plugin placeholderAPI = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI");
                if (placeholderAPI != null && placeholderAPI.isEnabled()) command = applyPlaceholderAPI(e, originalCommand);
            }
            command = command.replace("{player}", e.getName());
            final String cmd = command;
            FoliaScheduler.getGlobalRegionScheduler().run(api.getPlugin(), (FA) -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
        }
        commands.clear();
    }
    
    private void decayVLS(Player e, String mo) {
    	String m = mo.toLowerCase();
    	int currentVLS = api.getVLS(e, m);
    	if (currentVLS > 0) {
    		int decayAmount = filesManager.getAC().getInt(m+".decay.amount", 4);
            for (int i = 0; i < currentVLS; i++) {
                if ("world".equals(m)) { this.EC_WL.computeIfAbsent(e, k -> new HashSet<>()).remove("world.commands-" + i); }
                else if ("mechanics".equals(m)) { this.EC_MC.computeIfAbsent(e, k -> new HashSet<>()).remove("mechanics.commands-" + i); }
                else if ("badpackets".equals(m)) { this.EC_BP.computeIfAbsent(e, k -> new HashSet<>()).remove("badpackets.commands-" + i); }
            }
            int newVLS = Math.max(0, currentVLS - decayAmount);
            api.setVLS(e, m, newVLS);
    	}
    }
    
    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin().equals(plugin)) { if (mechanics != null) { mechanics.cancel(); }} 
        if (event.getPlugin().equals(plugin)) { if (world != null) { world.cancel(); }}
        if (event.getPlugin().equals(plugin)) { if (mechanics != null) { mechanics.cancel(); }}
    }
    
    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player e = event.getPlayer();
        if(e != null);
        api.setVLS(e, "mechanics", 0);
        api.setVLS(e, "world", 0);
        api.setVLS(e, "badpackets", 0);
        this.EC_WL.remove(e);
       	this.EC_MC.remove(e);
       	this.EC_BP.remove(e);
    }
    
    public List<String> loadVLSCommands(final Player e, final String configSection, final int vls) {
        final List<String> commands = new ArrayList<>();
        final String configValue = configSection.replace(".commands", "");
        Set<String> executedCommands = getExecutedCommands(e, configValue);
        final ConfigurationSection vlsCommandsConfig = api.getFiles().getAC().getConfigurationSection(configSection);
        if (vlsCommandsConfig != null) {
            for (String key : vlsCommandsConfig.getKeys(false)) {
                int threshold = Integer.parseInt(key);
                String commandKey = configSection + "-" + threshold;
                if (vls >= threshold && !executedCommands.contains(commandKey)) {
                    Object value = vlsCommandsConfig.get(key);
                    if (value instanceof String) {
                        commands.add((String) value);
                    } else if (value instanceof List<?>) {
                        List<?> listValue = (List<?>) value;
                        for (Object item : listValue) {
                            if (item instanceof String) {
                                commands.add((String) item);
                            }
                        }
                    }
                    executedCommands.add(commandKey);
                    if ("world".equals(configValue)) { this.EC_WL.put(e, executedCommands); }
                    else if ("mechanics".equals(configValue)) { this.EC_MC.put(e, executedCommands); }
                    else if ("badpackets".equals(configValue)) { this.EC_BP.put(e, executedCommands); }
                }
            }
        }
        return commands;
    }
    private Set<String> getExecutedCommands(Player e, String configSection) {
        Map<Player, Set<String>> targetMap = getCommands(configSection);
        if (targetMap != null) { return targetMap.computeIfAbsent(e, k -> new HashSet<>()); }
        else { return Collections.emptySet(); }
    }
    public Map<Player, Set<String>> getCommands(String configValue) {
        if ("world".equals(configValue)) { return this.EC_WL; }
        else if ("mechanics".equals(configValue)) { return this.EC_MC; }
        else if ("badpackets".equals(configValue)) { return this.EC_BP; }
        else { return Collections.emptyMap(); }
    }
    public String applyPlaceholderAPI(final Player e, final String input) {
        try {
            final Class<?> placeholderAPI = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            final Method setPlaceholdersMethod = placeholderAPI.getMethod("setPlaceholders", Player.class, String.class);
            return (String) setPlaceholdersMethod.invoke(null, e, input);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | NullPointerException ignored) {
            return input;
        }
    }
}