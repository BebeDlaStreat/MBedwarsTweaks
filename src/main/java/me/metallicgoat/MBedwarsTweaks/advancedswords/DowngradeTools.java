package me.metallicgoat.MBedwarsTweaks.advancedswords;

import de.marcely.bedwars.api.BedwarsAPI;
import de.marcely.bedwars.api.arena.Arena;
import de.marcely.bedwars.api.event.arena.RoundStartEvent;
import de.marcely.bedwars.api.event.player.PlayerBuyInShopEvent;
import de.marcely.bedwars.api.event.player.PlayerIngameDeathEvent;
import de.marcely.bedwars.api.event.player.PlayerIngameRespawnEvent;
import de.marcely.bedwars.api.game.shop.BuyGroup;
import de.marcely.bedwars.api.game.shop.ShopItem;
import me.metallicgoat.MBedwarsTweaks.Main;
import me.metallicgoat.MBedwarsTweaks.utils.ServerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Collection;
import java.util.HashMap;

public class DowngradeTools implements Listener {

    private static final HashMap<Player, Integer> pickaxeHashMap = new HashMap<>();
    private static final HashMap<Player, Integer> axeHashMap = new HashMap<>();

    @EventHandler
    public void onStart(RoundStartEvent e){
        if(ServerManager.getSwordsToolsConfig().getBoolean("Degraded-Tool-BuyGroups")
                && ServerManager.getSwordsToolsConfig().getBoolean("Advanced-Tool-Replacement.Enabled")) {
            for (Player player : e.getArena().getPlayers()) {
                pickaxeHashMap.put(player, 0);
                axeHashMap.put(player, 0);
            }
        }
    }

    @EventHandler
    public void onRespawn(PlayerIngameRespawnEvent e){
        if(ServerManager.getSwordsToolsConfig().getBoolean("Degraded-Tool-BuyGroups")
                && ServerManager.getSwordsToolsConfig().getBoolean("Advanced-Tool-Replacement.Enabled")) {
            Player player = e.getPlayer();
            Arena arena = e.getArena();
            Collection<BuyGroup> buyGroups = BedwarsAPI.getGameAPI().getBuyGroups();
            for (BuyGroup buyGroup : buyGroups) {
                String buyGroupName = buyGroup.getName();
                if (buyGroupName.equalsIgnoreCase("pickaxe")
                        || buyGroup.getName().equalsIgnoreCase("axe")) {
                    int level = buyGroupName.equalsIgnoreCase("pickaxe") ? pickaxeHashMap.get(player) : axeHashMap.get(player);
                    Collection<? extends ShopItem> shopItems = buyGroup.getItems(level);
                    if(shopItems != null) {
                        for (ShopItem item : shopItems) {
                            BukkitScheduler scheduler = Bukkit.getScheduler();
                            scheduler.runTaskLater(Main.getInstance(), () -> item.getProducts().forEach(shopProduct -> {
                                arena.setBuyGroupLevel(player, buyGroup, level);
                                shopProduct.give(e.getPlayer(), e.getArena().getPlayerTeam(player), e.getArena(), 1);
                            }), 1L);
                            break;
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void levelOneBuy(PlayerBuyInShopEvent e) {
        Player p = e.getPlayer();

        //If enabled, and item has buy-group
        if(e.getProblems().isEmpty() && e.getItem().hasBuyGroup() &&
                ServerManager.getSwordsToolsConfig().getBoolean("Degraded-Tool-BuyGroups")
                && ServerManager.getSwordsToolsConfig().getBoolean("Advanced-Tool-Replacement.Enabled")){

            BuyGroup group = e.getItem().getBuyGroup();
            int level = e.getItem().getBuyGroupLevel();

            assert group != null;
            //if proper buy-group
            if(level == 1 && (group.getName().equalsIgnoreCase("axe")
                    || group.getName().equalsIgnoreCase("pickaxe"))) {

                if(group.getName().equalsIgnoreCase("pickaxe")){
                    pickaxeHashMap.put(p, 1);
                }else{
                    axeHashMap.put(p, 1);
                }
            }
        }
    }

    @EventHandler
    public void onRespawn(PlayerIngameDeathEvent e){
        if(ServerManager.getSwordsToolsConfig().getBoolean("Degraded-Tool-BuyGroups")
                && ServerManager.getSwordsToolsConfig().getBoolean("Advanced-Tool-Replacement.Enabled")) {
            Player player = e.getPlayer();
            Arena arena = e.getArena();

            Collection<BuyGroup> buyGroups = BedwarsAPI.getGameAPI().getBuyGroups();
            for (BuyGroup buyGroup : buyGroups) {
                if (buyGroup.getName().contains("axe")) {
                    int level = arena.getBuyGroupLevel(player, buyGroup);
                    if (level > 1) {
                        if (buyGroup.getName().equalsIgnoreCase("pickaxe")) {
                            pickaxeHashMap.put(player, level - 1);
                            arena.setBuyGroupLevel(player, buyGroup, level - 1);
                        } else if (buyGroup.getName().equalsIgnoreCase("axe")) {
                            axeHashMap.put(player, level - 1);
                            arena.setBuyGroupLevel(player, buyGroup, level - 1);
                        }
                    }
                }
            }
        }
    }
}
