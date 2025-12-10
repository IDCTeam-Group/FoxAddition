package zoruafan.foxaddition.checks.badpackets;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import zoruafan.foxaddition.FoxAdditionAPI;
import zoruafan.foxaddition.utils.FoxPlayer;
import zoruafan.foxaddition.utils.listeners.InteractEntityEvent;

public class BadPacketsB extends FoxPlayer implements Listener {
	FoxAdditionAPI api = FoxAdditionAPI.INSTANCE;
	JavaPlugin plugin = api.getPlugin();
	String p = "badpackets.modules.packets.b";
	
	public BadPacketsB() {}
	
	@EventHandler
	public void onInteractEntity(InteractEntityEvent ev) {
		if (ev.isCancelled()) return;
		Player e = ev.getPlayer();
		int id = ev.getId();
		boolean invalid = false;
		String invalid_reason = "";
		if (id < 0) { invalid = true; invalid_reason = "Sending interaction entity id less than 0 (invalid)."; }
		else if (id == e.getEntityId()) { invalid = true; invalid_reason = "Sending self interaction entity id (invalid)."; }
		if (invalid && !iAD(e, "badpackets", p+".enable", true)) {
        	flag(false, e, invalid_reason, "BadPackets (Packets) [B]", "[id:"+id+"] [isBedrock:"+api.getGeyser().iB(e)+"]", api.getFiles().getAC().getInt(p+".vls", 5), "badpackets");
        	if(api.getFiles().getAC().getBoolean(p+".cancel", true)) { ev.setCancelled(true); }
        	return;
		}
	}
}
