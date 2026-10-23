package com.baronclient;

import com.baronclient.gui.BaronScreen;
import com.baronclient.hud.BaronHud;
import com.baronclient.modules.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class BaronClient implements ClientModInitializer {
    public static final String NAME = "Baron Client";
    public static KeyBinding menuKey;

    @Override
    public void onInitializeClient() {
        ModuleManager.init();
        BaronHud.init();
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.baronclient.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.baronclient"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (menuKey.wasPressed() && client.currentScreen == null) client.setScreen(new BaronScreen());
            ModuleManager.tick(client);
        });
    }
}
