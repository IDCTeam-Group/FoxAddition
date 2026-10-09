package zoruafan.foxaddition;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.manager.player.PlayerManager;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.player.User;

import zoruafan.foxaddition.checks.badpackets.BadPacketsA;
import zoruafan.foxaddition.checks.badpackets.BadPacketsB;
import zoruafan.foxaddition.checks.badpackets.BadPacketsC;
import zoruafan.foxaddition.checks.badpackets.BadPacketsD;
import zoruafan.foxaddition.checks.badpackets.BadPacketsE;
import zoruafan.foxaddition.checks.badpackets.BadPacketsF;
import zoruafan.foxaddition.checks.badpackets.BadPacketsG;
import zoruafan.foxaddition.checks.badpackets.BadPacketsH;
import zoruafan.foxaddition.checks.mechanics.Abilities;
import zoruafan.foxaddition.checks.mechanics.BlockPlace;
import zoruafan.foxaddition.checks.mechanics.KeepAlive;
import zoruafan.foxaddition.checks.mechanics.Position;
import zoruafan.foxaddition.checks.world.Nuker;
import zoruafan.foxaddition.utils.BrandChecker;
import zoruafan.foxaddition.utils.FilesManager;
import zoruafan.foxaddition.utils.FoxPlayer;
import zoruafan.foxaddition.utils.GeyserManager;
import zoruafan.foxaddition.utils.LogManager;
import zoruafan.foxaddition.utils.TPSUtil;
import zoruafan.foxaddition.utils.FoxPacketListener;
import zoruafan.foxaddition.utils.VLSystem;
import zoruafan.foxaddition.utils.DiscordSRVManager;

public enum FoxAdditionAPI {
    INSTANCE;
	private JavaPlugin plugin;
	private FilesManager files;
	private VLSystem vlsystem;
	private DiscordSRVManager dsrv;
	private GeyserManager geyser;
	private LogManager log;
	private PacketListenerCommon packetListener;
	private List<CommandSender> verboseL = new CopyOnWriteArrayList<>();
	private Map<Player, Map<String, Integer>> fVLS = new ConcurrentHashMap<>();
	private static final Set<String> cT = new HashSet<>(Arrays.asList("mechanics", "world", "badpackets"));

	protected void load(JavaPlugin plugin) {
		this.plugin = plugin;
		files = new FilesManager();
		geyser = new GeyserManager();
		log = new LogManager();
		// PacketEvents is the shared instance of the packetevents plugin (depend). Replacing it with
		// PacketEvents.setAPI() would take it away from every other plugin that uses it.
	}
	
	protected void enable(JavaPlugin plugin) {
		try {
	        packetListener = PacketEvents.getAPI().getEventManager().registerListener((PacketListener) new FoxPacketListener(), PacketListenerPriority.HIGHEST);
		} catch (Exception e) {}
		Object[] checks = {
				new FoxPlayer(),
				new Abilities(),
				new BlockPlace(),
				new Position(),
				new KeepAlive(),
				
				new BrandChecker(),
				
				new Nuker(),
				
				new BadPacketsA(),
				new BadPacketsB(),
				new BadPacketsC(),
				new BadPacketsD(),
				new BadPacketsE(),
				new BadPacketsF(),
				new BadPacketsG(),
				new BadPacketsH()
		};
        for (Object obj : checks) {
            if (obj instanceof Listener) {
                Bukkit.getServer().getPluginManager().registerEvents((Listener) obj, plugin);
            }
        }
		this.dsrv = new DiscordSRVManager();
        this.vlsystem = new VLSystem();
        Bukkit.getServer().getPluginManager().registerEvents(this.vlsystem, plugin);
	}
	
	protected void disable(JavaPlugin plugin) {
		if (vlsystem != null) vlsystem.shutdown();
		// Only remove our own listener; terminating would shut PacketEvents down for all plugins.
		try { if (packetListener != null) PacketEvents.getAPI().getEventManager().unregisterListener(packetListener); } catch (Exception e) {}
        try { Bukkit.getServer().getMessenger().unregisterIncomingPluginChannel(plugin, "minecraft:brand"); } catch (Exception e) {}
        try { Bukkit.getServer().getMessenger().unregisterIncomingPluginChannel(plugin, "MC|Brand"); } catch (Exception e) {}
	}
	
    public int getVLS(Player player, String checkType) {
    	if (!(cT.contains(checkType))) {
    		Bukkit.getLogger().severe("[FA] [API] Value for checkType '"+checkType+"' is invalid. Event ignored.");
    		return 0;
    	}
        Map<String, Integer> playerVLS = fVLS.get(player);
        if (playerVLS == null) return 0;
        return playerVLS.getOrDefault(checkType, 0);
    }
    
    public void addVLS(Player player, String checkType, int vls) {
    	if (!(cT.contains(checkType))) {
    		Bukkit.getLogger().severe("[FA] [API] Value for checkType '"+checkType+"' is invalid. Event ignored.");
    		return;
    	}
        Map<String, Integer> playerVLS = fVLS.computeIfAbsent(player, k -> new ConcurrentHashMap<>());
        playerVLS.put(checkType, playerVLS.getOrDefault(checkType, 0) + vls);
    }
    
    public void setVLS(Player player, String checkType, int vls) {
    	if (!(cT.contains(checkType))) {
    		Bukkit.getLogger().severe("[FA] [API] Value for checkType '"+checkType+"' is invalid. Event ignored.");
    		return;
    	}
        Map<String, Integer> playerVLS = fVLS.computeIfAbsent(player, k -> new ConcurrentHashMap<>());
        playerVLS.put(checkType, Math.max(0, vls));
    }
    
    protected void checkForUpdates() {
        // Off the server thread: the request may take up to 6 seconds.
        CompletableFuture.runAsync(this::requestLatestVersion);
    }
    
    private void requestLatestVersion() {
        try {
        	if(!files.getST().getBoolean("updates.update", true)) return;
            @SuppressWarnings("deprecation")
			URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=111260");
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            connection.setUseCaches(false);
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                String currentVersion = plugin.getDescription().getVersion();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String latestVersion = reader.readLine();
                if (!latestVersion.equals(currentVersion)) {
                    plugin.getLogger().warning("[UPDATER] A new version of this plugin has been found.");
                    plugin.getLogger().warning("[UPDATER] Your version is: "+currentVersion);
                    plugin.getLogger().warning("[UPDATER] Latest version is: "+latestVersion);
                    plugin.getLogger().warning("[UPDATER] Download it in: https://www.spigotmc.org/resources/111260/");
                } else plugin.getLogger().info("[UPDATER] You are currently using the lastest version.");
                reader.close();
                connection.disconnect();
            }
            else plugin.getLogger().warning("[UPDATER] Failed to check for new updates!");
            connection.disconnect();
        }
        catch (Exception e) { plugin.getLogger().warning("[UPDATER] Failed to check for new updates!"); }
    }
	
    public Set<Player> getTrackedPlayers() { return fVLS.keySet(); }
    public void forgetPlayer(Player player) {
        fVLS.remove(player);
        verboseL.remove(player);
        if (dsrv != null) dsrv.forget(player);
    }
	
    public boolean getVerbose(CommandSender sender) { return verboseL.contains(sender); }
    public void toggleVerbose(CommandSender sender) {
    	if(getVerbose(sender)) verboseL.remove(sender);
    	else verboseL.add(sender);
    }
    public void verboseNotify(String message) {
        for (CommandSender sender : verboseL) { sender.sendMessage(message); }
    }
	
	public JavaPlugin getPlugin() { return plugin; }
	public FilesManager getFiles() { return files; }
	public VLSystem getVL() { return vlsystem; }
	public GeyserManager getGeyser() { return geyser; }
	public LogManager getLog() { return log; }
	public DiscordSRVManager getDSRV() { return this.dsrv; }
    public ServerVersion getVersion() { return PacketEvents.getAPI().getServerManager().getVersion(); }
    public String getClientVersion(Player e) { 
    	User user = PacketEvents.getAPI().getPlayerManager().getUser(e);
    	return user.getClientVersion().getReleaseName(); 
    }
    public int getPing(Player e) {
        PlayerManager pM = PacketEvents.getAPI().getPlayerManager();
        return pM.getPing(e); 	
    }
    public double getTPS() {
    	double[] tps = TPSUtil.getRecentTPS();
    	return Math.round(tps[0]*10.0)/10.0;
    }
}
