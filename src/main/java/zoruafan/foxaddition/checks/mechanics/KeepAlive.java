package zoruafan.foxaddition.checks.mechanics;

import java.util.concurrent.ConcurrentHashMap;
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
	private final Map<Player, Integer> pC = new ConcurrentHashMap<>();
	private final Map<Player, Long> lastId = new ConcurrentHashMap<>();
	
	public KeepAlive() {
		FoliaScheduler.getGlobalRegionScheduler().runAtFixedRate(plugin, (ignored) -> pC.clear(), 20, 20);
	}

	@EventHandler
	public void onKeepAlive(KeepAliveEvent ev) {
		if (ev.isCancelled()) return;
		Player e = ev.getPlayer();
        int count = pC.getOrDefault(e, 0);
        pC.put(e, count+1);
        int maxium = api.getFiles().getAC().getInt(p+".amount.max", 10);
        final Long id = lastId.get(e);
        if((id != null && id != 0L) && !iAD(e, "mechanics", p+".amount", true) && (count >= maxium)) {
        	ev.setCancelled(true);
        	flag(true, e, "Exceeds maxium amount of packets!", "Mechanics [KeepAlive]", "[packets:"+count+"/"+maxium+"]", api.getFiles().getAC().getInt(p+".amount.vls", 1), "mechanics");
        	if(api.getFiles().getAC().getBoolean(p+".amount.kick", false)) packetExceeded(e);
        	return;
        }
        
        if(id == null) {
        	lastId.put(e, ev.getID());
        	return;
        }
        if (id > ev.getID() && !iAD(e, "mechanics", p+".checks.decreasing", true)) {
        	ev.setCancelled(true);
        	flag(false, e, "Sending decreased value in ID!", "Mechanics [KeepAlive]", "[id:"+id+" -> "+ev.getID()+"]", api.getFiles().getAC().getInt(p+".checks.decreasing.vls", 1), "mechanics");
        	if(api.getFiles().getAC().getBoolean(p+".checks.decreasing.kick", false)) timeOut(e);
        	return;        	
        }
        lastId.put(e, ev.getID());
	}
	
	@Override
	protected void forget(Player e) {
		super.forget(e);
		pC.remove(e);
		lastId.remove(e);
	}
}
