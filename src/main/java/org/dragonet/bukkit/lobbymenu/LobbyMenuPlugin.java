package org.dragonet.bukkit.lobbymenu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created on 2017/9/18.
 */
public class LobbyMenuPlugin extends JavaPlugin implements Listener {

    public final static String PREFIX_MENU = "\u00a70menu:";

    private ItemMenu menu;

    private YamlConfiguration config;
    private Map<String, YamlConfiguration> menuConfigs = new HashMap<>();
    private File menusDir;

    @Override
    public YamlConfiguration getConfig() {
        return config;
    }

    public ItemMenu getMenu() {
        return menu;
    }

    public Map<String, YamlConfiguration> getMenuConfigs() {
        return Collections.unmodifiableMap(menuConfigs);
    }

    @Override
    public void onEnable() {
        getLogger().info("Creating menu system... ");
        menu = new ItemMenu(this);
        getLogger().info("Loading language file... ");
        saveResource("lang.yml", false);
        Lang.lang = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "lang.yml"));
        getLogger().info("Registering channel... ");
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getLogger().info("Loading configurations... ");
        saveResource("config.yml", false);
        saveResource("menus/test.yml", false);
        getLogger().info("Registering events... ");
        getServer().getPluginManager().registerEvents(menu, this);
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new ItemUseListener(this), this);
        getCommand("menu").setExecutor(new MenuCommand(this));

        reloadConfigurations();
    }

    public void reloadConfigurations() {
        if(menu != null) menu.cleanUp();
        menu = null;
        menuConfigs = null;

        menu = new ItemMenu(this);
        getServer().getPluginManager().registerEvents(menu, this);
        menuConfigs = new HashMap<>();

        config = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));
        getLogger().info("Loading menues... ");
        menusDir = new File(getDataFolder(), "menus");
        menusDir.mkdirs();
        loadMenus();

        Bukkit.getServer().getOnlinePlayers().forEach(this::applyHotbar);
    }

    @Override
    public void onDisable() {
        menu.cleanUp();
        menu = null;
        menuConfigs = null;
    }

    private void loadMenus() {
        File[] files = menusDir.listFiles(((dir, name) -> {
            if(name.toLowerCase().endsWith(".yml")) {
                return true;
            } else {
                return false;
            }
        }));
        for(File f : files) {
            if(f.isDirectory()) continue;
            String name = f.getName().substring(0, f.getName().length() - 4);
            getLogger().info(" -- Loading menu [" + name + "]");
            YamlConfiguration c = YamlConfiguration.loadConfiguration(f);
            menuConfigs.put(name, c);
        }
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent e) {
        applyHotbar(e.getPlayer());
    }

    public boolean isButton(int slot) {
        return config.contains("hotbar." + slot);
    }

    @EventHandler
    private void onItemClick(InventoryClickEvent e) {
        if(e.getWhoClicked().hasPermission("lobby.admin")) {
            return;
        }
        if(e.getClickedInventory().getType().equals(InventoryType.PLAYER) && isButton(e.getSlot())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    private void onItemDrop(PlayerDropItemEvent e){
        if(e.getPlayer().hasPermission("lobby.admin")) {
            return;
        }
        if(isButton(e.getPlayer().getInventory().getHeldItemSlot())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    private void onDimensionChange(PlayerTeleportEvent e) {
        getServer().getScheduler().runTaskLater(this, () -> applyHotbar(e.getPlayer()) , 20L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlayerDeath(PlayerDeathEvent e) {
        if(e.getKeepInventory()) return;
        Iterator<ItemStack> iterator = e.getDrops().iterator();
        while(iterator.hasNext()) {
            ItemStack i = iterator.next();
            if(ItemUseListener.searchItemName(i) != null) iterator.remove();
        }
    }

    @EventHandler
    private void onRespawn(PlayerRespawnEvent e) {
        applyHotbar(e.getPlayer());
    }

    @EventHandler
    private void onHotbarManipulate(InventoryClickEvent e) {
        if(e.getSlotType().equals(InventoryType.SlotType.QUICKBAR)) {
            if(config.getConfigurationSection("hotbar").getKeys(false).contains(Integer.toString(e.getSlot()))) {
                e.setCancelled(true);
            }
        }
    }

    public void applyHotbar(Player p) {
        ConfigurationSection hotbar = config.getConfigurationSection("hotbar");
        for(String strSlotIndex : hotbar.getKeys(false)) {
            int slot = Integer.parseInt(strSlotIndex);
            ItemStack item = new ItemStack(
                    Material.matchMaterial(hotbar.getString(strSlotIndex + ".material")),
                    config.getInt(strSlotIndex + ".amount", 1)
            );
            ItemMeta meta = item.getItemMeta();
            if(hotbar.contains(strSlotIndex + ".enchantments") && hotbar.isList(strSlotIndex + ".enchantments")) {
                for(String strEnchantAndLevel : hotbar.getStringList(strSlotIndex + ".enchantments")) {
                    String[] split = strEnchantAndLevel.split(":");
                    NamespacedKey key = NamespacedKey.minecraft(split[0].toLowerCase());
                    Enchantment enchantment = Enchantment.getByKey(key);
                    if(enchantment == null) {
                        getLogger().warning("Failed getting enchantment with key: " + key.toString());
                        continue;
                    }
                    meta.addEnchant(enchantment,
                            Integer.parseInt(split[1]),
                            true);
                }
            }
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

            if(hotbar.getBoolean(strSlotIndex + ".hide-enchantments", false)) {
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            List<String> lore = hotbar.getStringList(strSlotIndex + ".lore").stream().map(l -> l.replace("&", "\u00a7")).collect(Collectors.toList());
            lore.add(PREFIX_MENU + hotbar.getString(strSlotIndex + ".menu"));
            meta.setDisplayName(hotbar.getString(strSlotIndex + ".name").replace("&", "\u00a7"));
            meta.setLore(lore);
            item.setItemMeta(meta);
            p.getInventory().setItem(slot, item);
        }
    }

}
