package zoruafan.foxaddition.checks.badpackets;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import zoruafan.foxaddition.FoxAdditionAPI;
import zoruafan.foxaddition.utils.FoxPlayer;
import zoruafan.foxaddition.utils.listeners.EntityActionEvent;
import zoruafan.foxaddition.utils.listeners.InteractEntityEvent;

public class BadPacketsG extends FoxPlayer implements Listener {
	FoxAdditionAPI api = FoxAdditionAPI.INSTANCE;
	JavaPlugin plugin = api.getPlugin();
	String p = "badpackets.modules.packets.g";
	
	public BadPacketsG() {}
	
	@EventHandler
	public void onInteractEntity(InteractEntityEvent ev) {
		if (ev.isCancelled()) return;
		Player e = ev.getPlayer();
		int id = ev.getId();
		
		boolean invalid = false;
		String invalid_reason = "";
		if (e.isDead()) { invalid = true; invalid_reason = "Sending interaction packet when this is dead (impossible)."; }
		else if (e.isSleeping()) { invalid = true; invalid_reason = "Sending interaction packet when this is sleeping (impossible)."; }

		if (invalid && !iAD(e, "badpackets", p+".enable", true)) {
			detect(e, invalid_reason, id);
        	if(api.getFiles().getAC().getBoolean(p+".cancel", true)) { ev.setCancelled(true); }
        	return;
		}
	}
	
	@EventHandler
	public void onEntityAction(EntityActionEvent ev) {
		if (ev.isCancelled()) return;
		Player e = ev.getPlayer();
		int id = ev.getEntityId();
		boolean invalid = false;
		String invalid_reason = "";
		if (e.isDead()) { invalid = true; invalid_reason = "Sending entity action packet when this is dead (impossible)."; }
		else if (e.isSleeping()) { invalid = true; invalid_reason = "Sending entity action packet when this is sleeping (impossible)."; }
		if (invalid && !iAD(e, "badpackets", p+".enable", true)) {
			detect(e, invalid_reason, id);
        	if(api.getFiles().getAC().getBoolean(p+".cancel", true)) { ev.setCancelled(true); }
        	return;
		}
	}
	
	private void detect(Player e, String r, int id) {
		flag(false, e, r, "BadPackets (Packets) [G]", "[id:"+id+"] [isBedrock:"+api.getGeyser().iB(e)+"]", api.getFiles().getAC().getInt(p+".vls", 5), "badpackets");
	}
}
