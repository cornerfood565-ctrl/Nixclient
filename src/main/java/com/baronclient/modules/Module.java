package com.baronclient.modules;

public class Module {
    public final String name, category;
    public boolean enabled;
    public Module(String name, String category, boolean enabled) { this.name=name; this.category=category; this.enabled=enabled; }
    public void toggle(){ enabled=!enabled; }
}
