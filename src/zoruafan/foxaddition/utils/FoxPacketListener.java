package zoruafan.foxaddition.utils;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
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
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import zoruafan.foxaddition.FoxAdditionAPI;
import zoruafan.foxaddition.utils.listeners.*;
import java.util.Optional;

public class FoxPacketListener extends FoliaScheduler implements PacketListener {
    FoxAdditionAPI api = FoxAdditionAPI.INSTANCE;
    JavaPlugin pl = api.getPlugin();
    
    public void onPacketReceive(PacketReceiveEvent ev) {
    	User user = ev.getUser();
    	if (user.getUUID() == null) { return; }
        Player e = Bukkit.getPlayer(user.getUUID());
        if (e == null) { return; }
        PacketTypeCommon p = ev.getPacketType();
        
        if (p == PacketType.Play.Client.PLAYER_ABILITIES) {
        	WrapperPlayClientPlayerAbilities a = new WrapperPlayClientPlayerAbilities(ev);
    	    final Optional<Boolean> flying = Optional.of(a.isFlying());
    	    final Optional<Boolean> godMode = a.isInGodMode();
    	    final Optional<Boolean> flightAllowed = a.isFlightAllowed();
    	    final Optional<Boolean> creativeMode = a.isInCreativeMode();
    	    final Optional<Float> flySpeed = a.getFlySpeed();
    	    final Optional<Float> walkSpeed = a.getWalkSpeed();
		    final Player finalPlayer = e;
		    final PacketReceiveEvent finalEv = ev;
		    e.getScheduler().run(pl, task -> {
			    AbilitiesEvent abi = new AbilitiesEvent(finalPlayer, flying, godMode, flightAllowed, creativeMode, flySpeed, walkSpeed);
			    Bukkit.getServer().getPluginManager().callEvent(abi);
			    if (abi.isCancelled()) { finalEv.setCancelled(true); }
		    }, null);
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
        	e.getScheduler().run(pl, task -> {
	        	BlockDigEvent bde = new BlockDigEvent(finalPlayer, a, bP, f);
		        Bukkit.getServer().getPluginManager().callEvent(bde);
		        if (bde.isCancelled()) { finalEv.setCancelled(true); }
        	}, null);
        } else if (p == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
        	WrapperPlayClientPlayerBlockPlacement bp = new WrapperPlayClientPlayerBlockPlacement(ev);
        	Vector3i b = bp.getBlockPosition();
    		int x = b.getX();
    		int y = b.getY();
    		int z = b.getZ();
        	final Vector3i bP = new Vector3i(x, y, z);
        	final BlockFace f = bp.getFace();
        	final long tm = bp.getSequence();
        	final InteractionHand hand = bp.getHand();
        	final Player finalPlayer = e;
        	final PacketReceiveEvent finalEv = ev;
        	e.getScheduler().run(pl, task -> {
	        	BlockPlaceEvent bpe = new BlockPlaceEvent(finalPlayer, bP, f, tm, hand);
	        	Bukkit.getServer().getPluginManager().callEvent(bpe);
	        	if (bpe.isCancelled()) { finalEv.setCancelled(true); }
        	}, null);
        } else if (p == PacketType.Play.Client.KEEP_ALIVE) {
        	WrapperPlayClientKeepAlive k = new WrapperPlayClientKeepAlive(ev);
        	final long id = k.getId();
        	final Player finalPlayer = e;
        	final PacketReceiveEvent finalEv = ev;
        	e.getScheduler().run(pl, task -> {
	        	KeepAliveEvent kae = new KeepAliveEvent(finalPlayer, id);
		    	Bukkit.getServer().getPluginManager().callEvent(kae);
		    	if (kae.isCancelled()) { finalEv.setCancelled(true); }
        	}, null);
        } else if (p == PacketType.Play.Client.HELD_ITEM_CHANGE) {
        	WrapperPlayClientHeldItemChange hi = new WrapperPlayClientHeldItemChange(ev);
        	final int slot = hi.getSlot();
        	final Player finalPlayer = e;
        	final PacketReceiveEvent finalEv = ev;
        	e.getScheduler().run(pl, task -> {
		    	HeldItemEvent hie = new HeldItemEvent(finalPlayer, slot);
		    	Bukkit.getServer().getPluginManager().callEvent(hie);
		    	if (hie.isCancelled()) { finalEv.setCancelled(true); }
        	}, null);
        } else if (p == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
        	WrapperPlayClientPlayerPositionAndRotation po = new WrapperPlayClientPlayerPositionAndRotation(ev);
        	final boolean onGround = po.isOnGround();
        	Vector3d X = po.getPosition();
        	final double xP = X.getX();
        	final double yP = X.getY();
        	final double zP = X.getZ();
        	final float wP = po.getYaw();
        	final float pP = po.getPitch();
        	final PositionType ty = PositionType.POSITION_ROTATION;
        	final boolean iF = WrapperPlayClientPlayerFlying.isFlying(ev.getPacketType());
        	final Player finalPlayer = e;
        	final PacketReceiveEvent finalEv = ev;
        	e.getScheduler().run(pl, task -> {
		    	PositionEvent pos = new PositionEvent(finalPlayer, xP, yP, zP, wP, pP, onGround, iF, finalEv, ty);
		    	Bukkit.getServer().getPluginManager().callEvent(pos);
		    	if (pos.isCancelled()) { finalEv.setCancelled(true); }
	        	LookEvent loe = new LookEvent(finalPlayer, wP, pP);
	           	Bukkit.getServer().getPluginManager().callEvent(loe);
	        	if (loe.isCancelled()) { finalEv.setCancelled(true); }
        	}, null);
        } else if (p == PacketType.Play.Client.PLAYER_POSITION) {
        	WrapperPlayClientPlayerPosition po = new WrapperPlayClientPlayerPosition(ev);
        	final boolean onGround = po.isOnGround();
        	Vector3d X = po.getPosition();
        	final double xP = X.getX();
        	final double yP = X.getY();
        	final double zP = X.getZ();
        	final float wP = e.getLocation().getYaw();
        	final float pP = e.getLocation().getPitch();
        	final PositionType ty = PositionType.POSITION;
        	final boolean iF = WrapperPlayClientPlayerFlying.isFlying(ev.getPacketType());
        	final Player finalPlayer = e;
        	final PacketReceiveEvent finalEv = ev;
        	e.getScheduler().run(pl, task -> {
		    	PositionEvent pos = new PositionEvent(finalPlayer, xP, yP, zP, wP, pP, onGround, iF, finalEv, ty);
		    	Bukkit.getServer().getPluginManager().callEvent(pos);
		    	if (pos.isCancelled()) { finalEv.setCancelled(true); }
        	}, null);
        } else if (p == PacketType.Play.Client.INTERACT_ENTITY) {
        	WrapperPlayClientInteractEntity ie = new WrapperPlayClientInteractEntity(ev);
        	final int id = ie.getEntityId();
        	final InteractAction ac = ie.getAction();
        	final InteractionHand ha = ie.getHand();
        	final Optional<Vector3f> ta = ie.getTarget();
        	final Optional<Boolean> sn = ie.isSneaking();
        	final Player finalPlayer = e;
        	final PacketReceiveEvent finalEv = ev;
        	e.getScheduler().run(pl, task -> {
	        	InteractEntityEvent iee = new InteractEntityEvent(finalPlayer, id, ac, ha, ta, sn);
		    	Bukkit.getServer().getPluginManager().callEvent(iee);
		    	if (iee.isCancelled()) { finalEv.setCancelled(true); }
        	}, null);
        } else if(p == PacketType.Play.Client.STEER_VEHICLE) {
        	WrapperPlayClientSteerVehicle sv = new WrapperPlayClientSteerVehicle(ev);
        	final float fo = sv.getForward();
        	final float si = sv.getSideways();
        	final byte fl = sv.getFlags();
        	final Player finalPlayer = e;
        	final PacketReceiveEvent finalEv = ev;
        	e.getScheduler().run(pl, task -> {
	        	SteerVehicleEvent sve = new SteerVehicleEvent(finalPlayer, fo, si, fl);
		    	Bukkit.getServer().getPluginManager().callEvent(sve);
		    	if (sve.isCancelled()) { finalEv.setCancelled(true); }
        	}, null);
        } else if(p == PacketType.Play.Client.ENTITY_ACTION) {
        	WrapperPlayClientEntityAction ea = new WrapperPlayClientEntityAction(ev);
        	final int eI = ea.getEntityId();
        	final Action ac = ea.getAction();
        	final int jB = ea.getJumpBoost();
        	final Player finalPlayer = e;
        	final PacketReceiveEvent finalEv = ev;
        	e.getScheduler().run(pl, task -> {
	        	EntityActionEvent eae = new EntityActionEvent(finalPlayer, eI, ac, jB);
		    	Bukkit.getServer().getPluginManager().callEvent(eae);
		    	if (eae.isCancelled()) { finalEv.setCancelled(true); }
        	}, null);
        } else if(p == PacketType.Play.Client.ANIMATION) {
        	WrapperPlayClientAnimation an = new WrapperPlayClientAnimation(ev);
        	final InteractionHand ih = an.getHand();
        	final Player finalPlayer = e;
        	final PacketReceiveEvent finalEv = ev;
        	e.getScheduler().run(pl, task -> {
	        	AnimationEvent eae = new AnimationEvent(finalPlayer, ih);
	        	Bukkit.getServer().getPluginManager().callEvent(eae);
	        	if (eae.isCancelled()) { finalEv.setCancelled(true); }
        	}, null);
        } else if (p == PacketType.Play.Client.PLAYER_ROTATION) {
        	WrapperPlayClientPlayerRotation pr = new WrapperPlayClientPlayerRotation(ev);
        	final double pitch = pr.getPitch();
        	final double yaw = pr.getYaw();
        	final Player finalPlayer = e;
        	final PacketReceiveEvent finalEv = ev;
        	e.getScheduler().run(pl, task -> {
	        	LookEvent loe = new LookEvent(finalPlayer, (float) yaw, (float) pitch);
	           	Bukkit.getServer().getPluginManager().callEvent(loe);
	        	if (loe.isCancelled()) { finalEv.setCancelled(true); }
        	}, null);
    	}
        else return;
    }
}
