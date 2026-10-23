package com.baronclient.modules;

import net.minecraft.client.MinecraftClient;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.HashSet;
import java.util.Set;
import org.lwjgl.glfw.GLFW;

public final class ModuleManager {
    private static final List<Module> modules = new ArrayList<>();
    private static Path configPath;
    private static final Set<Integer> heldKeys = new HashSet<>();

    private ModuleManager() {}

    public static void init() {
        if (!modules.isEmpty()) return;
        String[][] defs = {
            {"CPS Counter","Combat","true"},{"Keystrokes","Combat","true"},{"Hit Indicator","Combat","true"},
            {"Custom Crosshair","Render","true"},{"Damage Flash","Render","true"},{"Fullbright","Render","false"},
            {"Item Physics","Render","false"},{"Motion Blur","Render","false"},
            {"FPS","HUD","true"},{"Coordinates","HUD","true"},{"Ping","HUD","true"},
            {"Potion Effects","HUD","true"},{"Armor HUD","HUD","true"},{"Boss Bar","HUD","true"},
            {"Scoreboard","HUD","true"},{"Direction","HUD","false"},{"Clock","HUD","false"},{"Memory","HUD","false"},
            {"Performance Mode","Utility","false"},{"Particles","Utility","true"},{"Animations","Utility","true"}
        };
        for (String[] d : defs) modules.add(new Module(d[0], d[1], Boolean.parseBoolean(d[2])));
        configPath = MinecraftClient.getInstance().runDirectory.toPath().resolve("config").resolve("baronclient.properties");
        load();
    }

    public static List<Module> all() { return modules; }
    public static Module get(String name) {
        return modules.stream().filter(m -> m.name.equals(name)).findFirst().orElse(null);
    }
    public static void tick(MinecraftClient client) {
        if (client == null || client.getWindow() == null || client.currentScreen != null) {
            heldKeys.clear();
            return;
        }
        Set<Integer> nowHeld = new HashSet<>();
        long window = client.getWindow().getHandle();
        for (Module m : modules) {
            if (m.keyCode <= 0) continue;
            if (GLFW.glfwGetKey(window, m.keyCode) == GLFW.GLFW_PRESS) {
                nowHeld.add(m.keyCode);
                if (!heldKeys.contains(m.keyCode)) {
                    m.toggle();
                    save();
                }
            }
        }
        heldKeys.clear();
        heldKeys.addAll(nowHeld);
    }

    public static void save() {
        if (configPath == null) return;
        Properties p = new Properties();
        for (Module m : modules) {
            String key = m.name.replace(' ', '_');
            p.setProperty(key + ".enabled", Boolean.toString(m.enabled));
            p.setProperty(key + ".scale", Float.toString(m.scale));
            p.setProperty(key + ".color", Integer.toHexString(m.accentColor));
            p.setProperty(key + ".background", Boolean.toString(m.showBackground));
            p.setProperty(key + ".keyCode", Integer.toString(m.keyCode));
        }
        try {
            Files.createDirectories(configPath.getParent());
            try (OutputStream out = Files.newOutputStream(configPath)) {
                p.store(out, "Baron Client settings");
            }
        } catch (IOException ignored) {}
    }

    private static void load() {
        if (configPath == null || !Files.exists(configPath)) return;
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(configPath)) {
            p.load(in);
            for (Module m : modules) {
                String key = m.name.replace(' ', '_');
                m.enabled = Boolean.parseBoolean(p.getProperty(key + ".enabled", Boolean.toString(m.enabled)));
                try { m.scale = Math.max(0.5f, Math.min(2.0f, Float.parseFloat(p.getProperty(key + ".scale", "1.0")))); } catch (NumberFormatException ignored) {}
                try { m.accentColor = (int) Long.parseLong(p.getProperty(key + ".color", Integer.toHexString(m.accentColor)), 16); } catch (NumberFormatException ignored) {}
                m.showBackground = Boolean.parseBoolean(p.getProperty(key + ".background", "true"));
                try { m.keyCode = Integer.parseInt(p.getProperty(key + ".keyCode", "0")); } catch (NumberFormatException ignored) {}
            }
        } catch (IOException ignored) {}
    }
}