package org.dragonet.bukkit.lobbymenu;

import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Created on 2017/9/18.
 */
public enum Lang {

    MENU_NOT_FOUND,
    CONNECTING,
    ACTION_NOT_DEFINED,
    HOTBAR_RESTORED,
    MESSAGE_NOT_DEFINED;


    public static YamlConfiguration lang;

    public String build(Object... options) {
        return String.format(lang.getString(name()), options);
    }
}
