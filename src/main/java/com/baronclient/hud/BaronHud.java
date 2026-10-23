package com.baronclient.hud;

import com.baronclient.modules.Module;
import com.baronclient.modules.ModuleManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public final class BaronHud {
    private static final Deque<Long> leftClicks = new ArrayDeque<>();
    private static final Deque<Long> rightClicks = new ArrayDeque<>();
    private static boolean leftDown, rightDown;

    private BaronHud() {}

    public static void init() {
        HudRenderCallback.EVENT.register((draw, tickDelta) -> render(draw));
        ClientTickEvents.END_CLIENT_TICK.register(BaronHud::pollClicks);
    }

    private static void pollClicks(MinecraftClient c) {
        if (c.getWindow() == null || c.currentScreen != null) {
            leftDown = false;
            rightDown = false;
            return;
        }
        long window = c.getWindow().getHandle();
        boolean l = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_1) == GLFW.GLFW_PRESS;
        boolean r = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_2) == GLFW.GLFW_PRESS;
        long now = System.currentTimeMillis();
        if (l && !leftDown) leftClicks.addLast(now);
        if (r && !rightDown) rightClicks.addLast(now);
        leftDown = l;
        rightDown = r;
        trim(leftClicks, now);
        trim(rightClicks, now);
    }

    private static void trim(Deque<Long> clicks, long now) {
        while (!clicks.isEmpty() && now - clicks.peekFirst() > 1000L) clicks.removeFirst();
    }

    private static void render(DrawContext draw) {
        MinecraftClient c = MinecraftClient.getInstance();
        if (c.player == null || c.options.hudHidden) return;
        int x = 9, y = 9, line = 13;
        draw.drawTextWithShadow(c.textRenderer, Text.literal("BARON"), x, y, 0xFFFF3434);
        y += line + 3;
        if (on("FPS")) y = line(draw, "FPS  " + c.getCurrentFps(), x, y, onColor("FPS"));
        ClientPlayerEntity p = c.player;
        if (on("Coordinates")) y = line(draw, String.format(java.util.Locale.ROOT, "XYZ  %.0f  %.0f  %.0f", p.getX(), p.getY(), p.getZ()), x, y, onColor("Coordinates"));
        if (on("Ping")) {
            int ping = -1;
            if (c.getNetworkHandler() != null && p.getUuid() != null && c.getNetworkHandler().getPlayerListEntry(p.getUuid()) != null)
                ping = c.getNetworkHandler().getPlayerListEntry(p.getUuid()).getLatency();
            y = line(draw, "PING  " + ping + "ms", x, y, onColor("Ping"));
        }
        if (on("CPS Counter")) y = line(draw, "CPS  " + leftClicks.size() + " | " + rightClicks.size(), x, y, onColor("CPS Counter"));
        if (on("Keystrokes")) renderKeys(draw, c);
        if (on("Memory")) {
            Runtime runtime = Runtime.getRuntime();
            long used = (runtime.totalMemory() - runtime.freeMemory()) / (1024L * 1024L);
            line(draw, "MEM  " + used + " MB", x, y, onColor("Memory"));
        }
    }

    private static int line(DrawContext draw, String text, int x, int y, int color) {
        Module m = ModuleManager.get(text.startsWith("FPS") ? "FPS"
                : text.startsWith("XYZ") ? "Coordinates"
                : text.startsWith("PING") ? "Ping"
                : text.startsWith("CPS") ? "CPS Counter" : "Memory");
        int drawX = m != null && m.hudX >= 0 ? m.hudX : x;
        int drawY = m != null && m.hudY >= 0 ? m.hudY : y;
        float scale = m == null ? 1.0f : m.scale;
        draw.getMatrices().push();
        draw.getMatrices().translate(drawX, drawY);
        draw.getMatrices().scale(scale, scale);
        if (m != null && m.showBackground) draw.fill(-3, -2, 128, 10, 0x990C0C10);
        draw.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.literal(text), 0, 0, color);
        draw.getMatrices().pop();
        return y + 13;
    }

    private static void renderKeys(DrawContext draw, MinecraftClient c) {
        Module m = ModuleManager.get("Keystrokes");
        int x = m != null && m.hudX >= 0 ? m.hudX : c.getWindow().getScaledWidth() - 76;
        int y = m != null && m.hudY >= 0 ? m.hudY : c.getWindow().getScaledHeight() - 82;
        float scale = m == null ? 1.0f : m.scale;
        draw.getMatrices().push();
        draw.getMatrices().translate(x, y);
        draw.getMatrices().scale(scale, scale, 1.0f);
        key(draw, "W", 24, 0, c.options.forwardKey.isPressed());
        key(draw, "A", 0, 24, c.options.leftKey.isPressed());
        key(draw, "S", 24, 24, c.options.backKey.isPressed());
        key(draw, "D", 48, 24, c.options.rightKey.isPressed());
        key(draw, "LMB", -4, 50, leftDown);
        key(draw, "RMB", 36, 50, rightDown);
        draw.getMatrices().pop();
    }

    private static void key(DrawContext d, String label, int x, int y, boolean pressed) {
        int bg = pressed ? 0xFFFF3434 : 0xDD15151B;
        d.fill(x, y, x + (label.length() > 1 ? 36 : 20), y + 20, bg);
        d.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.literal(label),
                x + (label.length() > 1 ? 18 : 10), y + 6, 0xFFFFFFFF);
    }

    private static boolean on(String name) {
        Module m = ModuleManager.get(name);
        return m != null && m.enabled;
    }

    private static int onColor(String name) {
        Module m = ModuleManager.get(name);
        return m == null ? 0xFFFF3434 : m.accentColor;
    }
}