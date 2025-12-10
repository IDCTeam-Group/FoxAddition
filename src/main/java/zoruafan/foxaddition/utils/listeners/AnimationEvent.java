package zoruafan.foxaddition.utils.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.github.retrooper.packetevents.protocol.player.InteractionHand;

import io.github.retrooper.packetevents.util.folia.FoliaScheduler;

public class AnimationEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	Player player;
	private InteractionHand hand;
	private boolean cancelled;
	  
	public AnimationEvent(Player player, InteractionHand hand) {
		super(!FoliaScheduler.isFolia());
		
		this.player = player;
        this.hand = hand;
	}

	public Player getPlayer() {
		return this.player;
	}
	
    public InteractionHand getHand() {
        return hand;
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
