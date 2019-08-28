package org.dragonet.bukkit.lobbymenu;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.stream.Collectors;

import static org.dragonet.bukkit.lobbymenu.LobbyMenuPlugin.PREFIX_MENU;

/**
 * Created on 2017/9/18.
 */
public class ItemUseListener implements Listener {

    private final LobbyMenuPlugin plugin;

    public ItemUseListener(LobbyMenuPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void onItemUse(PlayerInteractEvent e) {
        if(!plugin.isButton(e.getPlayer().getInventory().getHeldItemSlot())) return;
        if(!(e.getAction().equals(Action.RIGHT_CLICK_AIR) ||e.getAction().equals(Action.RIGHT_CLICK_BLOCK))) {
            return;
        }
        if(e.getItem() == null || e.getItem().getType().equals(Material.AIR)) return;
        if(!e.getPlayer().hasPermission("lobby.admin")) {
            e.setCancelled(true);
        }
        ItemStack used_item = e.getItem();
        String menu_name = searchItemName(used_item);
        if(menu_name == null) return;
        if(menu_name.startsWith("cmd:")) {
            e.getPlayer().performCommand(menu_name.substring(4));
        } else {
            openMenuForPlayer(e.getPlayer(), menu_name);
        }
    }

    private void openMenuForPlayer(Player player, String menu_name) {
        if(!plugin.getMenuConfigs().containsKey(menu_name)) {
            player.sendMessage(Lang.MENU_NOT_FOUND.build(menu_name));
            return;
        }
        YamlConfiguration config = plugin.getMenuConfigs().get(menu_name);
        ConfigurationSection buttons = config.getConfigurationSection("buttons");
        ItemMenuInstance i = new ItemMenuInstance(config.getString("title").replace("&", "\u00a7"), config.getInt("size"));
        for(String strSlot : buttons.getKeys(false)) {
            ConfigurationSection b = buttons.getConfigurationSection(strSlot);
            int slot = Integer.parseInt(strSlot);
            ItemMenu.MenuItemHandler handler;
            String type = b.getString("type");
            if(type.equalsIgnoreCase("server")) {
                handler = new SwitchServerButtonHandler(plugin, b.getString("value"));
            }else if(type.equalsIgnoreCase("menu")) {
                handler = ((human, menu) -> openMenuForPlayer((Player)human, b.getString("value")));
            } else if(type.equalsIgnoreCase("command")) {
                handler = ((human, menu) -> ((Player) human).performCommand(b.getString("value")));
            } else if(type.equalsIgnoreCase("message")) {
                handler = ((human, menu) -> {
                    if(!b.contains("message")) {
                        human.sendMessage(Lang.MENU_NOT_FOUND.build());
                        return;
                    }
                    for(String s : b.getStringList("message")) {
                        String replaced = ChatColor.translateAlternateColorCodes('&', s);
                        human.sendMessage(replaced);
                    }
                });
            } else {
                handler = ((human, menu) -> human.sendMessage(Lang.ACTION_NOT_DEFINED.build()));
            }
            i.setButton(slot,
                    Material.matchMaterial(b.getString("material")),
                    b.getString("name").replace("&", "\u00a7"),
                    b.getStringList("lore").stream().map((l) -> l.replace("&", "\u00a7")).collect(Collectors.toList()),
                    handler);
        }
        plugin.getMenu().open(player, i);
    }

    public static String searchItemName(ItemStack item) {
        if(item == null) return null;
        ItemMeta meta = item.getItemMeta();
        if(!meta.hasLore()) return null;
        for (String l : meta.getLore()) {
            if(l.startsWith(PREFIX_MENU)) {
                return l.substring(PREFIX_MENU.length());
            }
        }
        return null;
    }
}
