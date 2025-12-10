package zoruafan.foxaddition.utils.listeners;

import java.util.Optional;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity.InteractAction;

import io.github.retrooper.packetevents.util.folia.FoliaScheduler;

public class InteractEntityEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	Player player;
	int id;
	InteractAction action;
	InteractionHand hand;
	Optional<Vector3f> target;
	Optional<Boolean> sneaking;
	private boolean cancelled;
	
	public InteractEntityEvent(Player player, int id, InteractAction action, InteractionHand hand, Optional<Vector3f> target, Optional<Boolean> sneaking) {
		super(!FoliaScheduler.isFolia());
		
		this.player = player;
		this.id = id;
		this.action = action;
		this.hand = hand;
		this.target = target;
		this.sneaking = sneaking;
	}
	
	public Player getPlayer() {
		return this.player;
	}
	
	public int getId() {
		return this.id;
	}
	
	public InteractAction getAction() {
		return this.action;
	}
	
	public InteractionHand getHand() {
		return this.hand;
	}
	
	public Optional<Vector3f> getTarget() {
		return this.target;
	}
	
	public Optional<Boolean> isSneaking() {
		return this.sneaking;
	}
	
    public void setCancelled(boolean value) {
    	cancelled = value;
    }
    
	public boolean isCancelled() { 
		return cancelled;
	}
	
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
