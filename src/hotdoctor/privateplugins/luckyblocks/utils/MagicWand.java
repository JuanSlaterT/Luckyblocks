package hotdoctor.privateplugins.luckyblocks.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import hotdoctor.privateplugins.luckyblocks.Main;

public class MagicWand {

	private String configPath;
	private Main plugin;

	public MagicWand(Main plugin, String configPath) {
		this.configPath = configPath;
		this.plugin = plugin;
	}

	private ItemStack wand;

	public ItemStack getItemStack() {
		return wand;
	}

	private HashMap<Player, Integer> skillCountdown = new HashMap<>();

	public String getID() {
		return configPath;
	}

	// item construction
	public boolean build() {
		try {
			ItemStack item = new ItemStack(
					Material.valueOf(plugin.getConfig().getString("wands." + configPath + ".material")));
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(plugin.color(plugin.getConfig().getString("wands." + configPath + ".name")));
			List<String> list = new ArrayList<>();
			for (String text : plugin.getConfig().getStringList("wands." + configPath + ".lore")) {
				list.add(plugin.color(text));
			}
			meta.setLore(list);
			item.setItemMeta(meta);
			wand = item;
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean isEqualsWith(ItemStack item) {
		if (item.getItemMeta().getDisplayName() != null) {
			if (item.getItemMeta().getLore() != null) {
				if (item.getType().equals(this.getItemStack().getType())
						&& item.getItemMeta().getDisplayName()
								.equals(this.getItemStack().getItemMeta().getDisplayName())
						&& item.getItemMeta().getLore().equals(this.getItemStack().getItemMeta().getLore())) {
					return true;

				}
			}
		}
		return false;
	}

	// skill invocation
	public boolean useSkill(Player owner) {
		if (skillCountdown.containsKey(owner)) {
			owner.sendMessage(plugin.color(plugin.getConfig().getString("wands." + configPath + ".cooldown-message"))
					.replace("<seconds>", skillCountdown.get(owner) + ""));
			return false;
		} else {
			skillCountdown.put(owner, plugin.getConfig().getInt("wands." + configPath + ".cooldown"));
			countdown(owner);
			String skill = plugin.getConfig().getString("wands." + configPath + ".skill");
			String[] ability = skill.split(":");
			Location location = owner.getLocation().clone();
			owner.playSound(location, Sound.NOTE_PLING, 2.0f, 2.0f);
			if (ability[0].equalsIgnoreCase("POTION_EFFECT")) {
				try {

					PotionEffectType type = PotionEffectType.getByName(ability[1]);
					int level = Integer.valueOf(ability[2]);
					int time = Integer.valueOf(ability[3]);
					if (type.equals(PotionEffectType.REGENERATION)) {
						int[] list = { 45, 90, 135, 180, 225, 280, 325, 360 };
						double y = owner.getLocation().getY() + 2;
						// summoning effects of "hearts" around of player using the degrees in the list
						for (int degree : list) {
							double radians = Math.toRadians(degree);
							double x = Math.cos(radians);
							double z = Math.sin(radians);
							location.add(x, y, z);
							location.getWorld().playEffect(location, Effect.HEART, 1);
							location.subtract(x, 0, z);
							y -= 0.5;
						}

					}
					owner.addPotionEffect(new PotionEffect(type, time * 20, level));
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else if (ability[0].equalsIgnoreCase("FAKE_LUCKY")) {
				Vector direction = getDirection(owner.getLocation().clone().add(0, 1, 0));
				owner.getLocation().getWorld().spawnFallingBlock(owner.getLocation(), Material.SPONGE, (byte) 1)
						.setVelocity(direction.multiply(3));
			} else if (ability[0].equalsIgnoreCase("BRIDGE")) {
				Bukkit.broadcastMessage("Construyendo puente...");
				Location a = owner.getLocation();
				Location b = owner.getLocation().clone().add(owner.getLocation().getDirection().multiply(11));
				int timer = 1;
				for(Location loc : getLineBetweenPoints(a,b)) {
					setBlocks(loc.getX(), loc.getY(), loc.getZ(), timer, owner);
					timer += 2;
				}
			}
			return true;
		}

	}

	public static HashSet<Location> getLineBetweenPoints(Location l1, Location l2) {
		HashSet<Location> line = new HashSet<>();
		double xSlope = (l1.getBlockX() - l2.getBlockX());
		double ySlope = (l1.getBlockY() - l2.getBlockY()) / xSlope;
		double zSlope = (l1.getBlockZ() - l2.getBlockZ()) / xSlope;
		double y = l1.getBlockY();
		double z = l1.getBlockZ();
		double interval = 1 / (Math.abs(ySlope) > Math.abs(zSlope) ? ySlope : zSlope);
		for (double x = l1.getBlockX(); x - l1.getBlockX() < Math
				.abs(xSlope); x += interval, y += ySlope, z += zSlope) {
			Location loc = new Location(l1.getWorld(), x, y, z);
			line.add(loc);
		}
		return line;
	}

	private void setBlocks(double x, double y, double z, long timer, Player owner) {
		Location original = new Location(owner.getWorld(), x, y, z);
		Bukkit.getScheduler().runTaskLater(plugin, () -> {
			Bukkit.broadcastMessage("Construyendo puente... " + x + " " + y + " " + z);
			FallingBlock block = owner.getLocation().getWorld().spawnFallingBlock(original, Material.PACKED_ICE,
					(byte) 1);
			block.setVelocity(new Vector(0, 1, 0).multiply(0.5));
			dead(block, original);
		}, timer);
	}

	private void dead(FallingBlock block, Location original) {
		Bukkit.getScheduler().runTaskLater(plugin, () -> {
			if (block.isValid()) {
				block.remove();
				Material type = original.getBlock().getType();
				if (type == null || type.equals(Material.AIR)) {
					original.getBlock().setType(Material.PACKED_ICE);
				}

			}
		}, 6);
	}

	private Vector getDirection(Location loc) {
		Vector vector = new Vector();
		double rotX = (double) loc.getYaw();
		double rotY = (double) loc.getPitch();
		vector.setY(-Math.sin(Math.toRadians(rotY)));
		double xz = Math.cos(Math.toRadians(rotY));
		vector.setX(-xz * Math.sin(Math.toRadians(rotX)));
		vector.setZ(xz * Math.cos(Math.toRadians(rotX)));
		return vector;
	}

	// skill countdown usage
	private void countdown(Player player) {
		new BukkitRunnable() {

			@Override
			public void run() {
				if (skillCountdown.containsKey(player)) {
					int time = skillCountdown.get(player);
					time = time - 1;
					if (time == 0) {
						skillCountdown.remove(player);
						this.cancel();
					} else {
						skillCountdown.remove(player);
						skillCountdown.put(player, time);
					}
				} else {
					this.cancel();
				}

			}

		}.runTaskTimer(plugin, 20, 20);

	}

}
