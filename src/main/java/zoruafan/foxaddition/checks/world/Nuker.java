package zoruafan.foxaddition.checks.world;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BlockIterator;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import zoruafan.foxaddition.FoxAdditionAPI;
import zoruafan.foxaddition.utils.FoxPlayer;
import zoruafan.foxaddition.utils.listeners.BlockDigEvent;

public class Nuker extends FoxPlayer implements Listener {
	FoxAdditionAPI api = FoxAdditionAPI.INSTANCE;
	JavaPlugin plugin = api.getPlugin();
	String p = "world.modules.nuker";
	private final Map<Player, Integer> pC = new HashMap<>();
	private int ms = 200;
	private Map<Player, Long> lBT = new HashMap<>();
	
	public Nuker() {
		FoliaScheduler.getGlobalRegionScheduler().runAtFixedRate(plugin, (ignored) -> { for (Map.Entry<Player, Integer> entry : pC.entrySet()) entry.setValue(0); }, 20, 20);
	}

	@EventHandler
	public void onBlockPlace(BlockDigEvent ev) {
		if (ev.isCancelled()) return;
		Player e = ev.getPlayer();
		if (ev.getAction() != DiggingAction.FINISHED_DIGGING && ev.getAction() != DiggingAction.CANCELLED_DIGGING) { return; }
		if ((!e.isValid() && !e.isDead()) && api.getFiles().getAC().getBoolean(p+".dead", true)) { ev.setCancelled(true); return; }
		long currentTime = System.currentTimeMillis();
	   	if (lBT.containsKey(e)) {
	       	long lastBreakTime = lBT.get(e);
	       	if (currentTime - lastBreakTime < ms) { startCooldown(e); ev.setCancelled(true); return; }
	       	else { lBT.remove(e); return; }
	  	}
	   	if (!(ev.getPlayer().getType() == EntityType.PLAYER)) { return; }
        if (!pC.containsKey(e)) pC.put(e, 0);
		int x = ev.getBlockPosition().getX();
  		int y = ev.getBlockPosition().getY();
     	int z = ev.getBlockPosition().getZ();
      	Block b = e.getWorld().getBlockAt(x, y, z);
        if(api.getFiles().getAC().getBoolean(p+".blocklist.enable", false)) { if (!isAllowed(e, b.getType())) { return; } }
        int count = pC.get(e);
        pC.put(e, count+1);
        int maxium = api.getFiles().getAC().getInt(p+".flood.max", 20);
        if(!iAD(e, "world", p+".flood", true) && (count >= maxium)) {
        	flag(true, e, "Exceeds maxium amount of packets!", "World (Nuker) [Flood]", "[packets:"+count+"/"+maxium+"] [isBedrock:"+api.getGeyser().iB(e)+"]", api.getFiles().getAC().getInt(p+".flood.vls", 4), "world");
        	int vls = api.getVLS(e, "world");
        	if(api.getFiles().getAC().getBoolean(p+".flood.cancel.enable", true) && vls >=api.getFiles().getAC().getInt(p+".flood.cancel.vl", 0)) { ev.setCancelled(true); if(api.getFiles().getAC().getBoolean(p+".force_cancel.enable", true)) { startCooldown(e); }}
        	if(api.getFiles().getAC().getBoolean(p+".kick", false)) packetExceeded(e);
        	return;
        }
        Location tL = gPTL(e);
     	if (!(tL != null)) return;
     	Block tB = tL.getBlock();
     	if (b == null || tB == null) return;
      	double d = tB.getLocation().distance(b.getLocation());
        double dX = Math.abs(tB.getX() - b.getX());
        double dZ = Math.abs(tB.getZ() - b.getZ());
        double ds = Math.sqrt(dX * dX + dZ * dZ);
        boolean ib = api.getGeyser().iB(e);
        String bde = api.getGeyser().getDevice(e);
    	double a3d = 3.0;
    	double adi = 1.42;
        if(!ib || !api.getFiles().getAC().getBoolean(p+".angle.values.bedrock.devices."+bde.toLowerCase(), true)) {
        	a3d = api.getFiles().getAC().getDouble(p+".angle.values.java.3d_distance", 3.0);
        	adi = api.getFiles().getAC().getDouble(p+".angle.values.java.plane_distance", 1.42);
        } else {
        	a3d = api.getFiles().getAC().getDouble(p+".angle.values.bedrock.3d_distance", 3.0);
        	adi = api.getFiles().getAC().getDouble(p+".angle.values.bedrock.plane_distance", 1.42);
        }
        this.ms = api.getFiles().getAC().getInt(p+".force_cancel.ms", 200);
      	if (!iAD(e, "world", p+".angle", true) && !b.equals(tB) && (d >= adi && ds >= a3d)) {
      		flag(true, e, "Destroying blocks in a wrong angle!", "World (Nuker) [Angle]", "[T1="+b.getType()+"] [T2="+tB.getType()+"] [DI:"+d+"/"+adi+"] [3D:"+ds+"/"+a3d+"] [isBedrock:"+ib+"]", api.getFiles().getAC().getInt(p+".angle.vls", 2), "world");
      		int vls = api.getVLS(e, "world");
      		if(api.getFiles().getAC().getBoolean(p+".angle.cancel.enable", true) && vls >=api.getFiles().getAC().getInt(p+".angle.cancel.vl", 0)) { ev.setCancelled(true); if(api.getFiles().getAC().getBoolean(p+".force_cancel.enable", true)) { startCooldown(e); }}
        	if(api.getFiles().getAC().getBoolean(p+".kick", false)) timeOut(e);
        	return;
      	}
	}
	
    private boolean isAllowed(Player e, Material block) {
        List<String> list = api.getFiles().getAC().getStringList(p+".blocklist.list");
        String type = api.getFiles().getAC().getString(p+".blocklist.type", "whitelist");        
        if (type.equalsIgnoreCase("whitelist")) { return list.contains(block.toString().toUpperCase()); }
        else if (type.equalsIgnoreCase("blacklist")) { return !list.contains(block.toString().toUpperCase()); }
        else {
        	plugin.getLogger().severe("[NUKER] Invalid option in 'type'. Use 'whitelist'/'blacklist' for this option.");
        	plugin.getLogger().severe("[NUKER] The nuker checker don't works correctly.");
            return false;
        }
    }
    
	private Location gPTL(Player player) {
		BlockIterator iterator = new BlockIterator(player, 100);
		while (iterator.hasNext()) {
	      	Block b = iterator.next();
	        if (b == null) continue;
	        World w = b.getWorld();
	        if (w == null) continue;
	       	if (b.getType() != Material.AIR && b.getType().isSolid()) return b.getLocation();
	    }
		return null;
	}
	  
	private void startCooldown(Player player) {
	  	lBT.put(player, System.currentTimeMillis()+ms);
	}
	   
	@EventHandler
	public void onBlockBreak(BlockBreakEvent ev) {
	  	Player e = ev.getPlayer();
	  	if(!(ev.getPlayer().getType() == EntityType.PLAYER)) return;
	  	if ((!e.isValid() && !e.isDead()) && api.getFiles().getAC().getBoolean(p+".dead", true)) { ev.setCancelled(true); return; }
	   	if (lBT.containsKey(e)) {
	       	long lastBreakTime = lBT.get(e);
	      	long currentTime = System.currentTimeMillis();
	       	if (currentTime - lastBreakTime < ms) { startCooldown(e); ev.setCancelled(true); return; }
	       	else { lBT.remove(e); return; }
	  	}
	}
}