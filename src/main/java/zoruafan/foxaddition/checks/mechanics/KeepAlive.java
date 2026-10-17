package zoruafan.foxaddition.checks.mechanics;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import zoruafan.foxaddition.FoxAdditionAPI;
import zoruafan.foxaddition.utils.FoxPlayer;

public class KeepAlive extends FoxPlayer implements Listener {
	FoxAdditionAPI api = FoxAdditionAPI.INSTANCE;
	JavaPlugin plugin = api.getPlugin();
	String p = "mechanics.modules.keepalive";
	private final Map<Player, Integer> pC = new ConcurrentHashMap<>();
	private final Map<Player, Long> lastId = new ConcurrentHashMap<>();
	
	public KeepAlive() {
		FoliaScheduler.getGlobalRegionScheduler().runAtFixedRate(plugin, (ignored) -> pC.clear(), 20, 20);
	}

	// Runs on the netty thread from FoxPacketListener, before the server handles the packet, so the
	// return value can still cancel it. Flags and Bukkit calls are moved to the player's thread.
	public boolean onPacket(Player e, long packetId) {
        int count = pC.getOrDefault(e, 0);
        pC.put(e, count+1);
        int maxium = api.getFiles().getAC().getInt(p+".amount.max", 10);
        final Long id = lastId.get(e);
        if((id != null && id != 0L) && !iAD(e, "mechanics", p+".amount", true) && (count >= maxium)) {
        	flagLater(true, e, "Exceeds maxium amount of packets!", "Mechanics [KeepAlive]", "[packets:"+count+"/"+maxium+"]", api.getFiles().getAC().getInt(p+".amount.vls", 1), "mechanics");
        	if(api.getFiles().getAC().getBoolean(p+".amount.kick", false)) packetExceeded(e);
        	return true;
        }
        
        if(id == null) {
        	lastId.put(e, packetId);
        	return false;
        }
        if (id > packetId && !iAD(e, "mechanics", p+".checks.decreasing", true)) {
        	flagLater(false, e, "Sending decreased value in ID!", "Mechanics [KeepAlive]", "[id:"+id+" -> "+packetId+"]", api.getFiles().getAC().getInt(p+".checks.decreasing.vls", 1), "mechanics");
        	if(api.getFiles().getAC().getBoolean(p+".checks.decreasing.kick", false)) timeOut(e);
        	return true;
        }
        lastId.put(e, packetId);
        return false;
	}
	
	@Override
	protected void forget(Player e) {
		super.forget(e);
		pC.remove(e);
		lastId.remove(e);
	}
}
