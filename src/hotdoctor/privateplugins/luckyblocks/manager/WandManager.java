package hotdoctor.privateplugins.luckyblocks.manager;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import hotdoctor.privateplugins.luckyblocks.Main;
import hotdoctor.privateplugins.luckyblocks.utils.MagicWand;

public class WandManager {
	
	private HashMap<String, MagicWand> wands = new HashMap<>();
	
	public WandManager(Main plugin) {
		this.plugin = plugin;
	}
	private Main plugin;
	
	public MagicWand registerMagicWand(String id) {
		MagicWand wand = new MagicWand(plugin, id);
		if(wand.build() && !wands.containsKey(id)) {
			wands.put(id, wand);
		}
		return wand;
	}
	
	public MagicWand getMagicWandByID(String id) {
		return wands.get(id);
	}
	
	public List<MagicWand> getRegisteredMagicWands(){
		return wands.values().stream().collect(Collectors.toList());
	}

}
