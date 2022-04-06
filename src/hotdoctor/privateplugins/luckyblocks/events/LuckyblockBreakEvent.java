package hotdoctor.privateplugins.luckyblocks.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;

public class LuckyblockBreakEvent extends Event implements Cancellable{
	private final static HandlerList HANDLERS = new HandlerList();
	BlockBreakEvent e;
	public LuckyblockBreakEvent(BlockBreakEvent e) {
		this.e = e;
		e.setExpToDrop(0);
		e.getBlock().getDrops().clear();
	}
	
	public Block getBrokenLuckyBlock() {
		return e.getBlock();
	}
	
	public Player getPlayer() {
		return e.getPlayer();
	}
	
	public BlockBreakEvent getMainLuckyblockEvent() {
		return e;
	}
	
	@Override
	public boolean isCancelled() {
		// TODO Auto-generated method stub
		return e.isCancelled();
	}

	@Override
	public void setCancelled(boolean cancel) {
		e.setCancelled(cancel);
		
	}

	public static HandlerList getHandlerList() {
	    return HANDLERS;
	}
	@Override
	public HandlerList getHandlers() {
		// TODO Auto-generated method stub
		return HANDLERS;
	}

}
