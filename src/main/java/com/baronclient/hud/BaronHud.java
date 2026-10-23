package com.baronclient.hud;

import com.baronclient.modules.ModuleManager;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

public final class BaronHud {
    public static void init() {
        HudRenderCallback.EVENT.register((draw, tickDelta) -> render(draw));
    }
    private static void render(DrawContext draw) {
        MinecraftClient c = MinecraftClient.getInstance();
        if (c.player == null || c.options.hudHidden) return;
        int x=8,y=8; int line=12;
        draw.drawTextWithShadow(c.textRenderer, Text.literal("BARON"), x, y, 0xFFFFFFFF); y+=line+2;
        if (on("FPS")) { draw.drawTextWithShadow(c.textRenderer, Text.literal("FPS  " + c.getCurrentFps()), x,y,0xFFE6E6E6); y+=line; }
        ClientPlayerEntity p=c.player;
        if (on("Coordinates")) { draw.drawTextWithShadow(c.textRenderer, Text.literal(String.format("XYZ  %.0f  %.0f  %.0f",p.getX(),p.getY(),p.getZ())),x,y,0xFFE6E6E6); y+=line; }
        if (on("Ping")) { int ping=-1; if(c.getNetworkHandler()!=null && p.getUuid()!=null && c.getNetworkHandler().getPlayerListEntry(p.getUuid())!=null) ping=c.getNetworkHandler().getPlayerListEntry(p.getUuid()).getLatency(); draw.drawTextWithShadow(c.textRenderer,Text.literal("PING  "+ping+"ms"),x,y,0xFFE6E6E6); }
    }
    private static boolean on(String n){ var m=ModuleManager.get(n); return m!=null&&m.enabled; }
}
