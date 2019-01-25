package org.dragonet.bukkit.lobbymenu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created on 2017/8/23.
 */
public class ItemMenuInstance {

    private final String id;
    private final Inventory inventory;
    private final HashMap<Integer, ItemMenu.MenuItemHandler> handlers = new HashMap<>();

    public String getId() {
        return id;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public HashMap<Integer, ItemMenu.MenuItemHandler> getHandlers() {
        return handlers;
    }

    public ItemMenuInstance(String name, int size) {
        size = size + (9 - size % 9); // regulation, prevent errors by accident
        id = UUID.randomUUID().toString().substring(0, 8);
        inventory = Bukkit.createInventory(null, size, name + " " + ItemMenu.MENU_IDENTIFIER_START + id + ItemMenu.MENU_IDENTIFIER_END);
        int exitIndex = size / 9 * 9 - 1;
        setButton(exitIndex, Material.BARRIER, "\u00a7cClose Menu", (p, i) -> p.closeInventory());
    }

    public void setButton(int index, Material icon, String name, ItemMenu.MenuItemHandler handler) {
        setButton(index, icon, name, null, handler);
    }

    public void setButton(int index, Material icon, String name, List<String> lores, ItemMenu.MenuItemHandler handler) {
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if(lores != null) {
            meta.setLore(lores);
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        inventory.setItem(index, item);
        handlers.put(index, handler);
    }

}
