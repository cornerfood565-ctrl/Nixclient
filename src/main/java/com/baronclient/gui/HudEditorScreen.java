package com.baronclient.gui;

import com.baronclient.modules.Module;
import com.baronclient.modules.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Click;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

public class HudEditorScreen extends Screen {
    private static final int RED = 0xFFFF3434;
    private Module dragging;
    private int dragOffsetX, dragOffsetY;

    public HudEditorScreen() { super(Text.literal("Baron HUD Editor")); }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.literal("RESET LAYOUT"), b -> {
            for (Module m : ModuleManager.all()) { m.hudX = -1; m.hudY = -1; }
            ModuleManager.save();
        }).dimensions(width / 2 - 116, 12, 112, 22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("DONE"), b -> close())
                .dimensions(width / 2 + 4, 12, 112, 22).build());
    }

    @Override
    public void render(DrawContext d, int mouseX, int mouseY, float delta) {
        d.fill(0, 0, width, height, 0x99000000);
        d.fill(12, 42, width - 12, height - 12, 0xA80D0D12);
        d.drawCenteredTextWithShadow(textRenderer, Text.literal("BARON HUD EDITOR"), width / 2, 43, 0xFFFFFFFF);
        d.drawCenteredTextWithShadow(textRenderer, Text.literal("DRAG ELEMENTS TO MOVE  •  ESC TO SAVE & EXIT"), width / 2, 56, 0xFFB5B5BD);

        MinecraftClient c = MinecraftClient.getInstance();
        int index = 0;
        for (Module m : ModuleManager.all()) {
            if (!m.enabled || !isHudModule(m.name)) continue;
            int[] pos = positionFor(m, index++, c);
            String label = previewText(m, c);
            int x = m.hudX >= 0 ? m.hudX : pos[0];
            int y = m.hudY >= 0 ? m.hudY : pos[1];
            int w = Math.max(58, textRenderer.getWidth(label) + 12);
            int h = 20;
            d.fill(x, y, x + w, y + h, m.showBackground ? 0xE819191F : 0x6619191F);
            d.fill(x, y, x + 2, y + h, m.accentColor);
            d.drawTextWithShadow(textRenderer, Text.literal(label), x + 7, y + 6, 0xFFFFFFFF);
            if (m == dragging || (mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h)) {
                d.fill(x, y, x + w, y + 1, RED);
                d.fill(x, y + h - 1, x + w, y + h, RED);
            }
        }
        super.render(d, mouseX, mouseY, delta);
    }

    private boolean isHudModule(String name) {
        return List.of("FPS", "Coordinates", "Ping", "CPS Counter", "Keystrokes", "Memory").contains(name);
    }

    private int[] positionFor(Module m, int index, MinecraftClient c) {
        if (m.name.equals("Keystrokes")) return new int[]{Math.max(8, c.getWindow().getScaledWidth() - 92), Math.max(70, c.getWindow().getScaledHeight() - 96)};
        int row = 0;
        for (Module other : ModuleManager.all()) {
            if (other == m) break;
            if (other.enabled && isHudModule(other.name) && !other.name.equals("Keystrokes")) row++;
        }
        return new int[]{10, 12 + row * 18};
    }

    private String previewText(Module m, MinecraftClient c) {
        if (m.name.equals("FPS")) return "FPS  " + c.getCurrentFps();
        if (m.name.equals("Coordinates") && c.player != null)
            return String.format(java.util.Locale.ROOT, "XYZ  %.0f  %.0f  %.0f", c.player.getX(), c.player.getY(), c.player.getZ());
        if (m.name.equals("Ping")) {
            if (c.player == null || c.getNetworkHandler() == null) return "PING  --";
            var entry = c.getNetworkHandler().getPlayerListEntry(c.player.getUuid());
            return "PING  " + (entry == null ? -1 : entry.getLatency()) + "ms";
        }
        if (m.name.equals("CPS Counter")) return "CPS  0 | 0";
        if (m.name.equals("Keystrokes")) return "W  A  S  D  LMB  RMB";
        return "MEM  " + (Runtime.getRuntime().totalMemory() / (1024 * 1024)) + " MB";
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubleClick) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();
        if (button == 0) {
            MinecraftClient c = MinecraftClient.getInstance();
            int index = 0;
            for (Module m : ModuleManager.all()) {
                if (!m.enabled || !isHudModule(m.name)) continue;
                int[] pos = positionFor(m, index++, c);
                int x = m.hudX >= 0 ? m.hudX : pos[0];
                int y = m.hudY >= 0 ? m.hudY : pos[1];
                int w = Math.max(58, textRenderer.getWidth(previewText(m, c)) + 12);
                if (mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + 20) {
                    dragging = m;
                    dragging.hudX = x;
                    dragging.hudY = y;
                    dragOffsetX = (int) mouseX - x;
                    dragOffsetY = (int) mouseY - y;
                    return true;
                }
            }
        }
        return super.mouseClicked(click, doubleClick);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        int button = click.button();
        if (dragging != null && button == 0) {
            dragging.hudX = Math.max(0, Math.min(width - 30, dragging.hudX + (int) deltaX));
            dragging.hudY = Math.max(0, Math.min(height - 20, dragging.hudY + (int) deltaY));
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (dragging != null) {
            dragging = null;
            ModuleManager.save();
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() == 256) { close(); return true; }
        return super.keyPressed(input);
    }

    @Override
    public void close() {
        ModuleManager.save();
        super.close();
    }

    @Override
    public boolean shouldPause() { return false; }
}