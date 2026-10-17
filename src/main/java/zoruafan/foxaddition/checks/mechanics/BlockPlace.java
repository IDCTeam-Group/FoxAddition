package zoruafan.foxaddition.checks.mechanics;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.util.Vector3i;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import zoruafan.foxaddition.FoxAdditionAPI;
import zoruafan.foxaddition.utils.FoxPlayer;

public class BlockPlace extends FoxPlayer implements Listener {
	FoxAdditionAPI api = FoxAdditionAPI.INSTANCE;
	JavaPlugin plugin = api.getPlugin();
	String p = "mechanics.modules.blockplace";
	private final Map<Player, Integer> pC = new ConcurrentHashMap<>();
	
	public BlockPlace() {
		FoliaScheduler.getGlobalRegionScheduler().runAtFixedRate(plugin, (ignored) -> pC.clear(), 20, 20);
	}
	
	// Runs on the netty thread from FoxPacketListener, before the server handles the packet, so the
	// return value can still cancel it. Flags and Bukkit calls are moved to the player's thread.
	public boolean onPacket(Player e, Vector3i bP, BlockFace f, long tm, Vector3f cur) {
        int count = pC.getOrDefault(e, 0);
        pC.put(e, count+1);
        int maxium = api.getFiles().getAC().getInt(p+".amount.max", 15);
        if(!iAD(e, "mechanics", p+".amount", true) && (count >= maxium)) {
        	flagLater(true, e, "Exceeds maxium amount of packets!", "Mechanics [BlockPlace]", "[packets:"+count+"/"+maxium+"]", api.getFiles().getAC().getInt(p+".amount.vls", 1), "mechanics");
        	if(api.getFiles().getAC().getBoolean(p+".amount.kick", false)) packetExceeded(e);
        	return true;
        }
        // The block position is an int vector and can't be NaN or infinite; the cursor position is a float vector.
        if (cur == null) return false;
        
        if((Float.isNaN(cur.x) || Float.isNaN(cur.y) || Float.isNaN(cur.z)) && api.getFiles().getAC().getBoolean(p+".checks.invalid.enable.nan", true)) {
        	flagLater(false, e, "Sended NaN value in a placement!", "Mechanics [BlockPlace]", "[blockPosition:"+bP.x+", "+bP.y+", "+bP.z+"] [cursor:"+cur.x+", "+cur.y+", "+cur.z+"] [face:"+f+"] [timestamp:"+tm+"]", api.getFiles().getAC().getInt(p+".checks.invalid.vls", 1), "mechanics");
        	if(api.getFiles().getAC().getBoolean(p+".checks.invalid.kick", false)) timeOut(e);
        	return true;
        } else if((Float.isInfinite(cur.x) || Float.isInfinite(cur.y) || Float.isInfinite(cur.z)) && api.getFiles().getAC().getBoolean(p+".checks.invalid.enable.infinite", true)) {
        	flagLater(false, e, "Sended infinite value in a placement!", "Mechanics [BlockPlace]", "[blockPosition:"+bP.x+", "+bP.y+", "+bP.z+"] [cursor:"+cur.x+", "+cur.y+", "+cur.z+"] [face:"+f+"] [timestamp:"+tm+"]", api.getFiles().getAC().getInt(p+".checks.invalid.vls", 1), "mechanics");
        	if(api.getFiles().getAC().getBoolean(p+".checks.invalid.kick", false)) timeOut(e);
        	return true;
        }
        return false;
	}
	
	@Override
	protected void forget(Player e) {
		super.forget(e);
		pC.remove(e);
	}
}
