package zoruafan.foxaddition.utils;

import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisconnect;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import net.kyori.adventure.text.Component;
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
    }
    
    public void packetExceeded(Player e) {
    	disconnectPlayer(e, Component.translatable("disconnect.exceeded_packet_rate"));
    	return;
    }
    
    public void timeOut(Player e) {
    	disconnectPlayer(e, Component.translatable("disconnect.timeout"));
    	return;
    }
    
    public void closeConnection(Player e) {
    	disconnectPlayer(e, Component.translatable("disconnect.closed"));
    	return;
    }
    
    public void disconnectPlayer(Player e, Component reason) {
    	if (kickTC.containsKey(e) && System.currentTimeMillis()<kickTC.get(e)) return;
    	if (!api.getPlugin().isEnabled()) return;
    	kickTC.put(e, System.currentTimeMillis()+200);
    	try {
    		User u = (User) e;
    		u.sendPacket(new WrapperPlayServerDisconnect(reason));
            if (e != null) { 
            	if (FoliaScheduler.isFolia()) {
            		e.getScheduler().run(api.getPlugin(), (FA) -> e.kickPlayer("Disconnected"), null);
            	} else {
            		FoliaScheduler.getGlobalRegionScheduler().run(api.getPlugin(), (FA) -> e.kickPlayer("Disconnected"));
            	}
            }
        } catch (Exception p) {
        	if (FoliaScheduler.isFolia()) {
        		e.getScheduler().run(api.getPlugin(), (FA) -> e.kickPlayer("Disconnected"), null);
        	} else {
        		FoliaScheduler.getGlobalRegionScheduler().run(api.getPlugin(), (FA) -> e.kickPlayer("Disconnected"));
        	}
        }
    }
    
	public boolean iAD(Player e, String t, String c, boolean def) {
		if (e.hasPermission("foxac.bypass."+t)) return true;
		final List<String> disabledWorlds = (List<String>) api.getFiles().getAC().getStringList(t.toLowerCase()+".disabled-worlds");
		if (disabledWorlds != null && disabledWorlds.contains(e.getWorld().getName())) { return true; }
		if (api.getFiles().getAC().getBoolean(t.toLowerCase()+".ping.enable", false)) { if(api.getPing(e) >= api.getFiles().getAC().getInt(t.toLowerCase()+".ping.maxium", 1000)) { return true; } }
		if (api.getFiles().getAC().getBoolean(t.toLowerCase()+".tps.enable", false)) { double tps = api.getTPS(); if(tps >= 0 && tps < api.getFiles().getAC().getDouble(t.toLowerCase()+".tps.minium", 15.0)) { return true; } }
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
    	api.verboseNotify(api.getFiles().MN("command.verbose.format", e).replace("{player}", String.valueOf(e.getName())).replace("{vls}", String.valueOf(api.getVLS(e, module))).replace("{vls_added}", String.valueOf(vls)).replace("{module}", name).replace("{details}", details).replace("{debug}", debug).replace("{ver}", ver).replace("{device}", bde_f).replace("{device_noformat}", bde).replace("{server_version}", String.valueOf(api.getVersion())).replace("{foxaddition_version}", api.getPlugin().getDescription().getVersion()).replace("{ping}", String.valueOf(api.getPing(e))).replace("{tps}", String.valueOf(api.getTPS()))); 
    	api.getLog().log(ChatColor.stripColor(api.getFiles().MN("command.verbose.logs", e).replace("{player}", String.valueOf(e.getName())).replace("{vls}", String.valueOf(api.getVLS(e, module))).replace("{vls_added}", String.valueOf(vls)).replace("{module}", name).replace("{details}", details).replace("{debug}", debug).replace("{ver}", ver).replace("{device}", bde_f).replace("{device_noformat}", bde)).replace("{device_noformat}", bde).replace("{server_version}", String.valueOf(api.getVersion())).replace("{foxaddition_version}", api.getPlugin().getDescription().getVersion()).replace("{ping}", String.valueOf(api.getPing(e))).replace("{tps}", String.valueOf(api.getTPS())));
    }
}
