package zoruafan.foxaddition.utils;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientAnimation;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientAttack;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction.Action;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientHeldItemChange;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity.InteractAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientKeepAlive;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerAbilities;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerPosition;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerPositionAndRotation;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerRotation;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSteerVehicle;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientTeleportConfirm;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerPositionAndLook;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.bukkit.event.HandlerList;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import zoruafan.foxaddition.FoxAdditionAPI;
import zoruafan.foxaddition.checks.badpackets.BadPacketsB;
import zoruafan.foxaddition.checks.badpackets.BadPacketsC;
import zoruafan.foxaddition.checks.badpackets.BadPacketsD;
import zoruafan.foxaddition.checks.badpackets.BadPacketsE;
import zoruafan.foxaddition.checks.badpackets.BadPacketsF;
import zoruafan.foxaddition.checks.badpackets.BadPacketsH;
import zoruafan.foxaddition.checks.mechanics.Abilities;
import zoruafan.foxaddition.checks.mechanics.BlockPlace;
import zoruafan.foxaddition.checks.mechanics.KeepAlive;
import zoruafan.foxaddition.checks.mechanics.Position;
import zoruafan.foxaddition.utils.listeners.*;
import java.util.Optional;

public class FoxPacketListener extends FoliaScheduler implements PacketListener {
    FoxAdditionAPI api = FoxAdditionAPI.INSTANCE;
    JavaPlugin pl = api.getPlugin();
    private static final boolean IS_FOLIA = checkFolia();
    // Since 26.1 attacks have their own packet instead of INTERACT_ENTITY. Looked up by name, so
    // packetevents versions that don't know it yet (older than the 26.1 support) still work.
    private static final PacketTypeCommon ATTACK = clientPacketType("ATTACK");
    
    private static PacketTypeCommon clientPacketType(String name) {
        try {
            return (PacketTypeCommon) PacketType.Play.Client.class.getField(name).get(null);
        } catch (Throwable t) {
            return null;
        }
    }
    // Checks that only need the packet itself. They run right here on the netty thread, so a
    // violation can still cancel the packet; the Bukkit events below arrive a tick too late for that.
    private BadPacketsB badPacketsB;
    private BadPacketsC badPacketsC;
    private BadPacketsD badPacketsD;
    private BadPacketsE badPacketsE;
    private BadPacketsF badPacketsF;
    private BadPacketsH badPacketsH;
    private KeepAlive keepAlive;
    private Position position;
    private BlockPlace blockPlace;
    private Abilities abilities;
    
    public FoxPacketListener(Object[] checks) {
        for (Object c : checks) {
            if (c instanceof BadPacketsB) badPacketsB = (BadPacketsB) c;
            else if (c instanceof BadPacketsC) badPacketsC = (BadPacketsC) c;
            else if (c instanceof BadPacketsD) badPacketsD = (BadPacketsD) c;
            else if (c instanceof BadPacketsE) badPacketsE = (BadPacketsE) c;
            else if (c instanceof BadPacketsF) badPacketsF = (BadPacketsF) c;
            else if (c instanceof BadPacketsH) badPacketsH = (BadPacketsH) c;
            else if (c instanceof KeepAlive) keepAlive = (KeepAlive) c;
            else if (c instanceof Position) position = (Position) c;
            else if (c instanceof BlockPlace) blockPlace = (BlockPlace) c;
            else if (c instanceof Abilities) abilities = (Abilities) c;
        }
    }
    
    private static boolean checkFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    // Every dispatched event costs a scheduled task; skip the ones nothing listens to (other plugins
    // can still listen to them, then they are fired as before).
    private static boolean hasListeners(HandlerList handlers) {
        return handlers.getRegisteredListeners().length > 0;
    }
    
    private void runSync(Player player, PacketReceiveEvent ev, Runnable task) {
        if (!pl.isEnabled()) {
            return;
        }
        // The task runs a tick later; skip it if the player quit in between, so per-player state is not recreated.
        final Runnable guarded = () -> { if (player.isOnline()) task.run(); };
        if (IS_FOLIA) {
            player.getScheduler().run(pl, t -> guarded.run(), null);
        } else {
            Bukkit.getScheduler().runTask(pl, guarded);
        }
    }
    
    // The server sends this for every teleport. Until the client confirms it, the client's movement
    // packets can still carry the position from before the teleport (see Position).
    @Override
    public void onPacketSend(PacketSendEvent ev) {
    	if (position == null || ev.getPacketType() != PacketType.Play.Server.PLAYER_POSITION_AND_LOOK) return;
    	User user = ev.getUser();
    	if (user == null || user.getUUID() == null) return;
    	Player player = Bukkit.getPlayer(user.getUUID());
    	if (player != null) position.onServerTeleport(player, new WrapperPlayServerPlayerPositionAndLook(ev).getTeleportId());
    }
    
    public void onPacketReceive(PacketReceiveEvent ev) {
    	User user = ev.getUser();
    	if (user.getUUID() == null) { return; }
        Player e = Bukkit.getPlayer(user.getUUID());
        if (e == null) { return; }
        PacketTypeCommon p = ev.getPacketType();
        
        if (p == PacketType.Play.Client.TELEPORT_CONFIRM) {
        	if (position != null) position.onTeleportConfirm(e, new WrapperPlayClientTeleportConfirm(ev).getTeleportId());
        	return;
        }
        if (p == PacketType.Play.Client.PLAYER_ABILITIES) {
        	WrapperPlayClientPlayerAbilities a = new WrapperPlayClientPlayerAbilities(ev);
    	    final Optional<Boolean> flying = Optional.of(a.isFlying());
    	    final Optional<Boolean> godMode = a.isInGodMode();
    	    final Optional<Boolean> flightAllowed = a.isFlightAllowed();
    	    final Optional<Boolean> creativeMode = a.isInCreativeMode();
    	    final Optional<Float> flySpeed = a.getFlySpeed();
    	    final Optional<Float> walkSpeed = a.getWalkSpeed();
    	    if (abilities != null && abilities.onPacket(e, flightAllowed.orElse(false), a.isFlying(), creativeMode.orElse(false), godMode.orElse(false))) { ev.setCancelled(true); return; }
		    final Player finalPlayer = e;
		    final PacketReceiveEvent finalEv = ev;
		    if (!hasListeners(AbilitiesEvent.getHandlerList())) return;
		    runSync(e, ev, () -> {
			    AbilitiesEvent abi = new AbilitiesEvent(finalPlayer, flying, godMode, flightAllowed, creativeMode, flySpeed, walkSpeed);
			    Bukkit.getServer().getPluginManager().callEvent(abi);
			    if (abi.isCancelled()) { finalEv.setCancelled(true); }
		    });
        } else if (p == PacketType.Play.Client.PLAYER_DIGGING) {
        	WrapperPlayClientPlayerDigging d = new WrapperPlayClientPlayerDigging(ev);
        	Vector3i b = d.getBlockPosition();
    		int x = b.getX();
    		int y = b.getY();
    		int z = b.getZ();
        	final Vector3i bP = new Vector3i(x, y, z);
        	final BlockFace f = d.getBlockFace();
        	final DiggingAction a = d.getAction();
        	final Player finalPlayer = e;
        	final PacketReceiveEvent finalEv = ev;
        	if (!hasListeners(BlockDigEvent.getHandlerList())) return;
        	runSync(e, ev, () -> {
	        	BlockDigEvent bde = new BlockDigEvent(finalPlayer, a, bP, f);
		        Bukkit.getServer().getPluginManager().callEvent(bde);
		        if (bde.isCancelled()) { finalEv.setCancelled(true); }
        	});
        } else if (p == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
        	WrapperPlayClientPlayerBlockPlacement bp = new WrapperPlayClientPlayerBlockPlacement(ev);
        	Vector3i b = bp.getBlockPosition();
    		int x = b.getX();
    		int y = b.getY();
    		int z = b.getZ();
        	final Vector3i bP = new Vector3i(x, y, z);
        	final BlockFace f = bp.getFace();
        	final long tm = bp.getSequence();
        	final Vector3f cursor = bp.getCursorPosition();
        	if (blockPlace != null && blockPlace.onPacket(e, bP, f, tm, cursor)) { ev.setCancelled(true); return; }
        	final InteractionHand hand = bp.getHand();
        	final Player finalPlayer = e;
        	final PacketReceiveEvent finalEv = ev;
        	if (!hasListeners(BlockPlaceEvent.getHandlerList())) return;
        	runSync(e, ev, () -> {
	        	BlockPlaceEvent bpe = new BlockPlaceEvent(finalPlayer, bP, f, tm, hand, cursor);
	        	Bukkit.getServer().getPluginManager().callEvent(bpe);
	        	if (bpe.isCancelled()) { finalEv.setCancelled(true); }
        	});
        } else if (p == PacketType.Play.Client.KEEP_ALIVE) {
        	WrapperPlayClientKeepAlive k = new WrapperPlayClientKeepAlive(ev);
        	final long id = k.getId();
        	if (keepAlive != null && keepAlive.onPacket(e, id)) { ev.setCancelled(true); return; }
        	final Player finalPlayer = e;
        	final PacketReceiveEvent finalEv = ev;
        	if (!hasListeners(KeepAliveEvent.getHandlerList())) return;
        	runSync(e, ev, () -> {
	        	KeepAliveEvent kae = new KeepAliveEvent(finalPlayer, id);
		    	Bukkit.getServer().getPluginManager().callEvent(kae);
		    	if (kae.isCancelled()) { finalEv.setCancelled(true); }
        	});
        } else if (p == PacketType.Play.Client.HELD_ITEM_CHANGE) {
        	WrapperPlayClientHeldItemChange hi = new WrapperPlayClientHeldItemChange(ev);
        	final int slot = hi.getSlot();
        	boolean cancel = false;
        	if (badPacketsD != null) cancel |= badPacketsD.onPacket(e, slot);
        	if (badPacketsE != null) cancel |= badPacketsE.onPacket(e, slot);
        	if (cancel) { ev.setCancelled(true); return; }
        	final Player finalPlayer = e;
        	final PacketReceiveEvent finalEv = ev;
        	if (!hasListeners(HeldItemEvent.getHandlerList())) return;
        	runSync(e, ev, () -> {
		    	HeldItemEvent hie = new HeldItemEvent(finalPlayer, slot);
		    	Bukkit.getServer().getPluginManager().callEvent(hie);
		    	if (hie.isCancelled()) { finalEv.setCancelled(true); }
        	});
        } else if (p == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
        	WrapperPlayClientPlayerPositionAndRotation po = new WrapperPlayClientPlayerPositionAndRotation(ev);
        	final boolean onGround = po.isOnGround();
        	Vector3d X = po.getPosition();
        	final double xP = X.getX();
        	final double yP = X.getY();
        	final double zP = X.getZ();
        	final float wP = po.getYaw();
        	final float pP = po.getPitch();
        	if (position != null && position.onPacket(e, true, xP, yP, zP, true, wP, pP)) { ev.setCancelled(true); return; }
        	final PositionType ty = PositionType.POSITION_ROTATION;
        	// The event runs a tick later, after the server applied the move; keep the client's previous position and the velocity.
        	final boolean afterTeleport = position != null && position.isTeleportJump(e);
        	final Location before = position != null ? position.previousPacketPosition(e, xP, yP, zP) : null;
        	final Vector velocityBefore = e.getVelocity();
        	final long receivedAt = System.currentTimeMillis();
        	final boolean iF = WrapperPlayClientPlayerFlying.isFlying(ev.getPacketType());
        	final Player finalPlayer = e;
        	final PacketReceiveEvent finalEv = ev;
        	if (!hasListeners(PositionEvent.getHandlerList()) && !hasListeners(LookEvent.getHandlerList())) return;
        	runSync(e, ev, () -> {
		    	PositionEvent pos = new PositionEvent(finalPlayer, xP, yP, zP, wP, pP, onGround, iF, finalEv, ty, before, velocityBefore, afterTeleport, receivedAt);
		    	Bukkit.getServer().getPluginManager().callEvent(pos);
		    	if (pos.isCancelled()) { finalEv.setCancelled(true); }
	        	LookEvent loe = new LookEvent(finalPlayer, wP, pP);
	           	Bukkit.getServer().getPluginManager().callEvent(loe);
	        	if (loe.isCancelled()) { finalEv.setCancelled(true); }
        	});
        } else if (p == PacketType.Play.Client.PLAYER_POSITION) {
        	WrapperPlayClientPlayerPosition po = new WrapperPlayClientPlayerPosition(ev);
        	final boolean onGround = po.isOnGround();
        	Vector3d X = po.getPosition();
        	final double xP = X.getX();
        	final double yP = X.getY();
        	final double zP = X.getZ();
        	final float wP = e.getLocation().getYaw();
        	final float pP = e.getLocation().getPitch();
        	if (position != null && position.onPacket(e, true, xP, yP, zP, false, 0, 0)) { ev.setCancelled(true); return; }
        	final PositionType ty = PositionType.POSITION;
        	// The event runs a tick later, after the server applied the move; keep the client's previous position and the velocity.
        	final boolean afterTeleport = position != null && position.isTeleportJump(e);
        	final Location before = position != null ? position.previousPacketPosition(e, xP, yP, zP) : null;
        	final Vector velocityBefore = e.getVelocity();
        	final long receivedAt = System.currentTimeMillis();
        	final boolean iF = WrapperPlayClientPlayerFlying.isFlying(ev.getPacketType());
        	final Player finalPlayer = e;
        	final PacketReceiveEvent finalEv = ev;
        	if (!hasListeners(PositionEvent.getHandlerList())) return;
        	runSync(e, ev, () -> {
		    	PositionEvent pos = new PositionEvent(finalPlayer, xP, yP, zP, wP, pP, onGround, iF, finalEv, ty, before, velocityBefore, afterTeleport, receivedAt);
		    	Bukkit.getServer().getPluginManager().callEvent(pos);
		    	if (pos.isCancelled()) { finalEv.setCancelled(true); }
        	});
        } else if (p == PacketType.Play.Client.INTERACT_ENTITY) {
        	WrapperPlayClientInteractEntity ie = new WrapperPlayClientInteractEntity(ev);
        	final int id = ie.getEntityId();
        	final InteractAction ac = ie.getAction();
        	final InteractionHand ha = ie.getHand();
        	final Optional<Vector3f> ta = ie.getTarget();
        	final Optional<Boolean> sn = ie.isSneaking();
        	boolean cancel = false;
        	if (badPacketsB != null) cancel |= badPacketsB.onPacket(e, id);
        	if (badPacketsH != null) cancel |= badPacketsH.onInteract(e, ac);
        	if (cancel) { ev.setCancelled(true); return; }
        	final Player finalPlayer = e;
        	final PacketReceiveEvent finalEv = ev;
        	if (!hasListeners(InteractEntityEvent.getHandlerList())) return;
        	runSync(e, ev, () -> {
	        	InteractEntityEvent iee = new InteractEntityEvent(finalPlayer, id, ac, ha, ta, sn);
		    	Bukkit.getServer().getPluginManager().callEvent(iee);
		    	if (iee.isCancelled()) { finalEv.setCancelled(true); }
        	});
        } else if (ATTACK != null && p == ATTACK) {
        	final int id = new WrapperPlayClientAttack(ev).getEntityId();
        	boolean cancel = false;
        	if (badPacketsB != null) cancel |= badPacketsB.onPacket(e, id);
        	if (badPacketsH != null) cancel |= badPacketsH.onInteract(e, InteractAction.ATTACK);
        	if (cancel) { ev.setCancelled(true); return; }
        	final Player finalPlayer = e;
        	final PacketReceiveEvent finalEv = ev;
        	if (!hasListeners(InteractEntityEvent.getHandlerList())) return;
        	runSync(e, ev, () -> {
	        	InteractEntityEvent iee = new InteractEntityEvent(finalPlayer, id, InteractAction.ATTACK, InteractionHand.MAIN_HAND, Optional.empty(), Optional.empty());
		    	Bukkit.getServer().getPluginManager().callEvent(iee);
		    	if (iee.isCancelled()) { finalEv.setCancelled(true); }
        	});
        } else if(p == PacketType.Play.Client.STEER_VEHICLE) {
        	WrapperPlayClientSteerVehicle sv = new WrapperPlayClientSteerVehicle(ev);
        	final float fo = sv.getForward();
        	final float si = sv.getSideways();
        	final byte fl = sv.getFlags();
        	if (badPacketsC != null && badPacketsC.onPacket(e, fo, si)) { ev.setCancelled(true); return; }
        	final Player finalPlayer = e;
        	final PacketReceiveEvent finalEv = ev;
        	if (!hasListeners(SteerVehicleEvent.getHandlerList())) return;
        	runSync(e, ev, () -> {
	        	SteerVehicleEvent sve = new SteerVehicleEvent(finalPlayer, fo, si, fl);
		    	Bukkit.getServer().getPluginManager().callEvent(sve);
		    	if (sve.isCancelled()) { finalEv.setCancelled(true); }
        	});
        } else if(p == PacketType.Play.Client.ENTITY_ACTION) {
        	WrapperPlayClientEntityAction ea = new WrapperPlayClientEntityAction(ev);
        	final int eI = ea.getEntityId();
        	final Action ac = ea.getAction();
        	final int jB = ea.getJumpBoost();
        	if (badPacketsF != null && badPacketsF.onPacket(e, eI, ac, jB)) { ev.setCancelled(true); return; }
        	final Player finalPlayer = e;
        	final PacketReceiveEvent finalEv = ev;
        	if (!hasListeners(EntityActionEvent.getHandlerList())) return;
        	runSync(e, ev, () -> {
	        	EntityActionEvent eae = new EntityActionEvent(finalPlayer, eI, ac, jB);
		    	Bukkit.getServer().getPluginManager().callEvent(eae);
		    	if (eae.isCancelled()) { finalEv.setCancelled(true); }
        	});
        } else if(p == PacketType.Play.Client.ANIMATION) {
        	WrapperPlayClientAnimation an = new WrapperPlayClientAnimation(ev);
        	final InteractionHand ih = an.getHand();
        	if (badPacketsH != null) badPacketsH.onAnimation(e);
        	final Player finalPlayer = e;
        	final PacketReceiveEvent finalEv = ev;
        	if (!hasListeners(AnimationEvent.getHandlerList())) return;
        	runSync(e, ev, () -> {
	        	AnimationEvent eae = new AnimationEvent(finalPlayer, ih);
	        	Bukkit.getServer().getPluginManager().callEvent(eae);
	        	if (eae.isCancelled()) { finalEv.setCancelled(true); }
        	});
        } else if (p == PacketType.Play.Client.PLAYER_ROTATION) {
        	WrapperPlayClientPlayerRotation pr = new WrapperPlayClientPlayerRotation(ev);
        	final double pitch = pr.getPitch();
        	final double yaw = pr.getYaw();
        	if (position != null && position.onPacket(e, false, 0, 0, 0, true, (float) yaw, (float) pitch)) { ev.setCancelled(true); return; }
        	final Player finalPlayer = e;
        	final PacketReceiveEvent finalEv = ev;
        	if (!hasListeners(LookEvent.getHandlerList())) return;
        	runSync(e, ev, () -> {
	        	LookEvent loe = new LookEvent(finalPlayer, (float) yaw, (float) pitch);
	           	Bukkit.getServer().getPluginManager().callEvent(loe);
	        	if (loe.isCancelled()) { finalEv.setCancelled(true); }
        	});
    	}
        else return;
    }
}
