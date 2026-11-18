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
    private static final int ORANGE=0xFFF5A623, GREEN=0xFF2ECC40, BG=0xFF0A0A0A;
    private static final int CARD=0xFF1A1A1A, BORDER=0xFF2A2A2A, MUTED=0xFF929292, WHITE=0xFFF4F4F4;
    private static final String[] CATEGORIES={"All","HUD","Render","Movement","Player","Utility"};
    private String category="All", search="";
    private boolean searchFocused=false, settingsOpen=false, presetsOpen=false;
    private Module selected;
    private int scroll=0, tick=0;

    public BaronScreen(){super(Text.literal("Baron Client"));}
    private int panelW(){return Math.min(1120,Math.max(320,width-24));}
    private int panelH(){return Math.min(760,Math.max(260,height-24));}
    private int left(){return (width-panelW())/2;}
    private int top(){return (height-panelH())/2;}
    private boolean hit(double mx,double my,int x,int y,int w,int h){return mx>=x&&mx<x+w&&my>=y&&my<y+h;}
    private List<Module> filtered(){
        return ModuleManager.all().stream()
            .filter(m->category.equals("All")||m.category.equals(category))
            .filter(m->search.isBlank()||m.name.toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT)))
            .toList();
    }
    private String icon(String n){
        return switch(n){
            case "FPS Counter"->"▣"; case "Coordinates"->"✛"; case "Direction"->"➤"; case "Clock"->"◷";
            case "Memory Usage"->"▤"; case "Biome Display"->"♧"; case "Resource Packs"->"▧";
            case "Server Info"->"◉"; case "Ping"->"⌁"; case "Server TPS"->"◌"; case "Day Counter"->"◷";
            case "Speed"->"»"; case "Fullbright"->"☼"; case "Cape"->"◇"; case "Custom Sky"->"☾";
            case "Entity Hider"->"◉"; case "Item Physics"->"◆"; case "Sprint"->"➜"; case "NoFall"->"⬡";
            case "Fly"->"✦"; case "Step"->"↟"; case "AutoHeal"->"♥"; case "Reach Display"->"↔";
            case "Armor Status"->"⬡"; case "Potion HUD"->"⚗"; case "Auto GG"->"✓";
            case "Chat Filter"->"≋"; case "Screenshot"->"▣"; case "FPS Boost"->"ϟ"; default->"✦";
        };
    }
    @Override protected void init(){}
    @Override public void render(DrawContext d,int mouseX,int mouseY,float delta){
        tick++;
        d.fill(0,0,width,height,0xCC000000);
        int x=left(),y=top(),w=panelW(),h=panelH();
        d.fill(x,y,x+w,y+h,BG); d.fill(x,y,x+w,y+3,ORANGE);
        int pad=18, headerY=y+16;
        d.fill(x+pad,headerY,x+w-pad,headerY+42,CARD);
        button(d,x+pad+10,headerY+8,132,26,"≡ MOD MENU",true,mouseX,mouseY);
        button(d,x+w-pad-40,headerY+8,30,26,"✕",false,mouseX,mouseY);
        int tabsY=headerY+54, tabsX=x+pad;
        int rightW=310, available=w-pad*2-rightW-10, tabW=Math.max(38,available/6);
        for(int i=0;i<CATEGORIES.length;i++){
            int tx=tabsX+i*tabW; boolean active=category.equals(CATEGORIES[i]);
            d.fill(tx,tabsY,tx+tabW-5,tabsY+32,active?0xFF302416:CARD);
            d.drawCenteredTextWithShadow(textRenderer,Text.literal(CATEGORIES[i]),tx+(tabW-5)/2,tabsY+12,active?ORANGE:MUTED);
            if(active)d.fill(tx,tabsY+29,tx+tabW-5,tabsY+32,ORANGE);
        }
        int searchW=Math.min(190,Math.max(100,rightW-130));
        int searchX=x+w-pad-searchW-112;
        d.fill(searchX,tabsY,searchX+searchW,tabsY+32,searchFocused?0xFF292929:CARD);
        d.drawTextWithShadow(textRenderer,Text.literal("⌕ "+(search.isEmpty()?"Search modules":search)),searchX+8,tabsY+12,search.isEmpty()?MUTED:WHITE);
        button(d,x+w-pad-104,tabsY,48,32,"RESET",false,mouseX,mouseY);
        button(d,x+w-pad-50,tabsY,50,32,"PRESET",false,mouseX,mouseY);

        int gridX=x+pad, gridY=tabsY+48, gridW=w-pad*2;
        List<Module> mods=filtered();
        d.drawTextWithShadow(textRenderer,Text.literal(category.toUpperCase(Locale.ROOT)+"  /  "+mods.size()+" MODULES"),gridX,gridY,ORANGE);
        gridY+=22;
        int gap=10, cols=4, cardW=(gridW-gap*3)/4, cardH=112;
        int visible=Math.max(1,(y+h-18-gridY)/(cardH+gap));
        int totalRows=(mods.size()+cols-1)/cols;
        int maxScroll=Math.max(0,totalRows-visible); scroll=Math.max(0,Math.min(scroll,maxScroll));
        for(int i=0;i<mods.size();i++){
            Module m=mods.get(i); int col=i%cols,row=i/cols-scroll;
            if(row<0||row>=visible)continue;
            int cx=gridX+col*(cardW+gap),cy=gridY+row*(cardH+gap);
            boolean hover=hit(mouseX,mouseY,cx,cy,cardW,cardH);
            d.fill(cx,cy,cx+cardW,cy+cardH,hover?0xFF242424:CARD);
            d.fill(cx,cy,cx+cardW,cy+1,BORDER);
            d.fill(cx,cy,cx+cardW,cy+3,m.enabled?ORANGE:BORDER);
            d.drawTextWithShadow(textRenderer,Text.literal(m.name),cx+12,cy+13,WHITE);
            d.drawCenteredTextWithShadow(textRenderer,Text.literal(icon(m.name)),cx+cardW/2,cy+39,0xFFE6E6E6);
            d.drawTextWithShadow(textRenderer,Text.literal("⚙"),cx+12,cy+84,MUTED);
            int bw=Math.min(94,Math.max(72,cardW/3)), bx=cx+cardW-bw-10, by=cy+78;
            d.fill(bx,by,bx+bw,by+24,m.enabled?0xFF164B20:0xFF303030);
            d.drawCenteredTextWithShadow(textRenderer,Text.literal(m.enabled?"Enabled":"Disabled"),bx+bw/2,by+8,m.enabled?GREEN:MUTED);
            if(hover)d.fill(cx,cy,cx+cardW,cy+1,ORANGE);
            if(settingsOpen&&selected==m){
                int px=Math.min(cx+8,width-190),py=Math.min(cy+54,height-120);
                d.fill(px,py,px+180,py+94,0xFF101010); d.fill(px,py,px+180,py+2,ORANGE);
                d.drawTextWithShadow(textRenderer,Text.literal(m.name+" SETTINGS"),px+9,py+10,ORANGE);
                d.drawTextWithShadow(textRenderer,Text.literal("Position: X / Y"),px+9,py+29,WHITE);
                d.drawTextWithShadow(textRenderer,Text.literal("Scale: 100%   [-] [+]"),px+9,py+46,MUTED);
                d.drawTextWithShadow(textRenderer,Text.literal("Background: ON"),px+9,py+64,MUTED);
            }
        }
        if(presetsOpen){
            int px=x+w-pad-154,py=tabsY+36;
            d.fill(px,py,px+154,py+84,0xFF141414); d.fill(px,py,px+154,py+2,ORANGE);
            d.drawTextWithShadow(textRenderer,Text.literal("PvP"),px+12,py+12,WHITE);
            d.drawTextWithShadow(textRenderer,Text.literal("Speedrun"),px+12,py+34,WHITE);
            d.drawTextWithShadow(textRenderer,Text.literal("Casual"),px+12,py+56,WHITE);
        }
        d.drawTextWithShadow(textRenderer,Text.literal("BARON CLIENT  •  RIGHT SHIFT TO CLOSE"),x+pad,y+h-15,MUTED);
        if(maxScroll>0){int sx=x+w-7,sy=gridY,sh=y+h-25-gridY;d.fill(sx,sy,sx+2,sy+sh,BORDER);int th=Math.max(18,sh*visible/totalRows);d.fill(sx,sy+(sh-th)*scroll/maxScroll,sx+2,sy+(sh-th)*scroll/maxScroll+th,ORANGE);}
    }
    private void button(DrawContext d,int x,int y,int w,int h,String label,boolean active,int mx,int my){
        boolean hover=hit(mx,my,x,y,w,h); d.fill(x,y,x+w,y+h,active?0xFF382817:hover?0xFF303030:CARD);
        d.drawCenteredTextWithShadow(textRenderer,Text.literal(label),x+w/2,y+(h-8)/2,active?ORANGE:WHITE);
    }
    @Override public boolean mouseClicked(Click c,boolean dbl){
        if(c.button()!=0)return super.mouseClicked(c,dbl);
        double mx=c.x(),my=c.y(); int x=left(),y=top(),w=panelW(),h=panelH(),pad=18;
        int headerY=y+16,tabsY=headerY+54;
        if(hit(mx,my,x+w-pad-40,headerY+8,30,26)){close();return true;}
        int rightW=310,available=w-pad*2-rightW-10,tabW=Math.max(38,available/6);
        for(int i=0;i<CATEGORIES.length;i++)if(hit(mx,my,x+pad+i*tabW,tabsY,tabW-5,32)){category=CATEGORIES[i];scroll=0;return true;}
        int searchW=Math.min(190,Math.max(100,rightW-130)),searchX=x+w-pad-searchW-112;
        if(hit(mx,my,searchX,tabsY,searchW,32)){searchFocused=true;return true;}
        if(hit(mx,my,x+w-pad-104,tabsY,48,32)){for(Module m:ModuleManager.all())m.enabled=false;ModuleManager.save();settingsOpen=false;presetsOpen=false;return true;}
        if(hit(mx,my,x+w-pad-50,tabsY,50,32)){presetsOpen=!presetsOpen;settingsOpen=false;return true;}
        if(presetsOpen){
            int px=x+w-pad-154,py=tabsY+36;
            if(hit(mx,my,px,py,154,28)){preset("PvP");}
            else if(hit(mx,my,px,py+28,154,28)){preset("Speedrun");}
            else if(hit(mx,my,px,py+56,154,28)){preset("Casual");}
            presetsOpen=false;return true;
        }
        int gridX=x+pad,gridY=tabsY+70,gridW=w-pad*2,gap=10,cols=4,cardW=(gridW-gap*3)/4,cardH=112;
        int visible=Math.max(1,(y+h-18-gridY)/(cardH+gap)); List<Module> mods=filtered();
        int maxScroll=Math.max(0,(mods.size()+cols-1)/cols-visible);scroll=Math.max(0,Math.min(scroll,maxScroll));
        for(int i=0;i<mods.size();i++){
            Module m=mods.get(i);int col=i%cols,row=i/cols-scroll;if(row<0||row>=visible)continue;
            int cx=gridX+col*(cardW+gap),cy=gridY+row*(cardH+gap);
            if(hit(mx,my,cx,cy,cardW,cardH)){
                if(hit(mx,my,cx+6,cy+76,38,30)){selected=m;settingsOpen=!(settingsOpen&&selected==m);}
                else{m.toggle();ModuleManager.save();selected=null;settingsOpen=false;}
                return true;
            }
        }
        settingsOpen=false;return true;
    }
    private void preset(String name){
        for(Module m:ModuleManager.all()){
            m.enabled=switch(name){
                case "PvP"->List.of("FPS Counter","Biome Display","Sprint","Armor Status","Potion HUD","Ping").contains(m.name);
                case "Speedrun"->List.of("FPS Counter","Coordinates","Direction","Clock","Speed","Sprint","NoFall").contains(m.name);
                default->List.of("FPS Counter","Biome Display","Coordinates","Clock","Memory Usage").contains(m.name);
            };
        }ModuleManager.save();
    }
    @Override public boolean keyPressed(KeyInput in){
        int k=in.key();
        if(k==GLFW.GLFW_KEY_ESCAPE||k==GLFW.GLFW_KEY_RIGHT_SHIFT){close();return true;}
        if(searchFocused){
            if(k==GLFW.GLFW_KEY_BACKSPACE&&!search.isEmpty())search=search.substring(0,search.length()-1);
            else if(k==GLFW.GLFW_KEY_ENTER)searchFocused=false;
            else if(k>=GLFW.GLFW_KEY_A&&k<=GLFW.GLFW_KEY_Z)search+=(char)('a'+k-GLFW.GLFW_KEY_A);
            else if(k>=GLFW.GLFW_KEY_0&&k<=GLFW.GLFW_KEY_9)search+=(char)('0'+k-GLFW.GLFW_KEY_0);
            else if(k==GLFW.GLFW_KEY_SPACE)search+=" ";
            else return super.keyPressed(in);
            scroll=0;return true;
        }
        return super.keyPressed(in);
    }
    @Override public boolean mouseScrolled(double mx,double my,double horizontal,double vertical){scroll=Math.max(0,Math.min(100,scroll-(int)Math.signum(vertical)));return true;}
    @Override public boolean shouldPause(){return false;}
}
