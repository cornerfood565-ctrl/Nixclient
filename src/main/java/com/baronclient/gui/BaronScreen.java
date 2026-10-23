package com.baronclient.gui;

import com.baronclient.modules.Module;
import com.baronclient.modules.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import java.util.List;

public class BaronScreen extends Screen {
    private final List<Module> modules = ModuleManager.all();
    private int scroll;
    public BaronScreen(){ super(Text.literal("Baron Client")); }
    @Override protected void init() { rebuild(); }
    private void rebuild(){
        clearChildren();
        int left=Math.max(20,width/2-210), top=50;
        int shown=0;
        for(int i=0;i<modules.size();i++){
            Module m=modules.get(i); int yy=top+(shown*28)-scroll;
            if(yy<35||yy>height-30) continue;
            int col=(i%2); int x=left+col*215;
            addDrawableChild(ButtonWidget.builder(Text.literal((m.enabled?"● ":"○ ")+m.name), b->{m.toggle(); rebuild();})
                    .dimensions(x,yy,205,22).build());
            if(col==1) shown++;
        }
    }
    @Override public void render(DrawContext d,int mouseX,int mouseY,float delta){
        d.fill(0,0,width,height,0xF20A0A0D);
        d.fill(width/2-225,22,width/2+225,height-18,0xFF111116);
        d.fill(width/2-225,22,width/2+225,55,0xFF17171D);
        d.drawTextWithShadow(textRenderer,Text.literal("BARON CLIENT"),width/2-205,33,0xFFFFFFFF);
        d.drawTextWithShadow(textRenderer,Text.literal("1.21.11  •  PvP / HUD / Visuals"),width/2-205,44,0xFF9999A3);
        super.render(d,mouseX,mouseY,delta);
    }
    @Override public boolean mouseScrolled(double mouseX,double mouseY,double horizontal,double vertical){ scroll=Math.max(0,Math.min(300,scroll-(int)(vertical*28))); rebuild(); return true; }
}
