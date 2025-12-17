package zoruafan.foxaddition.checks.mechanics;

import java.util.*;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import zoruafan.foxaddition.FoxAdditionAPI;
import zoruafan.foxaddition.utils.FoxPlayer;
import zoruafan.foxaddition.utils.listeners.PositionEvent;

public class Position extends FoxPlayer implements Listener {
	FoxAdditionAPI api = FoxAdditionAPI.INSTANCE;
	JavaPlugin plugin = api.getPlugin();
	String p = "mechanics.modules.position";
	boolean onGround = false;
	private final Map<Player, Integer> pC = new HashMap<>();
	
	public Position() {
		FoliaScheduler.getGlobalRegionScheduler().runAtFixedRate(plugin, (ignored) -> { for (Map.Entry<Player, Integer> entry : pC.entrySet()) entry.setValue(0); }, 20, 20);
	}
	
	private boolean isNaN(double pos) { return !Double.isNaN(pos); }
	
	@EventHandler
	public void onPosition(PositionEvent ev) {
		if (ev.isCancelled()) return;
		Player e = ev.getPlayer();
        if (!pC.containsKey(e)) pC.put(e, 0);
        int count = pC.get(e);
        pC.put(e, count+1);
        int maxium = api.getFiles().getAC().getInt(p+".amount.max", 300);
        if(!iAD(e, "mechanics", p+".amount", true) && (count >= maxium)) {
        	ev.setCancelled(true);
        	flag(true, e, "Exceeds maxium amount of packets!", "Mechanics [Position]", "[packets:"+count+"/"+maxium+"]", api.getFiles().getAC().getInt(p+".amount.vls", 1), "mechanics");
        	if(api.getFiles().getAC().getBoolean(p+".amount.kick", false)) packetExceeded(e);
        	return;
        }
        Location oldL = ev.getPlayer().getLocation();
        double oldX = ev.getPlayer().getLocation().getX();
        double oldY = ev.getPlayer().getLocation().getY();
        double oldZ = ev.getPlayer().getLocation().getZ();
        if ((e.isInsideVehicle() && !e.getVehicle().isValid()) && api.getFiles().getAC().getBoolean(p+".invalidvehicle", true)) {
        	flag(false, e, "Moved in invalid vehicle!", "Mechanics [Position]", "[inside:"+e.isInsideVehicle()+"] [isValid:"+e.getVehicle().isValid()+"]", 0, "mechanics");
        	ev.setCancelled(true);
        	if (FoliaScheduler.isFolia()) {
        		e.teleportAsync(oldL);
        	} else {
        		e.teleport(oldL);
        	}
        	try { e.getVehicle().eject(); } catch (Exception p) {}
        	return;
        } else if ((!e.isValid() && !e.isDead()) && api.getFiles().getAC().getBoolean(p+".invalidmove", true)) {
        	ev.setCancelled(true);
        	flag(false, e, "Moved in invalid state!", "Mechanics [Position]", "[isValid:"+e.isValid()+"] [isDead:"+e.isDead()+"]", 0, "mechanics");
        	closeConnection(e);
        	return;
        }
		
		double x = ev.getX();
		double y = ev.getY();
		double z = ev.getZ();
		double yaw = ev.getYaw();
		double pitch = ev.getPitch();
		float aYaw = (float) ((yaw % 360 + 360) % 360);
        double thresholdX = Math.abs(api.getFiles().getAC().getDouble(p+".checks.max.x", 2.9999999E7D));
        double thresholdY = Math.abs(api.getFiles().getAC().getDouble(p+".checks.max.y", 3.0E7));
        double thresholdZ = Math.abs(api.getFiles().getAC().getDouble(p+".checks.max.z", 2.9999999E7D));
        onGround = ev.isOnGround();
        
        double additional = api.getFiles().getAC().getDouble(p+".pullback.nosprint", 1.08);
        if(e.isSprinting()) additional = (additional+api.getFiles().getAC().getDouble(p+".pullback.sprint", .31));
        if(((y-1.00 == oldY || y > oldY+additional || oldY-y == api.getFiles().getAC().getDouble(p+".pullback.default", -1.0)) && (e.getGameMode() == GameMode.SURVIVAL || e.getGameMode() == GameMode.ADVENTURE && (!e.isFlying() && !e.getAllowFlight()))) && !iAD(e, "mechanics", p+".pullback", true)) { 
        	bif (FoliaScheduler.isFolia()) {
	        		FoliaScheduler.getGlobalRegionScheduler().run(api.getPlugin(), (FA) -> e.teleportAsync(oldL));
	        	} else {
	        		FoliaScheduler.getGlobalRegionScheduler().run(api.getPlugin(), (FA) -> e.teleport(oldL));
	        	}
        	if(api.getFiles().getAC().getBoolean(p+".pullback.ignore_velocity", true)) { if(e.getVelocity().getX() > 0.0 || e.getVelocity().getY() > 0.0 || e.getVelocity().getZ() > 0.0) { de = false; } }
        	if(de) {
	        	flag(true, e, "Detected using PullBack!", "Mechanics [Position]", "[Y:"+oldY+"/"+y+"] [diff:"+(oldY-y)+"]", 0, "mechanics");
	        	FoliaScheduler.getGlobalRegionScheduler().run(api.getPlugin(), (FA) -> e.teleportAsync(oldL));
	        	ev.setCancelled(true);
	        	return;
        	}
        }
    	if (!isNaN(x) || !isNaN(y) || !isNaN(z) && !iAD(e, "mechanics", p+".checks.nan", true)) {
        	ev.setCancelled(true);
        	flag(true, e, "Sending invalid NaN Position value.", "Mechanics [Position]", "[X:"+!isNaN(x)+", Y:"+!isNaN(y)+", Z:"+!isNaN(z)+"]", api.getFiles().getAC().getInt(p+".checks.nan.vls", 1), "mechanics");
            if(api.getFiles().getAC().getBoolean(p+".checks.nan.kick", false)) timeOut(e);
            return;
    	} else if (Math.abs(x) > thresholdX || Math.abs(y) > thresholdY || Math.abs(z) > thresholdZ && !iAD(e, "mechanics", p+".checks.max", true)) {
            ev.setCancelled(true);
            double diffX = Math.abs(x) - thresholdX;
            double diffY = Math.abs(y) - thresholdY;
            double diffZ = Math.abs(z) - thresholdZ;
            double abX = Math.abs(Math.abs(x) - thresholdX);
            double abY = Math.abs(Math.abs(y) - thresholdY);
            double abZ = Math.abs(Math.abs(z) - thresholdZ);
            flag(true, e, "Sending maxium movement in a direction!", "Mechanics [Position]", "[X:"+diffX+"/"+abX+", Y:"+diffY+"/"+abY+", Z:"+diffZ+"/"+abZ+"]", api.getFiles().getAC().getInt(p+".checks.nan.vls", 1), "mechanics");
            if(api.getFiles().getAC().getBoolean(p+".checks.max.kick", false)) timeOut(e);
    	} else if (Double.isInfinite(x) || Double.isInfinite(y) || Double.isInfinite(z) && !iAD(e, "mechanics", p+".checks.infinite", true)) {
    		ev.setCancelled(true);
    		flag(true, e, "Sending invalid Infinite Position value.", "Mechanics [Position]", "[X:"+Double.isInfinite(x)+", Y:"+Double.isInfinite(y)+", Z:"+Double.isInfinite(z)+"]", api.getFiles().getAC().getInt(p+".checks.nan.vls", 1), "mechanics");
    		if(api.getFiles().getAC().getBoolean(p+".checks.infinite.kick", false)) timeOut(e);
    		return;
    	} else if((oldY+api.getFiles().getAC().getInt(p+".checks.fast.y", 45) < y || oldX+api.getFiles().getAC().getInt(p+".checks.fast.x", 50) < x || oldZ+api.getFiles().getAC().getInt(p+".checks.fast.z", 50) < z) && !iAD(e, "mechanics", p+".checks.fast", false)) { 
        	ev.setCancelled(true);
         if (FoliaScheduler.isFolia()) {
        		FoliaScheduler.getGlobalRegionScheduler().run(api.getPlugin(), (FA) -> e.teleportAsync(oldL));
        	} else {
        		FoliaScheduler.getGlobalRegionScheduler().run(api.getPlugin(), (FA) -> e.teleport(oldL));
        	}
            double diffY = oldY+api.getFiles().getAC().getInt(p+".checks.fast.y", 45) - y;
            double diffZ = oldZ+api.getFiles().getAC().getInt(p+".checks.fast.z", 45) - z;
        	flag(true, e, "Sending fast movement in a direction!", "Mechanics [Position]", "[X:"+diffX+", Y:"+diffY+", Z:"+diffZ+"]", api.getFiles().getAC().getInt(p+".checks.fast.vls", 1), "mechanics");
        	FoliaScheduler.getGlobalRegionScheduler().run(api.getPlugin(), (FA) -> e.teleportAsync(oldL));
        	if(api.getFiles().getAC().getBoolean(p+".checks.fast.kick", false)) timeOut(e);
        	return;
    	} else if (((pitch > 90 || pitch < -90) || !isNaN(pitch) || Double.isInfinite(pitch)) || (!(aYaw > 0.0 || aYaw < 360.0) || !isNaN(aYaw) || Double.isInfinite(aYaw)) && !iAD(e, "mechanics", p+".checks.yawpitch", true)) {
            ev.setCancelled(true);
            flag(true, e, "Sending invalid Yaw/Pitch position!", "Mechanics [Position]", "", api.getFiles().getAC().getInt(p+".checks.yawpitch.vls", 2), "mechanics");
            if(api.getFiles().getAC().getBoolean(p+".checks.yawpitch.kick", false)) timeOut(e);
            return;
        }
	}
	
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerMove(final PlayerMoveEvent ev) {
        try {
        	Player e = ev.getPlayer();
	        Location to = ev.getTo();
	        World world = to.getWorld();
	        Chunk chunk = to.getChunk();
	        if ((chunk == null || !world.isChunkLoaded(chunk)) && api.getFiles().getAC().getBoolean(p+".nullchunk", true)) { flag(true, e, "Tried to move in a chunk unloaded!", "Mechanics [Position]", "", 0, "mechanics"); ev.setCancelled(true); }
        } catch (Exception p) {}
    }
}