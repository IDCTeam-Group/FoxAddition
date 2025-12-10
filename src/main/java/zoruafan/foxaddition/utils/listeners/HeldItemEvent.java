package zoruafan.foxaddition.utils.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class HeldItemEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	Player player;
	private int slot;
	private boolean cancelled;
	  
	public HeldItemEvent(Player player, int slot) {
		super(!FoliaScheduler.isFolia());
		
		this.player = player;
        this.slot = slot;
	}

	public Player getPlayer() {
		return this.player;
	}
	
    public int getSlot() {
        return slot;
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