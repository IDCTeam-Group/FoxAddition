package zoruafan.foxaddition.utils.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction.Action;

public class EntityActionEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	Player player;
    private int entityID;
    private Action action;
    private int jumpBoost;
	private boolean cancelled;
	  
	public EntityActionEvent(Player player, int entityID, Action action, int jumpBoost) {
		// Always fired from the player's tick thread (see FoxPacketListener#runSync); an async flag makes Paper reject the event.
		super(false);
		
		this.player = player;
        this.entityID = entityID;
        this.action = action;
        this.jumpBoost = jumpBoost;
	}

	public Player getPlayer() {
		return this.player;
	}
	
    public int getEntityId() {
        return this.entityID;
    }
    
    public Action getAction() {
        return this.action;
    }
    
    public int getJumpBoost() {
        return this.jumpBoost;
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