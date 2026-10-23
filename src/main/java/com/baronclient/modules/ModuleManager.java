package com.baronclient.modules;

import net.minecraft.client.MinecraftClient;
import java.util.*;

public final class ModuleManager {
    private static final List<Module> modules = new ArrayList<>();
    public static void init() {
        if (!modules.isEmpty()) return;
        String[][] defs = {
            {"CPS Counter","PvP","true"},{"Keystrokes","PvP","true"},{"Hit Indicator","PvP","true"},{"Motion Blur","Visual","false"},
            {"Custom Crosshair","Visual","true"},{"Damage Flash","Visual","true"},{"Fullbright","Visual","false"},{"Item Physics","Visual","false"},
            {"FPS","HUD","true"},{"Coordinates","HUD","true"},{"Ping","HUD","true"},{"Potion Effects","HUD","true"},{"Armor HUD","HUD","true"},
            {"Boss Bar","HUD","true"},{"Scoreboard","HUD","true"},{"Direction","HUD","false"},{"Clock","HUD","false"},{"Memory","HUD","false"},
            {"Performance Mode","Performance","false"},{"Particles","Performance","true"},{"Animations","Visual","true"}
        };
        for (String[] d : defs) modules.add(new Module(d[0],d[1],Boolean.parseBoolean(d[2])));
    }
    public static List<Module> all(){ return modules; }
    public static Module get(String name){ return modules.stream().filter(m->m.name.equals(name)).findFirst().orElse(null); }
    public static void tick(MinecraftClient client) { }
}
