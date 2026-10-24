package com.baronclient.gui;

import com.baronclient.modules.Module;
import com.baronclient.modules.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Locale;

public class BaronScreen extends Screen {
    private static final int RED = 0xFFFF3B4F;
    private static final int RED_DARK = 0xFF7D202B;
    private static final int BG = 0xF20A0A0F;
    private static final int SIDEBAR = 0xF20E0E14;
    private static final int CARD = 0xFF17171F;
    private static final int CARD_HOVER = 0xFF22222D;
    private static final int MUTED = 0xFF92929F;
    private static final String[] CATEGORIES = {"All Modules", "Combat", "HUD", "Render", "Utility"};
    private String category = "All Modules";
    private boolean compact = false;
    private boolean settingsOpen = true;
    private boolean waitingForKey = false;
    private Module selected;
    private int scroll = 0;
    private float openProgress = 0f;
    private int animTick = 0;
    private String search = "";
    private boolean searchFocused = false;

    public BaronScreen() { super(Text.literal("Baron Client")); }

    private int panelW() { return Math.min(1000, width - 32); }
    private int panelH() { return Math.min(620, height - 28); }
    private int left() { return (width - panelW()) / 2; }
    private int top() { return (height - panelH()) / 2; }
    private int sidebarW() { return 164; }
    private int settingsW() { return settingsOpen ? 232 : 0; }
    private int contentLeft() { return left() + sidebarW() + 20; }
    private int contentRight() { return left() + panelW() - settingsW() - 20; }

    @Override protected void init() { }

    private List<Module> filtered() {
        return ModuleManager.all().stream()
                .filter(m -> category.equals("All Modules") || m.category.equals(category))
                .filter(m -> search.isBlank() || m.name.toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT)))
                .toList();
    }

    @Override
    public void render(DrawContext d, int mouseX, int mouseY, float delta) {
        animTick++;
        openProgress = Math.min(1f, openProgress + 0.12f);
        d.fill(0, 0, width, height, 0xB8000000);
        int x = left(), y = top(), w = panelW(), h = panelH();
        int offset = (int)((1f - openProgress) * 14);
        d.fill(x + offset, y + offset, x + w + offset, y + h + offset, BG);
        d.fill(x + offset, y + offset, x + sidebarW() + offset, y + h + offset, SIDEBAR);

        // Brand header and animated accent line.
        d.fill(x + 18 + offset, y + 18 + offset, x + 22 + offset, y + 54 + offset, RED);
        d.drawTextWithShadow(textRenderer, Text.literal("BARON"), x + 32 + offset, y + 19 + offset, 0xFFFFFFFF);
        d.drawTextWithShadow(textRenderer, Text.literal("CLIENT"), x + 32 + offset, y + 32 + offset, RED);
        d.drawTextWithShadow(textRenderer, Text.literal("PVP  /  1.21.11"), x + 18 + offset, y + 67 + offset, MUTED);
        d.fill(x + 14 + offset, y + 91 + offset, x + sidebarW() - 14 + offset, y + 92 + offset, 0xFF2A2A34);
        d.drawTextWithShadow(textRenderer, Text.literal("WORKSPACE"), x + 18 + offset, y + 104 + offset, MUTED);

        int navY = y + 126 + offset;
        for (int i = 0; i < CATEGORIES.length; i++) {
            int ny = navY + i * 35;
            boolean active = category.equals(CATEGORIES[i]);
            boolean hover = inside(mouseX, mouseY, x + 10 + offset, ny - 5, sidebarW() - 20, 29);
            if (active || hover) d.fill(x + 10 + offset, ny - 5, x + sidebarW() - 10 + offset, ny + 24, active ? 0xFF2B171D : CARD_HOVER);
            if (active) d.fill(x + 10 + offset, ny - 5, x + 13 + offset, ny + 24, RED);
            String icon = switch (i) { case 1 -> "⚔"; case 2 -> "▦"; case 3 -> "◈"; case 4 -> "⚙"; default -> "▤"; };
            d.drawTextWithShadow(textRenderer, Text.literal(icon), x + 22 + offset, ny + 4, active ? RED : MUTED);
            d.drawTextWithShadow(textRenderer, Text.literal(CATEGORIES[i]), x + 43 + offset, ny + 4, active ? 0xFFFFFFFF : 0xFFB8B8C2);
        }

        int bottomY = y + h - 54 + offset;
        d.fill(x + 14 + offset, bottomY - 12, x + sidebarW() - 14 + offset, bottomY - 11, 0xFF2A2A34);
        d.fill(x + 19 + offset, bottomY + 3, x + 27 + offset, bottomY + 11, 0xFF52D68A);
        d.drawTextWithShadow(textRenderer, Text.literal("FABRIC READY"), x + 34 + offset, bottomY + 2, 0xFFB9C9BE);
        d.drawTextWithShadow(textRenderer, Text.literal("BARON  •  BUILD 01"), x + 18 + offset, bottomY + 20, MUTED);

        // Main content header.
        int cx = contentLeft() + offset;
        int cr = contentRight() + offset;
        int cw = cr - cx;
        d.drawTextWithShadow(textRenderer, Text.literal(category.equals("All Modules") ? "Module Library" : category),
                cx, y + 22 + offset, 0xFFFFFFFF);
        d.drawTextWithShadow(textRenderer, Text.literal("Tune your client. Make every frame count."),
                cx, y + 37 + offset, MUTED);

        // Search field is custom drawn, not a vanilla widget.
        int searchY = y + 60 + offset;
        d.fill(cx, searchY, cx + Math.min(245, cw - 176), searchY + 27, searchFocused ? 0xFF24242E : CARD);
        d.drawTextWithShadow(textRenderer, Text.literal("⌕"), cx + 9, searchY + 8, searchFocused ? RED : MUTED);
        String searchLabel = search.isEmpty() ? "Search modules..." : search;
        d.drawTextWithShadow(textRenderer, Text.literal(searchLabel + (searchFocused && animTick % 30 < 15 ? "▏" : "")),
                cx + 28, searchY + 8, search.isEmpty() ? MUTED : 0xFFECECF2);

        int layoutX = cr - 144;
        pill(d, layoutX, searchY, 68, 27, compact ? "COMPACT" : "FULL", compact, mouseX, mouseY);
        pill(d, layoutX + 74, searchY, 70, 27, "HUD EDIT", false, mouseX, mouseY);

        int listY = y + 104 + offset;
        d.drawTextWithShadow(textRenderer, Text.literal(filtered().size() + " MODULES"),
                cx, listY, RED);
        d.drawTextWithShadow(textRenderer, Text.literal("CLICK A CARD TO TOGGLE  •  OPEN THE SLIDER ICON FOR OPTIONS"),
                cx + 92, listY, MUTED);

        int gridTop = listY + 20;
        int availableW = Math.max(180, cw);
        int gap = 9;
        int columns = compact ? 1 : (availableW >= 460 ? 2 : 1);
        int cardW = Math.max(150, (availableW - gap * (columns - 1)) / columns);
        int cardH = compact ? 46 : 62;
        int visibleRows = Math.max(1, (y + h - 26 - gridTop) / (cardH + gap));
        List<Module> mods = filtered();
        int maxScroll = Math.max(0, (mods.size() + columns - 1) / columns - visibleRows);
        scroll = Math.min(scroll, maxScroll);
        for (int i = 0; i < mods.size(); i++) {
            Module m = mods.get(i);
            int col = i % columns;
            int row = i / columns - scroll;
            if (row < 0 || row >= visibleRows) continue;
            int mx = cx + col * (cardW + gap);
            int my = gridTop + row * (cardH + gap);
            boolean hover = inside(mouseX, mouseY, mx, my, cardW, cardH);
            d.fill(mx, my, mx + cardW, my + cardH, hover ? CARD_HOVER : CARD);
            d.fill(mx, my, mx + 3, my + cardH, m.enabled ? m.accentColor : 0xFF34343E);
            d.drawTextWithShadow(textRenderer, Text.literal(m.name), mx + 13, my + 10, m.enabled ? 0xFFFFFFFF : 0xFFC2C2CB);
            d.drawTextWithShadow(textRenderer, Text.literal(m.category.toUpperCase(Locale.ROOT)),
                    mx + 13, my + 28, MUTED);
            if (!compact) {
                d.drawTextWithShadow(textRenderer, Text.literal(m.enabled ? "ACTIVE" : "OFF"),
                        mx + cardW - 76, my + 11, m.enabled ? 0xFF69E5A1 : MUTED);
                int sw = 28, sh = 14, sx = mx + cardW - 41, sy = my + 31;
                d.fill(sx, sy, sx + sw, sy + sh, m.enabled ? RED_DARK : 0xFF34343E);
                int knobX = m.enabled ? sx + sw - 11 : sx + 3;
                d.fill(knobX, sy + 3, knobX + 8, sy + sh - 3, m.enabled ? RED : 0xFFB4B4BE);
            } else {
                d.fill(mx + cardW - 33, my + 15, mx + cardW - 13, my + 31, m.enabled ? RED : 0xFF34343E);
                d.drawTextWithShadow(textRenderer, Text.literal(m.enabled ? "✓" : "·"), mx + cardW - 27, my + 19, 0xFFFFFFFF);
            }
            // Dedicated options glyph at the far right of every card.
            d.drawTextWithShadow(textRenderer, Text.literal("⋯"), mx + cardW - 17, my + cardH - 17, MUTED);
            if (selected == m) {
                d.fill(mx + 4, my + cardH - 2, mx + cardW - 4, my + cardH - 1, RED);
            }
        }

        // Settings drawer.
        if (settingsOpen) {
            int sx = x + w - settingsW() - 14 + offset;
            int sy = y + 112 + offset;
            int sw = settingsW() - 12;
            d.fill(sx, sy, sx + sw, y + h - 18 + offset, 0xFF111117);
            d.drawTextWithShadow(textRenderer, Text.literal("MODULE SETTINGS"), sx + 12, sy + 12, MUTED);
            if (selected == null) {
                d.drawTextWithShadow(textRenderer, Text.literal("Select a module"), sx + 12, sy + 42, 0xFFFFFFFF);
                d.drawTextWithShadow(textRenderer, Text.literal("to customize its look"), sx + 12, sy + 56, MUTED);
                d.drawTextWithShadow(textRenderer, Text.literal("and behaviour."), sx + 12, sy + 69, MUTED);
            } else {
                d.drawTextWithShadow(textRenderer, Text.literal(selected.name), sx + 12, sy + 38, 0xFFFFFFFF);
                d.drawTextWithShadow(textRenderer, Text.literal(selected.enabled ? "● ENABLED" : "○ DISABLED"),
                        sx + 12, sy + 54, selected.enabled ? 0xFF69E5A1 : MUTED);
                d.fill(sx + 12, sy + 77, sx + sw - 12, sy + 78, 0xFF30303A);
                d.drawTextWithShadow(textRenderer, Text.literal("HUD SCALE"), sx + 12, sy + 90, MUTED);
                d.drawTextWithShadow(textRenderer, Text.literal(String.format(Locale.ROOT, "%.1fx", selected.scale)),
                        sx + sw - 48, sy + 90, 0xFFFFFFFF);
                customButton(d, sx + 12, sy + 110, 36, 25, "−", mouseX, mouseY);
                customButton(d, sx + 54, sy + 110, 36, 25, "+", mouseX, mouseY);
                customButton(d, sx + 12, sy + 145, sw - 24, 26,
                        selected.showBackground ? "BACKGROUND  ON" : "BACKGROUND  OFF", mouseX, mouseY);
                customButton(d, sx + 12, sy + 180, sw - 24, 26, "CHANGE ACCENT COLOUR", mouseX, mouseY);
                customButton(d, sx + 12, sy + 215, sw - 24, 26,
                        waitingForKey ? "PRESS A KEY..." : "SET MODULE KEYBIND", mouseX, mouseY);
                d.drawTextWithShadow(textRenderer, Text.literal("HUD POSITION"), sx + 12, sy + 256, MUTED);
                d.drawTextWithShadow(textRenderer, Text.literal(selected.hudX < 0 ? "Default layout" : selected.hudX + ", " + selected.hudY),
                        sx + 12, sy + 273, 0xFFECECF2);
                customButton(d, sx + 12, sy + 295, sw - 24, 26, "OPEN HUD EDITOR", mouseX, mouseY);
            }
        }

        d.drawTextWithShadow(textRenderer, Text.literal("RIGHT SHIFT  •  CLOSE"), x + w - 170 + offset, y + h - 13 + offset, MUTED);
    }

    private void pill(DrawContext d, int x, int y, int w, int h, String label, boolean active, int mx, int my) {
        boolean hover = inside(mx, my, x, y, w, h);
        d.fill(x, y, x + w, y + h, active ? RED_DARK : hover ? CARD_HOVER : CARD);
        d.drawCenteredTextWithShadow(textRenderer, Text.literal(label), x + w / 2, y + 9, active ? 0xFFFFFFFF : 0xFFE1E1E8);
    }

    private void customButton(DrawContext d, int x, int y, int w, int h, String label, int mx, int my) {
        d.fill(x, y, x + w, y + h, inside(mx, my, x, y, w, h) ? 0xFF34343F : 0xFF24242D);
        d.drawCenteredTextWithShadow(textRenderer, Text.literal(label), x + w / 2, y + (h - 8) / 2, 0xFFEAEAF0);
    }

    private boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubleClick) {
        double mx = click.x(), my = click.y();
        if (click.button() != 0) return super.mouseClicked(click, doubleClick);
        int x = left(), y = top(), w = panelW(), h = panelH(), offset = (int)((1f - openProgress) * 14);
        int navX = x + 10 + offset, navY = y + 121 + offset;
        for (int i = 0; i < CATEGORIES.length; i++) {
            if (inside(mx, my, navX, navY + i * 35, sidebarW() - 20, 29)) {
                category = CATEGORIES[i]; scroll = 0; selected = null; searchFocused = false; return true;
            }
        }
        int cx = contentLeft() + offset, cr = contentRight() + offset, cw = cr - cx;
        int searchY = y + 60 + offset;
        if (inside(mx, my, cx, searchY, Math.min(245, cw - 176), 27)) {
            searchFocused = true; return true;
        } else searchFocused = false;
        int layoutX = cr - 144;
        if (inside(mx, my, layoutX, searchY, 68, 27)) { compact = !compact; scroll = 0; return true; }
        if (inside(mx, my, layoutX + 74, searchY, 70, 27)) {
            if (client != null) client.setScreen(new HudEditorScreen()); return true;
        }

        int listY = y + 124 + offset;
        int gridTop = listY;
        int availableW = Math.max(180, cw);
        int gap = 9, columns = compact ? 1 : (availableW >= 460 ? 2 : 1);
        int cardW = Math.max(150, (availableW - gap * (columns - 1)) / columns);
        int cardH = compact ? 46 : 62;
        int visibleRows = Math.max(1, (y + h - 26 - gridTop) / (cardH + gap));
        List<Module> mods = filtered();
        int maxScroll = Math.max(0, (mods.size() + columns - 1) / columns - visibleRows);
        scroll = Math.min(scroll, maxScroll);
        for (int i = 0; i < mods.size(); i++) {
            Module m = mods.get(i);
            int col = i % columns, row = i / columns - scroll;
            if (row < 0 || row >= visibleRows) continue;
            int mx0 = cx + col * (cardW + gap), my0 = gridTop + row * (cardH + gap);
            if (inside(mx, my, mx0, my0, cardW, cardH)) {
                selected = m;
                if (mx >= mx0 + cardW - 24 && my >= my0 + cardH - 23) {
                    settingsOpen = true;
                } else {
                    m.toggle(); ModuleManager.save();
                }
                return true;
            }
        }
        if (settingsOpen && selected != null) {
            int sx = x + w - settingsW() - 14 + offset, sy = y + 112 + offset, sw = settingsW() - 12;
            if (inside(mx, my, sx + 12, sy + 110, 36, 25)) selected.adjustScale(-0.1f);
            else if (inside(mx, my, sx + 54, sy + 110, 36, 25)) selected.adjustScale(0.1f);
            else if (inside(mx, my, sx + 12, sy + 145, sw - 24, 26)) selected.toggleBackground();
            else if (inside(mx, my, sx + 12, sy + 180, sw - 24, 26)) selected.cycleAppearance();
            else if (inside(mx, my, sx + 12, sy + 215, sw - 24, 26)) { waitingForKey = true; return true; }
            else if (inside(mx, my, sx + 12, sy + 295, sw - 24, 26)) {
                if (client != null) client.setScreen(new HudEditorScreen()); return true;
            } else return super.mouseClicked(click, doubleClick);
            ModuleManager.save(); return true;
        }
        return super.mouseClicked(click, doubleClick);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int key = input.key();
        if (waitingForKey && selected != null) {
            selected.keyCode = key == GLFW.GLFW_KEY_ESCAPE ? 0 : key;
            waitingForKey = false; ModuleManager.save(); return true;
        }
        if (key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_RIGHT_SHIFT) { close(); return true; }
        if (searchFocused) {
            if (key == GLFW.GLFW_KEY_BACKSPACE && !search.isEmpty()) {
                search = search.substring(0, search.length() - 1);
            } else if (key == GLFW.GLFW_KEY_ENTER) {
                searchFocused = false;
            } else if (key >= GLFW.GLFW_KEY_A && key <= GLFW.GLFW_KEY_Z && search.length() < 40) {
                search += (char) ('a' + key - GLFW.GLFW_KEY_A);
            } else if (key >= GLFW.GLFW_KEY_0 && key <= GLFW.GLFW_KEY_9 && search.length() < 40) {
                search += (char) ('0' + key - GLFW.GLFW_KEY_0);
            } else if (key == GLFW.GLFW_KEY_SPACE && search.length() < 40) {
                search += " ";
            } else if (key == GLFW.GLFW_KEY_MINUS && search.length() < 40) {
                search += "-";
            } else {
                return super.keyPressed(input);
            }
            scroll = 0; return true;
        }
        if (key == GLFW.GLFW_KEY_TAB) { settingsOpen = !settingsOpen; return true; }
        return super.keyPressed(input);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        scroll = Math.max(0, Math.min(100, scroll - (int)Math.signum(vertical)));
        return true;
    }

    @Override public boolean shouldPause() { return false; }
}