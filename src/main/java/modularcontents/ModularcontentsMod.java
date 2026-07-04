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

import modularcontents.custom.item.CustomItemManager;
import modularcontents.custom.item.CustomItemInfo;
import modularcontents.custom.item.ItemCustom;
import modularcontents.custom.client.ModularResourcePack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import java.util.List;
import java.io.File;

import modularcontents.custom.block.BlockAirdrop;
import modularcontents.custom.block.TileEntityAirdrop;
import modularcontents.custom.inventory.ContainerAirdrop;
import modularcontents.custom.gui.GuiAirdrop;

import modularcontents.custom.entity.EntityAirdrop;
import modularcontents.custom.entity.RenderAirdrop;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraft.util.ResourceLocation;

@Mod(modid = ModularcontentsMod.MODID, version = ModularcontentsMod.VERSION, name = "ModularContents")
@Mod.EventBusSubscriber
public class ModularcontentsMod implements IGuiHandler {

    public static final String MODID = "modularcontents";
    public static final String VERSION = "1.0.0";

    @Mod.Instance(MODID)
    public static ModularcontentsMod instance;

    public static final SimpleNetworkWrapper PACKET_HANDLER = NetworkRegistry.INSTANCE.newSimpleChannel("modularcontents:main");

    public static final net.minecraft.creativetab.CreativeTabs MODULAR_TAB = new net.minecraft.creativetab.CreativeTabs("modular_contents") {
        @Override
        public ItemStack getTabIconItem() {
            return new ItemStack(custom_workbench_item);
        }
    };

    public static Block custom_workbench = new BlockListWorkbench().setRegistryName("custom_workbench").setUnlocalizedName("custom_workbench");
    public static Block custom_workbench_part = new modularcontents.custom.block.BlockListWorkbenchPart(custom_workbench).setRegistryName("custom_workbench_part").setUnlocalizedName("custom_workbench_part");
    public static Item custom_workbench_item = new modularcontents.custom.block.ItemBlockListWorkbench(custom_workbench).setRegistryName(custom_workbench.getRegistryName());

    public static Block airdrop = new BlockAirdrop();
    public static Item airdrop_item = new ItemBlock(airdrop).setRegistryName(airdrop.getRegistryName());

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Load custom config
        ModularContentsConfig.load(event.getModConfigurationDirectory().getParentFile());

        EntityRegistry.registerModEntity(new ResourceLocation(MODID, "airdrop"), EntityAirdrop.class, "Airdrop", 1, instance, 64, 1, true);

        if (event.getSide() == Side.CLIENT) {
            RenderingRegistry.registerEntityRenderingHandler(EntityAirdrop.class, RenderAirdrop::new);
        }

        // Setup directories for custom recipes
        ListWorkbenchRecipeManager.setupDirectories(event.getModConfigurationDirectory().getParentFile());

        // Load custom items JSON definitions BEFORE item registration
        CustomItemManager.loadItems(event.getModConfigurationDirectory().getParentFile());

        if (event.getSide() == Side.CLIENT) {
            injectCustomResourcePack(event.getModConfigurationDirectory().getParentFile());
        }

        // Register GUI handler
        NetworkRegistry.INSTANCE.registerGuiHandler(this, this);

        // Register Packets
        int packetId = 0;
        PACKET_HANDLER.registerMessage(PacketCraftStartHandler.class, PacketCraftStart.class, packetId++, Side.SERVER);
        PACKET_HANDLER.registerMessage(PacketCraftCancelHandler.class, PacketCraftCancel.class, packetId++, Side.SERVER);
    }

    @SideOnly(Side.CLIENT)
    private void injectCustomResourcePack(File gameDir) {
        try {
            ModularResourcePack pack = new ModularResourcePack(gameDir);
            List<IResourcePack> defaultPacks = ReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), "defaultResourcePacks", "field_110449_ao");
            defaultPacks.add(pack);

            net.minecraft.client.resources.IResourceManager manager = Minecraft.getMinecraft().getResourceManager();
            if (manager instanceof net.minecraft.client.resources.SimpleReloadableResourceManager) {
                java.util.Map<String, net.minecraft.client.resources.FallbackResourceManager> domainManagers = ReflectionHelper.getPrivateValue(net.minecraft.client.resources.SimpleReloadableResourceManager.class, (net.minecraft.client.resources.SimpleReloadableResourceManager) manager, "domainResourceManagers", "field_110548_a");
                net.minecraft.client.resources.FallbackResourceManager fallback = domainManagers.get("modularcontents");
                if (fallback != null) {
                    fallback.addResourcePack(pack);
                } else {
                    fallback = new net.minecraft.client.resources.FallbackResourceManager(new net.minecraft.client.resources.data.MetadataSerializer());
                    fallback.addResourcePack(pack);
                    domainManagers.put("modularcontents", fallback);
                }
            }

            System.out.println("[ModularContents] Successfully injected ModularResourcePack for dynamic textures!");
        } catch (Exception e) {
            System.out.println("[ModularContents] Failed to inject dynamic resource pack: " + e.getMessage());
            e.printStackTrace();
        }
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
        event.getRegistry().register(custom_workbench_part);
        GameRegistry.registerTileEntity(TileEntityListWorkbench.class, "modularcontents:tile_list_workbench");

        event.getRegistry().register(airdrop);
        GameRegistry.registerTileEntity(TileEntityAirdrop.class, "modularcontents:tile_airdrop");
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(custom_workbench_item);
        event.getRegistry().register(airdrop_item);

        for (CustomItemInfo info : CustomItemManager.CUSTOM_ITEMS.values()) {
            Item item = new ItemCustom(info);
            event.getRegistry().register(item);
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(custom_workbench_item, 0, new ModelResourceLocation(custom_workbench_item.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(airdrop_item, 0, new ModelResourceLocation(airdrop_item.getRegistryName(), "inventory"));

        for (CustomItemInfo info : CustomItemManager.CUSTOM_ITEMS.values()) {
            Item item = Item.getByNameOrId("modularcontents:" + info.id);
            if (item != null) {
                ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
            } else {
                System.out.println("[ModularContents] WARNING: Item.getByNameOrId returned null for " + info.id);
            }
        }
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
        if (id == 2) {
            TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
            if (te instanceof TileEntityAirdrop) {
                return new ContainerAirdrop(player.inventory, (TileEntityAirdrop) te);
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
        if (id == 2) {
            TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
            if (te instanceof TileEntityAirdrop) {
                return new GuiAirdrop(player.inventory, (TileEntityAirdrop) te);
            }
        }
        return null;
    }
}
