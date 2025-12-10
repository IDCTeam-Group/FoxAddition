package zoruafan.foxaddition.checks.mechanics;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.util.Vector3i;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import zoruafan.foxaddition.FoxAdditionAPI;
import zoruafan.foxaddition.utils.FoxPlayer;
import zoruafan.foxaddition.utils.listeners.BlockPlaceEvent;

public class BlockPlace extends FoxPlayer implements Listener {
	FoxAdditionAPI api = FoxAdditionAPI.INSTANCE;
	JavaPlugin plugin = api.getPlugin();
	String p = "mechanics.modules.blockplace";
	private final Map<Player, Integer> pC = new HashMap<>();
	
	public BlockPlace() {
		FoliaScheduler.getGlobalRegionScheduler().runAtFixedRate(plugin, (ignored) -> { for (Map.Entry<Player, Integer> entry : pC.entrySet()) entry.setValue(0); }, 20, 20);
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent ev) {
		if (ev.isCancelled()) return;
		Player e = ev.getPlayer();
        if (!pC.containsKey(e)) pC.put(e, 0);
        int count = pC.get(e);
        pC.put(e, count+1);
        int maxium = api.getFiles().getAC().getInt(p+".amount.max", 15);
        if(!iAD(e, "mechanics", p+".amount", true) && (count >= maxium)) {
        	ev.setCancelled(true);
        	flag(true, e, "Exceeds maxium amount of packets!", "Mechanics [BlockPlace]", "[packets:"+count+"/"+maxium+"]", api.getFiles().getAC().getInt(p+".amount.vls", 1), "mechanics");
        	if(api.getFiles().getAC().getBoolean(p+".amount.kick", false)) packetExceeded(e);
        	return;
        }
        
        Vector3i bP = ev.getBlockPosition();
        BlockFace f = ev.getFace();
        long tm = ev.getSequence();
        
        if((!isNaN(bP.x) || !isNaN(bP.y) || !isNaN(bP.z)) && api.getFiles().getAC().getBoolean(p+".checks.invalid.enable.nan", true)) {
        	ev.setCancelled(true);
        	flag(false, e, "Sended NaN value in a placement!", "Mechanics [BlockPlace]", "[blockPosition:"+bP.x+", "+bP.y+", "+bP.z+"] [face:"+f+"] [timestamp:"+tm+"]", api.getFiles().getAC().getInt(p+".checks.invalid.vls", 1), "mechanics");
        	if(api.getFiles().getAC().getBoolean(p+".checks.invalid.kick", false)) timeOut(e);
        	return;
        } else if((Double.isInfinite(bP.x) || Double.isInfinite(bP.y) || Double.isInfinite(bP.z)) && api.getFiles().getAC().getBoolean(p+".checks.invalid.enable.infinite", true)) {
        	ev.setCancelled(true);
        	flag(false, e, "Sended infinite value in a placement!", "Mechanics [BlockPlace]", "[blockPosition:"+bP.x+", "+bP.y+", "+bP.z+"] [face:"+f+"] [timestamp:"+tm+"]", api.getFiles().getAC().getInt(p+".checks.invalid.vls", 1), "mechanics");
        	if(api.getFiles().getAC().getBoolean(p+".checks.invalid.kick", false)) timeOut(e);
        	return;
        }
	}
	
	private boolean isNaN(double pos) { return !Double.isNaN(pos); }
}
