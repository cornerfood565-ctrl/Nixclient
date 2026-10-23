package com.baronclient.gui;

import com.baronclient.modules.Module;
import com.baronclient.modules.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class BaronScreen extends Screen {
    private static final int RED = 0xFFFF3434;
    private static final int PANEL = 0xF2121217;
    private static final int PANEL_LIGHT = 0xFF1D1D24;
    private static final String[] CATEGORIES = {"All", "Combat", "HUD", "Render", "Utility"};
    private String category = "All";
    private boolean compact = false;
    private int scroll = 0;
    private int animationTick = 0;
    private Module selected;
    private TextFieldWidget searchField;
    private final List<ButtonWidget> moduleButtons = new ArrayList<>();

    public BaronScreen() { super(Text.literal("Baron Client")); }

    @Override
    protected void init() {
        clearChildren();
        moduleButtons.clear();
        int panelLeft = Math.max(12, (width - Math.min(760, width - 24)) / 2);
        int panelWidth = Math.min(760, width - 24);
        int top = Math.max(12, height / 2 - Math.min(260, height / 2 - 8));
        int innerLeft = panelLeft + 16;

        addDrawableChild(ButtonWidget.builder(Text.literal(compact ? "LAYOUT: COMPACT" : "LAYOUT: FULL"),
                b -> { compact = !compact; scroll = 0; init(); })
                .dimensions(panelLeft + panelWidth - 160, top + 12, 144, 22).build());

        searchField = new TextFieldWidget(textRenderer, innerLeft, top + 44, Math.max(100, panelWidth - 32), 22, Text.literal("Search modules"));
        searchField.setMaxLength(64);
        searchField.setChangedListener(s -> { scroll = 0; rebuildModuleButtons(); });
        addDrawableChild(searchField);

        int catY = top + 73;
        int catWidth = Math.max(46, (panelWidth - 32) / CATEGORIES.length);
        for (int i = 0; i < CATEGORIES.length; i++) {
            final String value = CATEGORIES[i];
            addDrawableChild(ButtonWidget.builder(Text.literal(value.toUpperCase()), b -> {
                category = value; scroll = 0; init();
            }).dimensions(innerLeft + i * catWidth, catY, catWidth - 4, 20).build());
        }
        rebuildModuleButtons();
    }

    private int panelLeft() { return Math.max(12, (width - Math.min(760, width - 24)) / 2); }
    private int panelWidth() { return Math.min(760, width - 24); }
    private int panelTop() { return Math.max(12, height / 2 - Math.min(260, height / 2 - 8)); }

    private List<Module> filteredModules() {
        String query = searchField == null ? "" : searchField.getText().trim().toLowerCase();
        return ModuleManager.all().stream()
                .filter(m -> category.equals("All") || m.category.equals(category))
                .filter(m -> query.isEmpty() || m.name.toLowerCase().contains(query))
                .toList();
    }

    private void rebuildModuleButtons() {
        for (ButtonWidget button : moduleButtons) remove(button);
        moduleButtons.clear();
        int left = panelLeft() + 16;
        int top = panelTop() + 126;
        int panelW = panelWidth();
        int availableHeight = height - top - 26;
        int columns = compact ? 1 : (panelW >= 540 ? 2 : 1);
        int gap = 8;
        int cellW = (panelW - 32 - gap * (columns - 1)) / columns;
        List<Module> filtered = filteredModules();
        int visibleRows = Math.max(1, availableHeight / 32);
        int maxScroll = Math.max(0, ((filtered.size() + columns - 1) / columns) - visibleRows);
        scroll = Math.max(0, Math.min(scroll, maxScroll));
        for (int i = 0; i < filtered.size(); i++) {
            Module m = filtered.get(i);
            int col = i % columns;
            int row = i / columns - scroll;
            int y = top + row * 32;
            if (row < 0 || y + 27 > height - 18) continue;
            int x = left + col * (cellW + gap);
            ButtonWidget toggle = ButtonWidget.builder(Text.literal((m.enabled ? "●  " : "○  ") + m.name),
                    b -> { m.toggle(); ModuleManager.save(); rebuildModuleButtons(); })
                    .dimensions(x, y, Math.max(70, cellW - 34), 26).build();
            ButtonWidget settings = ButtonWidget.builder(Text.literal("⚙"),
                    b -> { selected = m; initSettingsButtons(); })
                    .dimensions(x + Math.max(70, cellW - 30), y, 30, 26).build();
            moduleButtons.add(toggle);
            moduleButtons.add(settings);
            addDrawableChild(toggle);
            addDrawableChild(settings);
        }
    }

    private void initSettingsButtons() {
        if (selected == null) return;
        // The selected module settings are represented by small, explicit controls at the bottom of the panel.
        int y = height - 44;
        int left = panelLeft() + 16;
        int w = Math.max(80, (panelWidth() - 40) / 3);
        addDrawableChild(ButtonWidget.builder(Text.literal("SIZE −"),
                b -> { selected.adjustScale(-0.1f); ModuleManager.save(); init(); })
                .dimensions(left, y, w, 22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("SIZE +"),
                b -> { selected.adjustScale(0.1f); ModuleManager.save(); init(); })
                .dimensions(left + w + 4, y, w, 22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("CYCLE COLOUR"),
                b -> { selected.cycleAppearance(); ModuleManager.save(); init(); })
                .dimensions(left + (w + 4) * 2, y, w, 22).build());
    }

    @Override
    public void render(DrawContext d, int mouseX, int mouseY, float delta) {
        animationTick++;
        d.fill(0, 0, width, height, 0xB9000000);
        int left = panelLeft(), w = panelWidth(), top = panelTop(), bottom = height - 12;
        d.fill(left, top, left + w, bottom, PANEL);
        d.fill(left, top, left + w, top + 38, PANEL_LIGHT);
        int lineW = Math.max(30, (animationTick * 5) % Math.max(31, w - 24));
        d.fill(left + 12, top + 36, left + 12 + lineW, top + 38, RED);
        d.drawTextWithShadow(textRenderer, Text.literal("BARON CLIENT"), left + 16, top + 12, 0xFFFFFFFF);
        d.drawTextWithShadow(textRenderer, Text.literal("1.21.11  •  CLIENT SETTINGS"), left + 16, top + 25, 0xFFAAAAAF);
        d.drawTextWithShadow(textRenderer, Text.literal(category.toUpperCase() + "  /  " + filteredModules().size() + " MODULES"),
                left + 16, top + 113, RED);
        super.render(d, mouseX, mouseY, delta);
        if (selected != null) {
            d.fill(left + 8, bottom - 30, left + w - 8, bottom - 4, 0xF01A1A21);
            d.drawTextWithShadow(textRenderer, Text.literal(selected.name + "  •  " + (selected.enabled ? "ON" : "OFF")
                    + "  •  Scale " + String.format(java.util.Locale.ROOT, "%.1f", selected.scale)
                    + "  •  Background " + (selected.showBackground ? "ON" : "OFF")),
                    left + 14, bottom - 20, selected.accentColor);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        scroll = Math.max(0, scroll - (int) Math.signum(vertical));
        rebuildModuleButtons();
        return true;
    }

    @Override
    public boolean shouldPause() { return false; }
}