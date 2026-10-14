package zoruafan.foxaddition.utils;

import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import zoruafan.foxaddition.FoxAdditionAPI;

public class FoxPlayer implements Listener {
	FoxAdditionAPI api = FoxAdditionAPI.INSTANCE;
    public static ServerVersion VERSION;
    public boolean inventory = false;
    private final Map<Player, Long> kickTC = new ConcurrentHashMap<>();
    JavaPlugin plugin = api.getPlugin();
    private final Map<Player, Long> delays = new ConcurrentHashMap<>();
    
    @EventHandler
    public void onQuit(PlayerQuitEvent ev) { forget(ev.getPlayer()); }
    
    /** Drops all per-player state of this check. Subclasses with own maps override this and call super. */
    protected void forget(Player e) {
    	kickTC.remove(e);
    	delays.remove(e);
    	lastServerVelocity.remove(e);
    }
    
    // When the server last gave a player velocity (knockback, explosions, wind charges, plugins).
    private static final Map<Player, Long> lastServerVelocity = new ConcurrentHashMap<>();
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onVelocity(PlayerVelocityEvent ev) { lastServerVelocity.put(ev.getPlayer(), System.currentTimeMillis()); }
    
    // The velocity a server keeps for a player is just its last movement and goes stale while the
    // player stands still (a sprint-jump leaves up to about 0.65), so its size says nothing about
    // knockback. Knockback is taken from PlayerVelocityEvent; an upward velocity means the player is
    // already rising.
    public static boolean hasExternalVelocity(Vector velocity, long millisSinceServerVelocity) {
    	return millisSinceServerVelocity < 1500L || velocity.getY() > 0.0;
    }
    
    public static boolean hasExternalVelocity(Player e, Vector velocity) {
    	return hasExternalVelocity(e, velocity, System.currentTimeMillis());
    }
    
    // at: when the movement happened; the check itself may run later on a busy server.
    public static boolean hasExternalVelocity(Player e, Vector velocity, long at) {
    	Long last = lastServerVelocity.get(e);
    	return hasExternalVelocity(velocity, last == null ? Long.MAX_VALUE : at - last);
    }
    
    // Packet checks decide on the netty thread; anything that touches the player or the world
    // (flags send messages, run commands and read the location for Discord) runs on the player's thread.
    protected void runLater(Player e, Runnable task) {
    	if (!plugin.isEnabled()) return;
    	if (FoliaScheduler.isFolia()) {
    		e.getScheduler().run(plugin, (FA) -> task.run(), null);
    	} else {
    		Bukkit.getScheduler().runTask(plugin, task);
    	}
    }
    
    protected void flagLater(boolean delay, Player e, String details, String name, String debug, int vls, String module) {
    	runLater(e, () -> { if (e.isOnline()) flag(delay, e, details, name, debug, vls, module); });
    }
    
    public void packetExceeded(Player e) {
    	disconnectPlayer(e, api.getFiles().getKickMessage("packets", "Kicked for exceeding packet rate limit"));
    	return;
    }
    
    public void timeOut(Player e) {
    	disconnectPlayer(e, api.getFiles().getKickMessage("timeout", "Timed out"));
    	return;
    }
    
    public void closeConnection(Player e) {
    	disconnectPlayer(e, api.getFiles().getKickMessage("closed", "Connection closed"));
    	return;
    }
    
    // The old disconnect-packet path cast Player to a PacketEvents User, which always threw,
    // so every kick already ended up here. The reason comes from language.yml (messages.kick).
    public void disconnectPlayer(Player e, String reason) {
    	if (kickTC.containsKey(e) && System.currentTimeMillis()<kickTC.get(e)) return;
    	if (!api.getPlugin().isEnabled()) return;
    	kickTC.put(e, System.currentTimeMillis()+200);
    	if (FoliaScheduler.isFolia()) {
    		e.getScheduler().run(api.getPlugin(), (FA) -> e.kickPlayer(reason), null);
    	} else {
    		FoliaScheduler.getGlobalRegionScheduler().run(api.getPlugin(), (FA) -> e.kickPlayer(reason));
    	}
    }
    
	public boolean iAD(Player e, String t, String c, boolean def) {
		if (e.hasPermission("foxac.bypass."+t)) return true;
		final List<String> disabledWorlds = (List<String>) api.getFiles().getAC().getStringList(t.toLowerCase()+".disabled-worlds");
		if (disabledWorlds != null && disabledWorlds.contains(e.getWorld().getName())) { return true; }
		if (api.getFiles().getAC().getBoolean(t.toLowerCase()+".ping.enable", false)) { if(api.getPing(e) >= api.getFiles().getAC().getInt(t.toLowerCase()+".ping.maxium", 1000)) { return true; } }
		if (api.getFiles().getAC().getBoolean(t.toLowerCase()+".tps.enable", false)) { double tps = api.getTPS(e); if(tps >= 0 && tps < api.getFiles().getAC().getDouble(t.toLowerCase()+".tps.minium", 15.0)) { return true; } }
		// Per-check exemptions, e.g. world.modules.nuker.angle.conditions in checks.yml.
		final String cond = c+".conditions";
		if (api.getFiles().getAC().getBoolean(cond+".ping.enable", false) && api.getPing(e) >= api.getFiles().getAC().getInt(cond+".ping.maxium", 1000)) { return true; }
		if (api.getFiles().getAC().getBoolean(cond+".tps.enable", false)) { double tps = api.getTPS(e); if(tps >= 0 && tps < api.getFiles().getAC().getDouble(cond+".tps.minium", 15.0)) { return true; } }
		boolean isBedrock = api.getGeyser().iB(e);
		int mode = 1;
		if(!isBedrock) mode = 1;
		else mode = 2;
		if(mode == 1) { return !api.getFiles().getAC().getBoolean(c+".enable.java", def); }
		else { return !api.getFiles().getAC().getBoolean(c+".enable.bedrock", def); }
	}
    
    public void flag(boolean delay, Player e, String details, String name, String debug, int vls, String module) {
    	if (delays.containsKey(e) && System.currentTimeMillis()<delays.get(e)) return;
        if(delay) { delays.put(e, System.currentTimeMillis()+500); } else { delays.put(e, System.currentTimeMillis()+50); }
    	debug = debug.replace("[", "").replace("] [", "§8, §7").replace("]", "§7").replaceAll(":", ":§b").replaceAll("/", "§8/§a").replace("true", "§atrue").replace("false", "§cfalse").replace(",", "§7,");
    	api.addVLS(e, module, vls);
    	api.getVL().flag(e, module);
    	String ver = api.getVersion().name().toString();
    	try { ver = api.getClientVersion(e); } catch (Exception ig) {}
    	ver = ver.replace("_", ".").replace("V.", "");
    	String bde_f = api.getGeyser().getDevice(e);
    	String bde = bde_f;
    	bde_f = "[device:"+api.getGeyser().getDevice(e)+"]";
		// Runs on the player's thread: the embed reads the player location, JDA queues the request itself.
		try { this.api.getDSRV().sendMessageToDiscord(module, e, api.getVLS(e, module), details); } catch (Throwable ig) {}
    	api.verboseNotify(api.getFiles().MN("command.verbose.format", e).replace("{player}", String.valueOf(e.getName())).replace("{vls}", String.valueOf(api.getVLS(e, module))).replace("{vls_added}", String.valueOf(vls)).replace("{module}", name).replace("{details}", details).replace("{debug}", debug).replace("{ver}", ver).replace("{device}", bde_f).replace("{device_noformat}", bde).replace("{server_version}", String.valueOf(api.getVersion())).replace("{foxaddition_version}", api.getPlugin().getDescription().getVersion()).replace("{ping}", String.valueOf(api.getPing(e))).replace("{tps}", String.valueOf(api.getTPS(e)))); 
    	api.getLog().log(ChatColor.stripColor(api.getFiles().MN("command.verbose.logs", e).replace("{player}", String.valueOf(e.getName())).replace("{vls}", String.valueOf(api.getVLS(e, module))).replace("{vls_added}", String.valueOf(vls)).replace("{module}", name).replace("{details}", details).replace("{debug}", debug).replace("{ver}", ver).replace("{device}", bde_f).replace("{device_noformat}", bde)).replace("{device_noformat}", bde).replace("{server_version}", String.valueOf(api.getVersion())).replace("{foxaddition_version}", api.getPlugin().getDescription().getVersion()).replace("{ping}", String.valueOf(api.getPing(e))).replace("{tps}", String.valueOf(api.getTPS(e))));
    }
}
