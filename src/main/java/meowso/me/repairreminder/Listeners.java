package meowso.me.repairreminder;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;

public class Listeners implements Listener {
    private RepairReminder repairReminder;
    private HashMap<String, Long> messageCooldowns;
    private static final int cooldownTime = 1000; // Milliseconds

    public Listeners(RepairReminder repairReminder) {
        this.repairReminder = repairReminder;
        messageCooldowns = new HashMap<>();
    }

    @EventHandler
    public void onPlayerItemBreak(PlayerItemBreakEvent event) {
        Player player = event.getPlayer();

        // Cancel if player doesn't have permission
        if (!player.hasPermission("repairreminder.remind") || player.getGameMode() != GameMode.SURVIVAL) return;

        // Get item damage info
        ItemStack item = player.getInventory().getItemInMainHand();
        Damageable itemDamageable = (Damageable) item.getItemMeta();

        // Set item back to maximum damage possible
        int maxDamage = item.getType().getMaxDurability();
        itemDamageable.setDamage(maxDamage);

        // Give item back to player after it's already been broken
        player.getInventory().setItemInMainHand(item);
        player.updateInventory();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        // Cancel if player doesn't have permission
        if (!player.hasPermission("repairreminder.remind") || player.getGameMode() != GameMode.SURVIVAL) return;

        // Get item damage info
        ItemStack item = player.getInventory().getItemInMainHand();
        Damageable itemDamageable = (Damageable) item.getItemMeta();

        if (itemDamageable == null) return;

        int itemDamage = itemDamageable.getDamage(); // Get item current damage
        int maxDamage = item.getType().getMaxDurability(); // Get item max damage
        String itemName = item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().toString().replace("_", " ").toLowerCase(); // Get item display name or name
        int levelCost = getRepairCost(player); // Get how much it would cost to repair item

        // If the item is breaking, cancel event and send message
        if (itemDamage == maxDamage && maxDamage > 0) {
            event.setCancelled(true);

            // Only send message every 1 second to prevent spam
            if (isInCooldown(player)) return;
            else {
                addCooldown(player);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', repairReminder.getConfig().getString("reminder-message")).replace("$0", itemName).replace("$1", String.valueOf(levelCost)));
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        removeCooldown(player);
    }

    // Nogard custom repair command costs in levels
    private int getRepairCost(Player player) {
        if (player.hasPermission("essentials.repair.all")) return 30;
        if (player.hasPermission("essentials.repair")) return 35;
        else return 40;
    }

    private void addCooldown(Player player) {
        messageCooldowns.put(player.getName(), System.currentTimeMillis() + cooldownTime);
    }

    private boolean isInCooldown(Player player) {
        if (messageCooldowns.containsKey(player.getName())) {
            if (System.currentTimeMillis() < messageCooldowns.get(player.getName())) {
                return true;
            } else {
                messageCooldowns.remove(player.getName());
                return false;
            }
        } else {
            return false;
        }
    }

    private void removeCooldown(Player player) {
        messageCooldowns.remove(player.getName());
    }
}
