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
    private boolean settingsOpen = false;
    private boolean waitingForKey = false;
    private Module selected;
    private int scroll = 0;
    private float openProgress = 0f;
    private int animTick = 0;
    private String search = "";
    private boolean searchFocused = false;

    public BaronScreen() { super(Text.literal("Baron Client")); }

    private int panelW() { return Math.max(1, Math.min(900, (int)(width * 0.84))); }
    private int panelH() { return Math.max(1, Math.min(540, (int)(height * 0.82))); }
    private int left() { return (width - panelW()) / 2; }
    private int top() { return (height - panelH()) / 2; }
    private int sidebarW() { return 0; }
    private int settingsW() { return settingsOpen ? Math.min(232, Math.max(168, panelW() - sidebarW() - 220)) : 0; }
    private int contentLeft() { return left() + 14; }
    private int contentRight() { return left() + panelW() - 14; }

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
        openProgress = Math.min(1f, openProgress + 0.16f);
        d.fill(0, 0, width, height, 0x99000000);

        int x = left(), y = top(), w = panelW(), h = panelH();
        int off = (int)((1f - openProgress) * 8);
        d.fill(x + off, y + off, x + w + off, y + h + off, 0xF20A0B10);
        d.fill(x + off, y + off, x + w + off, y + 2 + off, RED);
        d.fill(x + 12 + off, y + 12 + off, x + w - 12 + off, y + 43 + off, 0xFF111219);

        d.drawTextWithShadow(textRenderer, Text.literal("◈  BARON CLIENT"), x + 22 + off, y + 23 + off, 0xFFFFFFFF);
        d.drawTextWithShadow(textRenderer, Text.literal("1.21.11"), x + 128 + off, y + 23 + off, MUTED);
        pill(d, x + w - 112 + off, y + 15 + off, 94, 22, "✕  CLOSE", false, mouseX, mouseY);

        int tabY = y + 53 + off;
        String[] tabs = {"ALL", "NEW", "HUD", "HYPIXEL", "PVP"};
        int tabW = Math.max(43, Math.min(60, (w - 240) / 5));
        int tabX = x + 14 + off;
        for (int i = 0; i < tabs.length; i++) {
            boolean active = (i == 0 && category.equals("All Modules"))
                    || (i == 2 && category.equals("HUD"))
                    || (i == 4 && category.equals("Combat"))
                    || (i == 3 && category.equals("Render"))
                    || (i == 1 && category.equals("Utility"));
            pill(d, tabX + i * (tabW + 4), tabY, tabW, 22, tabs[i], active, mouseX, mouseY);
        }

        int searchW = Math.min(160, Math.max(100, w / 4));
        int searchX = x + w - searchW - 76 + off;
        d.fill(searchX, tabY, searchX + searchW, tabY + 22, searchFocused ? 0xFF292A32 : CARD);
        d.drawTextWithShadow(textRenderer, Text.literal("⌕"), searchX + 7, tabY + 7, RED);
        String searchText = search.isEmpty() ? "Search..." : search;
        d.drawTextWithShadow(textRenderer, Text.literal(searchText + (searchFocused && animTick % 30 < 15 ? "▏" : "")),
                searchX + 22, tabY + 7, search.isEmpty() ? MUTED : 0xFFEAEAF0);
        pill(d, x + w - 62 + off, tabY, 22, 22, "♥", false, mouseX, mouseY);
        pill(d, x + w - 36 + off, tabY, 22, 22, "▦", false, mouseX, mouseY);

        int contentX = x + 14 + off;
        int contentW = w - 28;
        int labelY = y + 83 + off;
        List<Module> mods = filtered();
        d.drawTextWithShadow(textRenderer, Text.literal("MODULES  " + mods.size()), contentX, labelY, RED);

        int gridY = y + 101 + off;
        int gap = 7;
        int columns = contentW >= 520 ? 5 : contentW >= 390 ? 4 : contentW >= 280 ? 3 : 2;
        int cardW = Math.max(54, (contentW - gap * (columns - 1)) / columns);
        int cardH = Math.max(56, Math.min(72, (h - 130) / 3));
        int rowsVisible = Math.max(1, (y + h - 13 - gridY) / (cardH + gap));
        int maxScroll = Math.max(0, (mods.size() + columns - 1) / columns - rowsVisible);
        scroll = Math.max(0, Math.min(scroll, maxScroll));

        for (int i = 0; i < mods.size(); i++) {
            Module m = mods.get(i);
            int col = i % columns, row = i / columns - scroll;
            if (row < 0 || row >= rowsVisible) continue;
            int mx = contentX + col * (cardW + gap);
            int my = gridY + row * (cardH + gap);
            boolean hover = inside(mouseX, mouseY, mx, my, cardW, cardH);
            d.fill(mx, my, mx + cardW, my + cardH, hover ? 0xFF292A33 : CARD);
            d.fill(mx, my, mx + cardW, my + 2, m.enabled ? 0xFF4DCA75 : 0xFF34343E);
            d.drawTextWithShadow(textRenderer, Text.literal(iconFor(m.name)), mx + cardW / 2 - 5, my + 9, 0xFFE8E8EE);
            String name = m.name.length() > Math.max(8, cardW / 6) ? m.name.substring(0, Math.max(5, cardW / 6 - 1)) + "…" : m.name;
            d.drawCenteredTextWithShadow(textRenderer, Text.literal(name), mx + cardW / 2, my + 26, 0xFFF0F0F4);
            d.drawCenteredTextWithShadow(textRenderer, Text.literal(m.enabled ? "ENABLED" : "DISABLED"),
                    mx + cardW / 2, my + 39, m.enabled ? 0xFF62D783 : MUTED);
            d.fill(mx + 5, my + cardH - 14, mx + cardW - 5, my + cardH - 5, m.enabled ? 0xFF174C25 : 0xFF24252C);
            d.drawCenteredTextWithShadow(textRenderer, Text.literal(m.enabled ? "ON" : "OFF"),
                    mx + cardW / 2, my + cardH - 12, m.enabled ? 0xFF9BEBA9 : MUTED);
            d.drawTextWithShadow(textRenderer, Text.literal("⚙"), mx + cardW - 11, my + 4, MUTED);
            if (selected == m) d.fill(mx + 3, my + cardH - 2, mx + cardW - 3, my + cardH - 1, RED);
        }
        d.drawTextWithShadow(textRenderer, Text.literal("RIGHT SHIFT  •  CLOSE"), x + w - 118 + off, y + h - 12 + off, MUTED);
    }

    private String iconFor(String name) {
        return switch (name) {
            case "CPS Counter" -> "⌁";
            case "Keystrokes" -> "⌨";
            case "Hit Indicator", "Attack Indicator" -> "✛";
            case "Custom Crosshair" -> "⊕";
            case "Damage Flash" -> "▰";
            case "Fullbright" -> "☀";
            case "Item Physics" -> "◆";
            case "Motion Blur" -> "◌";
            case "FPS" -> "FPS";
            case "Coordinates" -> "⌖";
            case "Ping" -> "⌁";
            case "Potion Effects" -> "⚗";
            case "Armor HUD" -> "♜";
            case "Boss Bar" -> "━";
            case "Scoreboard" -> "▤";
            case "Direction" -> "➤";
            case "Clock" -> "◷";
            case "Memory" -> "▥";
            default -> "✦";
        };
    }

    private void pill(DrawContext d, int x, int y, int w, int h, String label, boolean active, int mx, int my) {
        boolean hover = inside(mx, my, x, y, w, h);
        d.fill(x, y, x + w, y + h, active ? RED_DARK : hover ? CARD_HOVER : CARD);
        d.drawCenteredTextWithShadow(textRenderer, Text.literal(label), x + w / 2, y + (h - 8) / 2,
                active ? 0xFFFFFFFF : 0xFFE1E1E8);
    }

    private boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubleClick) {
        double mx = click.x(), my = click.y();
        if (click.button() != 0) return super.mouseClicked(click, doubleClick);
        int x = left(), y = top(), w = panelW(), h = panelH(), off = (int)((1f - openProgress) * 8);
        if (inside(mx, my, x + w - 112 + off, y + 15 + off, 94, 22)) { close(); return true; }

        int tabY = y + 53 + off;
        int tabW = Math.max(43, Math.min(60, (w - 240) / 5));
        int tabX = x + 14 + off;
        String[] tabs = {"ALL", "NEW", "HUD", "HYPIXEL", "PVP"};
        for (int i = 0; i < tabs.length; i++) {
            if (inside(mx, my, tabX + i * (tabW + 4), tabY, tabW, 22)) {
                category = switch (i) { case 1 -> "Utility"; case 2 -> "HUD"; case 3 -> "Render"; case 4 -> "Combat"; default -> "All Modules"; };
                scroll = 0; selected = null; return true;
            }
        }
        int searchW = Math.min(160, Math.max(100, w / 4));
        int searchX = x + w - searchW - 76 + off;
        if (inside(mx, my, searchX, tabY, searchW, 22)) { searchFocused = true; return true; }
        searchFocused = false;

        int contentX = x + 14 + off, contentW = w - 28;
        int gridY = y + 101 + off, gap = 7;
        int columns = contentW >= 520 ? 5 : contentW >= 390 ? 4 : contentW >= 280 ? 3 : 2;
        int cardW = Math.max(54, (contentW - gap * (columns - 1)) / columns);
        int cardH = Math.max(56, Math.min(72, (h - 130) / 3));
        int rowsVisible = Math.max(1, (y + h - 13 - gridY) / (cardH + gap));
        List<Module> mods = filtered();
        int maxScroll = Math.max(0, (mods.size() + columns - 1) / columns - rowsVisible);
        scroll = Math.max(0, Math.min(scroll, maxScroll));
        for (int i = 0; i < mods.size(); i++) {
            Module m = mods.get(i);
            int col = i % columns, row = i / columns - scroll;
            if (row < 0 || row >= rowsVisible) continue;
            int mx0 = contentX + col * (cardW + gap), my0 = gridY + row * (cardH + gap);
            if (inside(mx, my, mx0, my0, cardW, cardH)) {
                selected = m;
                if (mx >= mx0 + cardW - 18 && my <= my0 + 18) {
                    settingsOpen = !settingsOpen;
                } else {
                    m.toggle(); ModuleManager.save();
                }
                return true;
            }
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