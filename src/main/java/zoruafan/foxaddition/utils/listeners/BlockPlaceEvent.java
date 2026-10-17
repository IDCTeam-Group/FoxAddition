package zoruafan.foxaddition.utils.listeners;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.util.Vector3i;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class BlockPlaceEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	Player player;
	Vector3i blockPosition;
	BlockFace face;
	Block block;
	long sequence;
	InteractionHand hand;
	Vector3f cursor;
	private boolean cancelled;
	  
	public BlockPlaceEvent(Player player, Vector3i blockPosition, BlockFace face, long sequence, InteractionHand hand, Vector3f cursor) {
		// Always fired from the player's tick thread (see FoxPacketListener#runSync); an async flag makes Paper reject the event.
		super(false);
		
		this.player = player;
		this.blockPosition = blockPosition;
        this.face = face;
        this.sequence = sequence;
        this.hand = hand;
        this.cursor = cursor;
	}
	
	public Vector3f getCursorPosition() {
		return this.cursor;
	}

	public Player getPlayer() {
		return this.player;
	}
	
    public Vector3i getBlockPosition() {
        return blockPosition;
    }
    
    public BlockFace getFace() {
        return face;
    }
    
    public Block getBlock() {
        return block = player.getWorld().getBlockAt(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
    }
    
    public long getSequence() {
        return sequence;
    }

    public InteractionHand hand() {
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