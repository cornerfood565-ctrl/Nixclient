package com.baronclient.modules;

public class Module {
    public final String name, category;
    public boolean enabled;
    public float scale = 1.0f;
    public int accentColor = 0xFFFF3434;
    public boolean showBackground = true;
    public int keyCode = 0;

    public Module(String name, String category, boolean enabled) {
        this.name = name;
        this.category = category;
        this.enabled = enabled;
    }

    public void toggle() { enabled = !enabled; }
    public void adjustScale(float amount) { scale = Math.max(0.5f, Math.min(2.0f, scale + amount)); }
    public void cycleAppearance() {
        accentColor = accentColor == 0xFFFF3434 ? 0xFFFCFCFC
                : accentColor == 0xFFFCFCFC ? 0xFF7C4DFF : 0xFFFF3434;
    }
    public void toggleBackground() { showBackground = !showBackground; }
}