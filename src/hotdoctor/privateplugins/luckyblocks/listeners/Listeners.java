package hotdoctor.privateplugins.luckyblocks.listeners;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.boydti.fawe.FaweAPI;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.extent.clipboard.ClipboardFormats;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.world.World;

import hotdoctor.privateplugins.luckyblocks.Main;
import hotdoctor.privateplugins.luckyblocks.events.LuckyblockBreakEvent;
import hotdoctor.privateplugins.luckyblocks.utils.MagicWand;

public class Listeners implements Listener {

	public Listeners(Main plugin) {
		this.plugin = plugin;
	}

	private Main plugin;

	// event that is called when a player breaks a block

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST)
	public void beadk(BlockBreakEvent event) {
		Block block = event.getBlock();
		if (block.getType().equals(Material.SPONGE) && !event.isCancelled()) {
			event.getBlock().getDrops().clear();
			if (block.getData() == (byte) 1) { // if sponge is wet.
				// fake explosion
				Location mid = block.getLocation().clone().add(0.5, 0, 0.5);
				mid.getWorld().spigot().playEffect(mid, Effect.EXPLOSION_LARGE);
				mid.getWorld().playSound(mid, Sound.EXPLODE, 10, 10);
				Stream<Entity> players = mid.getWorld().getNearbyEntities(mid, 3, 3, 3).stream() // collects all players
																									// that are in a
																									// radius of 3
																									// blocks.
						.filter(ent -> ent instanceof Player);
				players.forEach(ent -> { // sets damage depending the double in config.
					if (ent instanceof Player) {
						Player player = (Player) ent;
						player.damage(plugin.getConfig().getDouble("luckyblocks.fake-luckyblock-damage"));
					}
				});
				event.setCancelled(true); // cancels the action of block break event
				block.setData((byte) 0); // sets the sponge from wet to their normal version.
			} else { // if sponge is not wet
				LuckyblockBreakEvent e = new LuckyblockBreakEvent(event);
				Bukkit.getPluginManager().callEvent(e);
				if (!e.isCancelled()) {
					Random random = new Random();
					int drops = 1;
					if (plugin.getConfig().contains("luckyblocks.max-amount-things-todrop")) {
						drops = plugin.getConfig().getInt("luckyblocks.max-amount-things-todrop");
					}
					if (drops == 1) {
						dropLuckyBlock(event.getPlayer(), drops,
								e.getBrokenLuckyBlock().getLocation().add(0.5, 0, 0.5)); // drops the result of a lucky
																							// blocks when this one is
																							// perfectly destroyed.
					} else {
						dropLuckyBlock(event.getPlayer(), 1 + random.nextInt(drops - 1),
								e.getBrokenLuckyBlock().getLocation().add(0.5, 0, 0.5)); // drops the result of a lucky
																							// blocks when this one is
																							// perfectly destroyed.
					}

				}
			}
		}
	}

	@EventHandler
	public void skillUsage(PlayerInteractEvent e) {
		Action action = e.getAction();
		ItemStack item = e.getItem();
		if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
			if (item != null) {
				if (!item.getType().equals(Material.AIR)) {
					for (MagicWand wands : this.plugin.getWandManger().getRegisteredMagicWands()) {
						if (wands.isEqualsWith(item)) {
							wands.useSkill(e.getPlayer());
						}
					}
				}
			}
		}
	}

	private void dropLuckyBlock(Player player, int amount, Location luckyBlockLocation) {
		boolean schematic = false;
		Random random = new Random();
		for (int i = 0; i < amount; i++) {
			String drop;
			Set<String> section = plugin.getConfig().getConfigurationSection("luckyblocks.drops").getKeys(false);
			List<String> list = section.stream().collect(Collectors.toList());
			String beforeDrop = list.get(random.nextInt(list.size()));
			if (beforeDrop.contains(":")) {
				drop = beforeDrop.split(":")[0];
			} else {
				drop = beforeDrop;
			}
			if (Material.matchMaterial(drop) != null) { // drops a normal item
				int dropAmount = 1;
				if (plugin.getConfig().contains("luckyblocks.drops." + beforeDrop + ".amount")) {
					dropAmount = plugin.getConfig().getInt("luckyblocks.drops." + beforeDrop + ".amount");
				}
				ItemStack item;
				if (beforeDrop.contains(":")) {
					String[] split = beforeDrop.split(":");
					item = new ItemStack(Material.valueOf(drop), (byte) Byte.valueOf(split[1]));
				} else {
					item = new ItemStack(Material.valueOf(drop));
				}
				Location loc = luckyBlockLocation.clone();
				for (int dropp = 0; dropp < dropAmount; dropp++) {
					loc.getWorld().dropItemNaturally(loc, item);
				}
				continue;
			} else if (drop.equalsIgnoreCase("SCHEMATIC")) { // drops and paste a schematic
				if (schematic) {
					i = i - 1;
				} else {
					schematic = true;
					Location parse = player.getLocation().getBlock().getLocation().clone().add(0.5, 0, 0.5);
					parse.setYaw(player.getLocation().getYaw());
					parse.setPitch(player.getLocation().getPitch());
					player.teleport(parse);
					pasteSchematic(plugin.getConfig().getString("luckyblocks.drops." + beforeDrop + ".schematic_name"),
							parse, false);
				}
				continue;
			} else if (drop.equalsIgnoreCase("MAGIC_WAND")) { // drops a magic wandesp
				String id = plugin.getConfig().getString("luckyblocks.drops." + beforeDrop + ".type");
				MagicWand wand = plugin.getWandManger().getMagicWandByID(id);
				int dropAmount = 1;
				if (plugin.getConfig().contains("luckyblocks.drops." + beforeDrop + ".amount")) {
					dropAmount = plugin.getConfig().getInt("luckyblocks.drops." + beforeDrop + ".amount");
				}
				Location loc = luckyBlockLocation.clone();
				ItemStack item = wand.getItemStack();
				for (int dropp = 0; dropp < dropAmount; dropp++) {
					loc.getWorld().dropItemNaturally(loc, item);
				}
			}

		}
	}

	private boolean pasteSchematic(String schematicName, Location loc, boolean noAir) {
		World weWorld = FaweAPI.getWorld(loc.getWorld().getName());

		File file = new File(
				plugin.getDataFolder() + File.separator + "schematics" + File.separator + schematicName + ".schematic");
		if (file.exists() == false) {
			return false;
		}
		boolean allowUndo = false;
		Vector vec = new Vector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		try {
			EditSession editSession = ClipboardFormats.findByFile(file).load(file).paste(weWorld, vec, allowUndo,
					!noAir, (Transform) null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
