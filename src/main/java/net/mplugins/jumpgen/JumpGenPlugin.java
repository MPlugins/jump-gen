package net.mplugins.jumpgen;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public final class JumpGenPlugin extends JavaPlugin implements Listener {
    // maximum distance a block can be away
    private static final int MAX_DISTANCE = 4;
    private static final Random random = new Random();
    private static final Map<UUID, Location> playerNextJump = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        final Location from = event.getFrom();
        final Location to = event.getTo();
        final Player player = event.getPlayer();

        // check if moved to different block
        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ())
            return;

        final Block targetBlock = to.getBlock().getRelative(BlockFace.DOWN);

        // if player plays parkour and missed next jump
        if (playerNextJump.containsKey(player.getUniqueId())
                && player.getLocation().getBlockY() < playerNextJump.get(player.getUniqueId()).getBlockY()) {
            // here when player failed parkour
            player.showTitle(
                    Title.title(
                            Component
                                    .text("You failed")
                                    .color(NamedTextColor.RED),
                            Component
                                    .text("Better luck next time!")
                                    .color(NamedTextColor.GRAY),
                            Title.Times.times(
                                    Duration.ofSeconds(0),
                                    Duration.ofSeconds(2),
                                    Duration.ofSeconds(1)
                            )
                    )
            );

            playerNextJump.remove(player.getUniqueId());
            targetBlock.setType(Material.AIR);
            return;
        }

        // start parkour when stepping on diamond block
        if (targetBlock.getType() != Material.DIAMOND_BLOCK)
            return;

        // change block type so diamond block doesn't trigger twice
        targetBlock.setType(Material.EMERALD_BLOCK);
        // play sound to indicate that game started
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        // calculate next jump position
        final Location nextJumpLocation = calculateNextJumpPosition(targetBlock.getLocation());
        nextJumpLocation.getBlock().setType(Material.DIAMOND_BLOCK);
        playerNextJump.put(player.getUniqueId(), nextJumpLocation);
    }

    private static Location calculateNextJumpPosition(Location from) {
        final int deltaX = getRandomDelta();
        final int deltaY = 1; // always make next block one higher
        final int deltaZ = getRandomDelta();

        // prevent next block from spawning on current block
        return deltaX == 0 && deltaZ == 0
                ? from.clone().add(1, deltaY, 0)
                : from.clone().add(deltaX, deltaY, deltaZ);
    }

    private static int getRandomDelta(int bound) {
        // random number from -bound to + bound
        return random.nextInt(bound * 2) - bound;
    }

    private static int getRandomDelta() {
        return getRandomDelta(MAX_DISTANCE);
    }
}
