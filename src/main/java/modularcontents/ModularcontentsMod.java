package modularcontents;

import modularcontents.custom.block.BlockListWorkbench;
import modularcontents.custom.block.TileEntityListWorkbench;
import modularcontents.custom.gui.GuiListWorkbench;
import modularcontents.custom.inventory.ContainerListWorkbench;
import modularcontents.custom.network.PacketCraftCancel;
import modularcontents.custom.network.PacketCraftCancelHandler;
import modularcontents.custom.network.PacketCraftStart;
import modularcontents.custom.network.PacketCraftStartHandler;
import modularcontents.custom.recipe.ListWorkbenchRecipeManager;
import modularcontents.custom.config.ModularContentsConfig;
import modularcontents.custom.command.CommandModularContents;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.model.ModelLoader;

@Mod(modid = ModularcontentsMod.MODID, version = ModularcontentsMod.VERSION, name = "ModularContents")
@Mod.EventBusSubscriber
public class ModularcontentsMod implements IGuiHandler {

    public static final String MODID = "modularcontents";
    public static final String VERSION = "1.0.0";

    @Mod.Instance(MODID)
    public static ModularcontentsMod instance;

    public static final SimpleNetworkWrapper PACKET_HANDLER = NetworkRegistry.INSTANCE.newSimpleChannel("modularcontents:main");

    public static Block custom_workbench = new BlockListWorkbench().setRegistryName("custom_workbench").setUnlocalizedName("custom_workbench");
    public static Item custom_workbench_item = new ItemBlock(custom_workbench).setRegistryName(custom_workbench.getRegistryName());

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Load custom config
        ModularContentsConfig.load(event.getModConfigurationDirectory().getParentFile());

        // Setup directories for custom recipes
        ListWorkbenchRecipeManager.setupDirectories(event.getModConfigurationDirectory().getParentFile());

        // Register GUI handler
        NetworkRegistry.INSTANCE.registerGuiHandler(this, this);

        // Register Packets
        int packetId = 0;
        PACKET_HANDLER.registerMessage(PacketCraftStartHandler.class, PacketCraftStart.class, packetId++, Side.SERVER);
        PACKET_HANDLER.registerMessage(PacketCraftCancelHandler.class, PacketCraftCancel.class, packetId++, Side.SERVER);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // Init logic if needed
    }

    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        // Load JSON recipes when server starts
        ListWorkbenchRecipeManager.loadRecipes(event.getServer().getDataDirectory());

        // Register in-game commands
        event.registerServerCommand(new CommandModularContents());
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(custom_workbench);
        GameRegistry.registerTileEntity(TileEntityListWorkbench.class, "modularcontents:tile_list_workbench");
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(custom_workbench_item);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(custom_workbench_item, 0, new ModelResourceLocation(custom_workbench_item.getRegistryName(), "inventory"));
    }

    // --- IGuiHandler Implementation ---

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id == 1) {
            TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
            if (te instanceof TileEntityListWorkbench) {
                return new ContainerListWorkbench(player.inventory, (TileEntityListWorkbench) te);
            }
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id == 1) {
            TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
            if (te instanceof TileEntityListWorkbench) {
                return new GuiListWorkbench(player.inventory, (TileEntityListWorkbench) te);
            }
        }
        return null;
    }
}
