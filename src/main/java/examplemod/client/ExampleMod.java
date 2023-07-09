package examplemod.client;

import org.lwjgl.input.Keyboard;

import com.labymedia.ultralight.UltralightRenderer;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import examplemod.client.ultralight.HTMLGui;
import examplemod.client.ultralight.UltraLight;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;


@Mod(
        modid = ExampleMod.MODID,
        name = "Example Mod",
        version = ExampleMod.VERSION,
        acceptedMinecraftVersions = "[1.7.10]"
)
public class ExampleMod {
   public static final String MODID = "examplemod";
   public static final String VERSION = "1";


   public HTMLGui HTMLGui;
   private UltralightRenderer renderer;

   @Mod.EventHandler
   public void init(FMLInitializationEvent event) {
      MinecraftForge.EVENT_BUS.register(this);
      UltraLight.init();
      renderer = UltraLight.getRenderer();


      HTMLGui = new HTMLGui(renderer, "file://ultralight/public/index.html", 1920, 1080);



   }


   @SubscribeEvent
   public void onTick(ClientTickEvent event) {
      renderer.update();
      renderer.render();

      if (isPlayerInGame()) {
         //right shift
         if (Keyboard.isKeyDown(54) && Minecraft.getMinecraft().currentScreen == null) {
            Minecraft.getMinecraft().thePlayer.playSound(MODID + ":sound.enable", 10, 1);
            Minecraft.getMinecraft().displayGuiScreen(HTMLGui);
         }

      }
   }
   public static boolean isPlayerInGame() {
      Minecraft mc = Minecraft.getMinecraft();
      return mc.thePlayer != null && mc.theWorld != null;
   }

}