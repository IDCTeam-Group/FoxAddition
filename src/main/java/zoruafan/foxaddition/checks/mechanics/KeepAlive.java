package zoruafan.foxaddition.checks.mechanics;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import zoruafan.foxaddition.FoxAdditionAPI;
import zoruafan.foxaddition.utils.FoxPlayer;
import zoruafan.foxaddition.utils.listeners.KeepAliveEvent;

public class KeepAlive extends FoxPlayer implements Listener {
	FoxAdditionAPI api = FoxAdditionAPI.INSTANCE;
	JavaPlugin plugin = api.getPlugin();
	String p = "mechanics.modules.keepalive";
	private final Map<Player, Integer> pC = new HashMap<>();
	long id = -1;
	boolean first = true;
	
	public KeepAlive() {
		FoliaScheduler.getGlobalRegionScheduler().runAtFixedRate(plugin, (ignored) -> { for (Map.Entry<Player, Integer> entry : pC.entrySet()) entry.setValue(0); }, 20, 20);
	}

	@EventHandler
	public void onKeepAlive(KeepAliveEvent ev) {
		if (ev.isCancelled()) return;
		Player e = ev.getPlayer();
        if (!pC.containsKey(e)) pC.put(e, 0);
        int count = pC.get(e);
        pC.put(e, count+1);
        int maxium = api.getFiles().getAC().getInt(p+".amount.max", 10);
        if((id != 0.0 && first == false) && !iAD(e, "mechanics", p+".amount", true) && (count >= maxium)) {
        	ev.setCancelled(true);
        	flag(true, e, "Exceeds maxium amount of packets!", "Mechanics [KeepAlive]", "[packets:"+count+"/"+maxium+"]", api.getFiles().getAC().getInt(p+".amount.vls", 1), "mechanics");
        	if(api.getFiles().getAC().getBoolean(p+".amount.kick", false)) packetExceeded(e);
        	return;
        }
        
        if(id == -1 && first == true) {
        	id = ev.getID();
        	first = false;
        	return;
        }
        if (id > ev.getID() && !iAD(e, "mechanics", p+".checks.decreasing", true)) {
        	ev.setCancelled(true);
        	flag(false, e, "Sending decreased value in ID!", "Mechanics [KeepAlive]", "[id:"+id+" -> "+ev.getID()+"]", api.getFiles().getAC().getInt(p+".checks.decreasing.vls", 1), "mechanics");
        	if(api.getFiles().getAC().getBoolean(p+".checks.decreasing.kick", false)) timeOut(e);
        	return;        	
        }
        id = ev.getID();
	}
}
