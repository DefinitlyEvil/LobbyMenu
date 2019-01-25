package org.dragonet.bukkit.lobbymenu;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

/**
 * Created on 2017/9/18.
 */
public class SwitchServerButtonHandler implements ItemMenu.MenuItemHandler {

    private final LobbyMenuPlugin plugin;
    private final String server;

    public SwitchServerButtonHandler(LobbyMenuPlugin plugin, String server) {
        this.plugin = plugin;
        this.server = server;
    }

    @Override
    public void onClick(HumanEntity human, ItemMenuInstance menu) {
        if(!Player.class.isAssignableFrom(human.getClass())) return;
        Player player = (Player) human;

        player.sendMessage(Lang.CONNECTING.build(server));

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);

        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }
}
