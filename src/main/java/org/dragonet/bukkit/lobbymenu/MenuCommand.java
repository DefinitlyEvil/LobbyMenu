package org.dragonet.bukkit.lobbymenu;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created on 2017/11/14.
 */
public class MenuCommand implements CommandExecutor {

    private final LobbyMenuPlugin plugin;

    public MenuCommand(LobbyMenuPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if(!Player.class.isAssignableFrom(sender.getClass())) return true;
        plugin.applyHotbar((Player)sender);
        sender.sendMessage(Lang.HOTBAR_RESTORED.build());
        return true;
    }
}
