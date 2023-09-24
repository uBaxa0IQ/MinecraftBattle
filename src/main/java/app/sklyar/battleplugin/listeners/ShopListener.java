package app.sklyar.battleplugin.listeners;

import app.sklyar.battleplugin.BattlePlugin;
import app.sklyar.battleplugin.Items.ItemManager;
import app.sklyar.battleplugin.classes.Parameters;
import app.sklyar.battleplugin.inventories.ShopInventory;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.Team;

import java.nio.channels.SelectableChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ShopListener implements Listener {

    private final HashMap<ItemStack, Integer> shopItemsLvl2;
    private final HashMap<ItemStack, Integer> shopItemsLvl1;
    public ShopListener(HashMap<ItemStack, Integer> shopItemsLvl1, HashMap<ItemStack, Integer> shopItemsLvl2) {
        this.shopItemsLvl1 = shopItemsLvl1;
        this.shopItemsLvl2 = shopItemsLvl2;
    }
    @EventHandler
    public void ShopListener(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) { return; }
        if (event.getClickedInventory().getHolder() instanceof ShopInventory){
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR){
                ItemStack item = event.getCurrentItem().clone();
                ItemMeta meta = item.getItemMeta();
                List<String> lore = meta.getLore();
                lore.remove(lore.size() - 1);
                meta.setLore(lore);
                item.setItemMeta(meta);
                meta.setLore(lore);
                HashMap<ItemStack, Integer> shopItems;
                if (shopItemsLvl1.containsKey(item)) {
                    shopItems = shopItemsLvl1;
                    Integer itemCost = shopItems.get(item);
                    Integer coinsCount = 0;
                    for(ItemStack itemStack : player.getInventory().getContents()){
                        if (itemStack != null && itemStack.getType() == ItemManager.coinlvl1.getType()){
                            coinsCount += itemStack.getAmount();
                        }
                    }
                    if (coinsCount >= itemCost){
                        if (item.getType() == Material.ENCHANTED_GOLDEN_APPLE){
                            double playerMaxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                            if (playerMaxHealth <= 18) { player.setMaxHealth(playerMaxHealth + 2); }
                        }
                        else if (item.getType() == Material.COMPASS){
                            BukkitScheduler scheduler = Bukkit.getScheduler();
                            scheduler.runTaskTimer(BattlePlugin.getInstance(), CompassUpdater(player), 0, 20);
                            player.getInventory().addItem(item);
                        }
                        else{ player.getInventory().addItem(item); }
                        ItemStack[] inventory = player.getInventory().getContents();
                        for (ItemStack target : inventory) {
                            if (target != null && target.getType().toString().equalsIgnoreCase(ItemManager.coinlvl1.getType().toString())) {
                                if (target.getAmount() >= itemCost) {
                                    target.setAmount(target.getAmount() - itemCost);
                                    break;
                                }
                                else{
                                    itemCost -= target.getAmount();
                                    target.setAmount(target.getAmount() - target.getAmount());
                                }
                            }
                        }
                    }
                }
                else {
                    shopItems = shopItemsLvl2;
                    if (item.getType() == Material.BOW){
                        player.getInventory().addItem(new ItemStack(Material.ARROW, 1));
                    }
                    Integer itemCost = shopItems.get(item);
                    Integer coinsCount = 0;
                    for(ItemStack itemStack : player.getInventory().getContents()){
                        if (itemStack != null && itemStack.getType() == ItemManager.coinlvl2.getType()){
                            coinsCount += itemStack.getAmount();
                        }
                    }
                    if (coinsCount >= itemCost){
                        player.getInventory().addItem(item);
                        ItemStack[] inventory = player.getInventory().getContents();
                        for (ItemStack target : inventory) {
                            if (target != null && target.getType().toString().equalsIgnoreCase(ItemManager.coinlvl2.getType().toString())) {
                                if (target.getAmount() >= itemCost) {
                                    target.setAmount(target.getAmount() - itemCost);
                                    break;
                                }
                                else{
                                    itemCost -= target.getAmount();
                                    target.setAmount(target.getAmount() - target.getAmount());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private Runnable CompassUpdater (Player player) {

        return () -> {
            Player target = findNearestPlayer(player);
            if (target != null) {
                Location targetLocation = target.getLocation();
                player.setCompassTarget(targetLocation);
            }
        };
    }

    @Deprecated
    private Player findNearestPlayer(Player player) {
        Player nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        Location loc1 = player.getLocation();

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getScoreboard().getPlayerTeam(onlinePlayer) == null || player.getScoreboard().getPlayerTeam(onlinePlayer) == null) { break; }
            if (!(player.getScoreboard().getPlayerTeam(player).equals(onlinePlayer.getScoreboard().getPlayerTeam(onlinePlayer)))) {
                Location loc2 = onlinePlayer.getLocation();
                double distance = loc1.distance(loc2);
                if (distance < nearestDistance) {
                    nearest = onlinePlayer;
                    nearestDistance = distance;
                }
            }
        }

        return nearest;
    }
}
