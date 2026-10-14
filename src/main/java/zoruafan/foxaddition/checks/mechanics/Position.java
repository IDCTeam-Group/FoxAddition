package zoruafan.foxaddition.checks.mechanics;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.potion.PotionEffect;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import zoruafan.foxaddition.FoxAdditionAPI;
import zoruafan.foxaddition.utils.FoxPlayer;
import zoruafan.foxaddition.utils.listeners.PositionEvent;

public class Position extends FoxPlayer implements Listener {
	FoxAdditionAPI api = FoxAdditionAPI.INSTANCE;
	JavaPlugin plugin = api.getPlugin();
	String p = "mechanics.modules.position";
	boolean onGround = false;
	private final Map<Player, Integer> pC = new ConcurrentHashMap<>();
	private final Map<Player, double[]> lastPacketPosition = new ConcurrentHashMap<>();
	private final Map<Player, String> lastPacketWorld = new ConcurrentHashMap<>();
	private final Map<Player, Long> unconfirmedTeleport = new ConcurrentHashMap<>();
	private final Map<Player, Set<Integer>> unconfirmedTeleportIds = new ConcurrentHashMap<>();
	private final Set<Player> teleportJustConfirmed = ConcurrentHashMap.newKeySet();
	private Boolean legacyServer;
	
	public Position() {
		FoliaScheduler.getGlobalRegionScheduler().runAtFixedRate(plugin, (ignored) -> pC.clear(), 20, 20);
	}
	
	
	@EventHandler
	public void onPosition(PositionEvent ev) {
		if (ev.isCancelled()) return;
		Player e = ev.getPlayer();
        // This runs a tick after the packet, when the server has already applied the move, so the
        // location from before the packet is captured when it arrives (FoxPacketListener).
        Location oldL = ev.getPreviousLocation() != null ? ev.getPreviousLocation() : e.getLocation();
        double oldX = oldL.getX();
        double oldY = oldL.getY();
        double oldZ = oldL.getZ();
        // The one packet that may carry a teleport's jump (see isTeleportJump).
        boolean teleported = ev.isAfterTeleport();
        // isValid() is also false while the chunk under the player isn't loaded yet, e.g. right after a
        // long teleport, which kicked players for "moving in an invalid state".
        boolean chunkLoaded = e.getWorld().isChunkLoaded(e.getLocation().getBlockX() >> 4, e.getLocation().getBlockZ() >> 4);
        if (!teleported && chunkLoaded && (e.isInsideVehicle() && !e.getVehicle().isValid()) && api.getFiles().getAC().getBoolean(p+".invalidvehicle", true)) {
        	flag(false, e, "Moved in invalid vehicle!", "Mechanics [Position]", "[inside:"+e.isInsideVehicle()+"] [isValid:"+e.getVehicle().isValid()+"]", 0, "mechanics");
        	ev.setCancelled(true);
        	if (FoliaScheduler.isFolia()) {
        		e.teleportAsync(oldL);
        	} else {
        		e.teleport(oldL);
        	}
        	try { e.getVehicle().eject(); } catch (Exception p) {}
        	return;
        } else if (!teleported && chunkLoaded && (!e.isValid() && !e.isDead()) && api.getFiles().getAC().getBoolean(p+".invalidmove", true)) {
        	ev.setCancelled(true);
        	flag(false, e, "Moved in invalid state!", "Mechanics [Position]", "[isValid:"+e.isValid()+"] [isDead:"+e.isDead()+"]", 0, "mechanics");
        	closeConnection(e);
        	return;
        }
		
		double x = ev.getX();
		double y = ev.getY();
		double z = ev.getZ();
        onGround = ev.isOnGround();
        
        double additional = api.getFiles().getAC().getDouble(p+".pullback.nosprint", 1.08);
        if(e.isSprinting()) additional = (additional+api.getFiles().getAC().getDouble(p+".pullback.sprint", .31));
        if(!teleported && ((Math.abs(y-1.00-oldY) < 1.0E-6 || y > oldY+additional || Math.abs(oldY-y-api.getFiles().getAC().getDouble(p+".pullback.default", -1.0)) < 1.0E-6) && (e.getGameMode() == GameMode.SURVIVAL || e.getGameMode() == GameMode.ADVENTURE) && !e.isFlying() && !e.getAllowFlight() && !canMoveUpFast(e, oldL)) && !iAD(e, "mechanics", p+".pullback", true)) { 
        	boolean de = true;
        	if(api.getFiles().getAC().getBoolean(p+".pullback.ignore_velocity", true)) { if(hasExternalVelocity(e, ev.getPreviousVelocity() != null ? ev.getPreviousVelocity() : e.getVelocity(), ev.getReceivedAt())) { de = false; } }
        	if(de) {
	        	flag(true, e, "Detected using PullBack!", "Mechanics [Position]", "[Y:"+oldY+"/"+y+"] [diff:"+(oldY-y)+"]", 0, "mechanics");
	        	if (FoliaScheduler.isFolia()) {
	        		FoliaScheduler.getGlobalRegionScheduler().run(api.getPlugin(), (FA) -> e.teleportAsync(oldL));
	        	} else {
	        		FoliaScheduler.getGlobalRegionScheduler().run(api.getPlugin(), (FA) -> e.teleport(oldL));
	        	}
	        	ev.setCancelled(true);
	        	return;
        	}
        }
    	if(!teleported && (Math.abs(y-oldY) > api.getFiles().getAC().getInt(p+".checks.fast.y", 45) || Math.abs(x-oldX) > api.getFiles().getAC().getInt(p+".checks.fast.x", 50) || Math.abs(z-oldZ) > api.getFiles().getAC().getInt(p+".checks.fast.z", 50)) && !iAD(e, "mechanics", p+".checks.fast", false)) { 
        	ev.setCancelled(true);
            double diffX = x - oldX;
            double diffY = y - oldY;
            double diffZ = z - oldZ;
        	flag(true, e, "Sending fast movement in a direction!", "Mechanics [Position]", "[X:"+diffX+", Y:"+diffY+", Z:"+diffZ+"]", api.getFiles().getAC().getInt(p+".checks.fast.vls", 1), "mechanics");
        	if (FoliaScheduler.isFolia()) {
        		FoliaScheduler.getGlobalRegionScheduler().run(api.getPlugin(), (FA) -> e.teleportAsync(oldL));
        	} else {
        		FoliaScheduler.getGlobalRegionScheduler().run(api.getPlugin(), (FA) -> e.teleport(oldL));
        	}
        	if(api.getFiles().getAC().getBoolean(p+".checks.fast.kick", false)) timeOut(e);
        	return;
    	}
	}
	
	// Runs on the netty thread from FoxPacketListener, before the server handles the packet, so the
	// return value can still cancel it. Flags and Bukkit calls are moved to the player's thread.
	static boolean isInvalidRotation(float yaw, float pitch) {
		float aYaw = (float) ((yaw % 360 + 360) % 360);
		return ((pitch > 90 || pitch < -90) || Float.isNaN(pitch) || Float.isInfinite(pitch)) || (!(aYaw > 0.0 || aYaw < 360.0) || Float.isNaN(aYaw) || Float.isInfinite(aYaw));
	}
	
	public boolean onPacket(Player e, boolean hasPosition, double x, double y, double z, boolean hasRotation, float yaw, float pitch) {
		if (hasPosition) {
			int count = pC.getOrDefault(e, 0);
			pC.put(e, count+1);
			int maxium = api.getFiles().getAC().getInt(p+".amount.max", 300);
			if(!iAD(e, "mechanics", p+".amount", true) && (count >= maxium)) {
				flagLater(true, e, "Exceeds maxium amount of packets!", "Mechanics [Position]", "[packets:"+count+"/"+maxium+"]", api.getFiles().getAC().getInt(p+".amount.vls", 1), "mechanics");
				if(api.getFiles().getAC().getBoolean(p+".amount.kick", false)) packetExceeded(e);
				return true;
			}
			double thresholdX = Math.abs(api.getFiles().getAC().getDouble(p+".checks.max.x", 2.9999999E7D));
			double thresholdY = Math.abs(api.getFiles().getAC().getDouble(p+".checks.max.y", 3.0E7));
			double thresholdZ = Math.abs(api.getFiles().getAC().getDouble(p+".checks.max.z", 2.9999999E7D));
			if ((Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z)) && !iAD(e, "mechanics", p+".checks.nan", true)) {
				flagLater(true, e, "Sending invalid NaN Position value.", "Mechanics [Position]", "[X:"+Double.isNaN(x)+", Y:"+Double.isNaN(y)+", Z:"+Double.isNaN(z)+"]", api.getFiles().getAC().getInt(p+".checks.nan.vls", 1), "mechanics");
				if(api.getFiles().getAC().getBoolean(p+".checks.nan.kick", false)) timeOut(e);
				return true;
			} else if ((Math.abs(x) > thresholdX || Math.abs(y) > thresholdY || Math.abs(z) > thresholdZ) && !iAD(e, "mechanics", p+".checks.max", true)) {
				double diffX = Math.abs(x) - thresholdX;
				double diffY = Math.abs(y) - thresholdY;
				double diffZ = Math.abs(z) - thresholdZ;
				double abX = Math.abs(Math.abs(x) - thresholdX);
				double abY = Math.abs(Math.abs(y) - thresholdY);
				double abZ = Math.abs(Math.abs(z) - thresholdZ);
				flagLater(true, e, "Sending maxium movement in a direction!", "Mechanics [Position]", "[X:"+diffX+"/"+abX+", Y:"+diffY+"/"+abY+", Z:"+diffZ+"/"+abZ+"]", api.getFiles().getAC().getInt(p+".checks.nan.vls", 1), "mechanics");
				if(api.getFiles().getAC().getBoolean(p+".checks.max.kick", false)) timeOut(e);
				return true;
			} else if ((Double.isInfinite(x) || Double.isInfinite(y) || Double.isInfinite(z)) && !iAD(e, "mechanics", p+".checks.infinite", true)) {
				flagLater(true, e, "Sending invalid Infinite Position value.", "Mechanics [Position]", "[X:"+Double.isInfinite(x)+", Y:"+Double.isInfinite(y)+", Z:"+Double.isInfinite(z)+"]", api.getFiles().getAC().getInt(p+".checks.nan.vls", 1), "mechanics");
				if(api.getFiles().getAC().getBoolean(p+".checks.infinite.kick", false)) timeOut(e);
				return true;
			}
		}
		if (hasRotation) {
			if (isInvalidRotation(yaw, pitch) && !iAD(e, "mechanics", p+".checks.yawpitch", true)) {
				flagLater(true, e, "Sending invalid Yaw/Pitch position!", "Mechanics [Position]", "", api.getFiles().getAC().getInt(p+".checks.yawpitch.vls", 2), "mechanics");
				if(api.getFiles().getAC().getBoolean(p+".checks.yawpitch.kick", false)) timeOut(e);
				return true;
			}
		}
		return false;
	}
	
    // Legit ways to move up more than a jump within one packet. Methods that older servers lack
    // (isGliding 1.9, isRiptiding 1.13) just count as "no".
    private static boolean canMoveUpFast(Player e, Location from) {
    	if (e.isInsideVehicle()) return true;
    	try { if (e.isGliding()) return true; } catch (Throwable ignored) {}
    	try { if (e.isRiptiding()) return true; } catch (Throwable ignored) {}
    	for (PotionEffect effect : e.getActivePotionEffects()) {
    		String type = String.valueOf(effect.getType()).toUpperCase(Locale.ROOT);
    		try { type += effect.getType().getName().toUpperCase(Locale.ROOT); } catch (Throwable ignored) {}
    		if (type.contains("JUMP") || type.contains("LEVITATION")) return true;
    	}
    	String feet = from.getBlock().getType().name();
    	String below = from.getBlock().getRelative(BlockFace.DOWN).getType().name();
    	return below.contains("SLIME") || below.contains("BED") || feet.contains("BUBBLE_COLUMN") || feet.contains("SLIME");
    }
    
    // FoxPacketListener calls these on the netty thread. Pullback and fast movement compare a movement
    // packet with the client's previous one, not with the server's location: after a teleport the server
    // (on Folia even before it sends the teleport) already has the new location while the client keeps
    // sending its old path until it gets the teleport. The client's own positions jump exactly once,
    // with the first packet after it confirmed the teleport (before 1.9, which has no confirmation,
    // within a second after it got it), and that packet is not checked.
    public void onServerTeleport(Player e, int teleportId) {
    	unconfirmedTeleportIds.computeIfAbsent(e, k -> ConcurrentHashMap.newKeySet()).add(teleportId);
    	unconfirmedTeleport.put(e, System.currentTimeMillis());
    }
    
    // With several teleports in a row the client jumps once per teleport, so every confirmation of a
    // teleport the server really sent frees the next packet; made-up IDs free nothing.
    public void onTeleportConfirm(Player e, int teleportId) {
    	Set<Integer> ids = unconfirmedTeleportIds.get(e);
    	if (ids == null || !ids.remove(teleportId)) return;
    	teleportJustConfirmed.add(e);
    	if (ids.isEmpty()) unconfirmedTeleport.remove(e);
    }
    
    public boolean isTeleportJump(Player e) {
    	if (teleportJustConfirmed.remove(e)) return true;
    	Long at = unconfirmedTeleport.get(e);
    	if (at == null) return false;
    	if (legacyServer == null) {
    		try { legacyServer = api.getVersion().isOlderThan(ServerVersion.V_1_9); } catch (Throwable t) { legacyServer = false; }
    	}
    	long age = System.currentTimeMillis() - at;
    	if (age > (legacyServer ? 1000L : 5000L)) {
    		unconfirmedTeleport.remove(e);
    		unconfirmedTeleportIds.remove(e);
    		return false;
    	}
    	return legacyServer;
    }
    
    // The client's previous position in the same world, and remembers this one for the next packet.
    public Location previousPacketPosition(Player e, double x, double y, double z) {
    	World world = e.getWorld();
    	double[] previous = lastPacketPosition.put(e, new double[] { x, y, z });
    	String previousWorld = lastPacketWorld.put(e, world.getName());
    	if (previous == null || !world.getName().equals(previousWorld)) return null;
    	return new Location(world, previous[0], previous[1], previous[2]);
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
	
	@Override
	protected void forget(Player e) {
		super.forget(e);
		pC.remove(e);
		lastPacketPosition.remove(e);
		lastPacketWorld.remove(e);
		unconfirmedTeleport.remove(e);
		unconfirmedTeleportIds.remove(e);
		teleportJustConfirmed.remove(e);
	}
}
