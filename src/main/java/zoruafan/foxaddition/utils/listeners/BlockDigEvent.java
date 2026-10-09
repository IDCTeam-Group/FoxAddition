package zoruafan.foxaddition.utils.listeners;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.util.Vector3i;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class BlockDigEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	DiggingAction action;
	Player player;
	Vector3i blockPosition;
	Block block;
	BlockFace face;
	private boolean cancelled;
	  
	public BlockDigEvent(Player player, DiggingAction action, Vector3i blockPosition, BlockFace face) {
		// Always fired from the player's tick thread (see FoxPacketListener#runSync); an async flag makes Paper reject the event.
		super(false);
		
		this.player = player;
		this.action = action;
		this.blockPosition = blockPosition;
		this.face = face;
	}

	public Player getPlayer() {
		return this.player;
	}
	
	public DiggingAction getAction() {
		return this.action;
	}
	
    public Vector3i getBlockPosition() {
        return this.blockPosition;
    }
    
    public BlockFace getFace() {
        return this.face;
    }
    
    public Block getBlock() {
        return block = player.getWorld().getBlockAt(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
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