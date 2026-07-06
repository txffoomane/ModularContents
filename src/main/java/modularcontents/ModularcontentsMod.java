package modularcontents;

import modularcontents.custom.block.BlockListWorkbench;
import modularcontents.custom.block.BlockListWorkbenchPart;
import modularcontents.custom.block.ItemBlockListWorkbench;
import modularcontents.custom.block.TileEntityListWorkbench;
import modularcontents.custom.gui.GuiContentCreator;
import modularcontents.custom.gui.GuiListWorkbench;
import modularcontents.custom.inventory.ContainerContentCreator;
import modularcontents.custom.inventory.ContainerListWorkbench;
import modularcontents.custom.item.ItemRadio;
import modularcontents.custom.item.ItemSignalFlare;
import modularcontents.custom.loot.AirdropLootManager;
import modularcontents.custom.loot.EquipmentManager;
import modularcontents.custom.pack.PackZipUtils;
import modularcontents.custom.tab.CustomTabManager;
import modularcontents.proxy.CommonProxy;
import modularcontents.custom.zone.LootZoneHandler;
import modularcontents.custom.network.PacketCraftCancel;
import modularcontents.custom.network.PacketCraftCancelHandler;
import modularcontents.custom.network.PacketCraftStart;
import modularcontents.custom.network.PacketCraftStartHandler;
import modularcontents.custom.network.PacketSyncContent;
import modularcontents.custom.network.PacketSyncContentHandler;
import modularcontents.custom.recipe.ListWorkbenchRecipeManager;
import modularcontents.custom.config.ModularContentsConfig;
import modularcontents.custom.command.CommandModularContents;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.SoundEvent;
import net.minecraft.client.resources.FallbackResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.model.ModelLoader;

import modularcontents.custom.item.CustomContentManager;
import modularcontents.custom.item.CustomBlockInfo;
import modularcontents.custom.item.CustomFoodInfo;
import modularcontents.custom.item.ItemCustomFood;
import modularcontents.custom.block.BlockCustom;
import net.minecraft.item.ItemBlock;
import net.minecraft.block.Block;
import modularcontents.custom.item.CustomItemInfo;
import modularcontents.custom.item.ItemCustom;
import modularcontents.custom.client.ModularResourcePack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import java.util.List;
import java.io.File;
import java.util.Map;

import modularcontents.custom.block.BlockAirdrop;
import modularcontents.custom.block.TileEntityAirdrop;
import modularcontents.custom.inventory.ContainerAirdrop;
import modularcontents.custom.gui.GuiAirdrop;

import modularcontents.custom.entity.EntityAirdrop;
import modularcontents.custom.entity.EntitySignalFlare;
import modularcontents.custom.npc.EntityCustomNPC;
import modularcontents.custom.npc.EntityNPCBullet;
import modularcontents.custom.npc.NPCManager;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraft.util.ResourceLocation;
import modularcontents.custom.block.BlockLaptop;
import modularcontents.custom.network.PacketLaptopAirdrop;
import modularcontents.custom.network.PacketLaptopAirdropHandler;
import modularcontents.custom.gui.GuiLaptop;
import modularcontents.custom.event.GlobalAirdropHandler;
import modularcontents.custom.network.PacketOpenCreator;
import modularcontents.custom.network.PacketOpenCreatorHandler;
import modularcontents.custom.network.PacketZoneLoot;
import modularcontents.custom.network.PacketZoneLootHandler;
import modularcontents.custom.network.PacketSyncZones;
import modularcontents.custom.network.PacketSyncZonesHandler;
import modularcontents.custom.network.PacketRequestZones;
import modularcontents.custom.network.PacketRequestZonesHandler;
import modularcontents.custom.network.PacketRequestPackList;
import modularcontents.custom.network.PacketRequestPackListHandler;
import modularcontents.custom.network.PacketSendPackList;
import modularcontents.custom.network.PacketSendPackListHandler;
import modularcontents.custom.network.PacketRequestFileContent;
import modularcontents.custom.network.PacketRequestFileContentHandler;
import modularcontents.custom.network.PacketSendFileContent;
import modularcontents.custom.network.PacketSendFileContentHandler;
import modularcontents.custom.network.PacketSaveContent;
import modularcontents.custom.network.PacketSaveContentHandler;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.ShapedOreRecipe;
import java.util.ArrayList;

@Mod(modid = ModularcontentsMod.MODID, version = ModularcontentsMod.VERSION, name = "ModularContents")
@Mod.EventBusSubscriber
public class ModularcontentsMod implements IGuiHandler {
    public static final String MODID = "modularcontents";
    public static final String VERSION = "0.3";

    @Mod.Instance(MODID)
    public static ModularcontentsMod instance;

    @SidedProxy(clientSide = "modularcontents.proxy.ClientProxy", serverSide = "modularcontents.proxy.CommonProxy")
    public static CommonProxy proxy;

    public static final SimpleNetworkWrapper PACKET_HANDLER = NetworkRegistry.INSTANCE.newSimpleChannel("modularcontents:main");

    public static final CreativeTabs MODULAR_TAB = new CreativeTabs("modular_contents") {
        @Override
        public ItemStack getTabIconItem() {
            return new ItemStack(custom_workbench_item);
        }
    };

    public static Block custom_workbench = new BlockListWorkbench().setRegistryName("custom_workbench").setUnlocalizedName("custom_workbench");
    public static Block custom_workbench_part = new BlockListWorkbenchPart(custom_workbench).setRegistryName("custom_workbench_part").setUnlocalizedName("custom_workbench_part");
    public static Item custom_workbench_item = new ItemBlockListWorkbench(custom_workbench).setRegistryName(custom_workbench.getRegistryName());

    public static Block airdrop = new BlockAirdrop();
    public static Item airdrop_item = new ItemBlock(airdrop).setRegistryName(airdrop.getRegistryName());
    public static Block laptop = new BlockLaptop();
    public static Item laptop_item = new ItemBlock(laptop).setRegistryName(laptop.getRegistryName());
    public static Item signal_flare = new ItemSignalFlare();
    public static Item radio = new ItemRadio();

    public static final SoundEvent AIRDROP_SMOKE = new SoundEvent(new ResourceLocation(MODID, "airdrop_smoke")).setRegistryName(MODID, "airdrop_smoke");

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Load custom config
        ModularContentsConfig.load(event.getModConfigurationDirectory().getParentFile());

        MinecraftForge.EVENT_BUS.register(GlobalAirdropHandler.class);
        MinecraftForge.EVENT_BUS.register(new LootZoneHandler());
        MinecraftForge.EVENT_BUS.register(new LootZoneHandler());

        EntityRegistry.registerModEntity(new ResourceLocation(MODID, "airdrop"), EntityAirdrop.class, "Airdrop", 1, instance, 64, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(MODID, "signal_flare"), EntitySignalFlare.class, "SignalFlare", 2, instance, 64, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(MODID, "custom_npc"), EntityCustomNPC.class, "CustomNPC", 3, instance, 64, 3, true);
        EntityRegistry.registerModEntity(new ResourceLocation(MODID, "npc_bullet"), EntityNPCBullet.class, "NPCBullet", 4, instance, 64, 20, true);

        // Load NPC definitions
        NPCManager.loadNPCs(event.getModConfigurationDirectory().getParentFile());

        // Setup directories for custom recipes
        ListWorkbenchRecipeManager.setupDirectories(event.getModConfigurationDirectory().getParentFile());

        // Load custom tabs and content JSON definitions BEFORE item registration
        CustomTabManager.loadTabs(event.getModConfigurationDirectory().getParentFile());
        CustomContentManager.loadContent(event.getModConfigurationDirectory().getParentFile());

        proxy.preInit(event);

        // Register GUI handler
        NetworkRegistry.INSTANCE.registerGuiHandler(this, this);

        // Register Packets
        int packetId = 0;
        PACKET_HANDLER.registerMessage(PacketCraftStartHandler.class, PacketCraftStart.class, packetId++, Side.SERVER);
        PACKET_HANDLER.registerMessage(PacketCraftCancelHandler.class, PacketCraftCancel.class, packetId++, Side.SERVER);
        PACKET_HANDLER.registerMessage(PacketLaptopAirdropHandler.class, PacketLaptopAirdrop.class, packetId++, Side.SERVER);
        PACKET_HANDLER.registerMessage(PacketOpenCreatorHandler.class, PacketOpenCreator.class, packetId++, Side.SERVER);
        PACKET_HANDLER.registerMessage(PacketSyncContentHandler.class, PacketSyncContent.class, packetId++, Side.CLIENT);
        PACKET_HANDLER.registerMessage(PacketZoneLootHandler.class, PacketZoneLoot.class, packetId++, Side.SERVER);
        PACKET_HANDLER.registerMessage(PacketSyncZonesHandler.class, PacketSyncZones.class, packetId++, Side.CLIENT);
        PACKET_HANDLER.registerMessage(PacketRequestZonesHandler.class, PacketRequestZones.class, packetId++, Side.SERVER);
        PACKET_HANDLER.registerMessage(PacketRequestPackListHandler.class, PacketRequestPackList.class, packetId++, Side.SERVER);
        PACKET_HANDLER.registerMessage(PacketSendPackListHandler.class, PacketSendPackList.class, packetId++, Side.CLIENT);
        PACKET_HANDLER.registerMessage(PacketRequestFileContentHandler.class, PacketRequestFileContent.class, packetId++, Side.SERVER);
        PACKET_HANDLER.registerMessage(PacketSendFileContentHandler.class, PacketSendFileContent.class, packetId++, Side.CLIENT);
        PACKET_HANDLER.registerMessage(PacketSaveContentHandler.class, PacketSaveContent.class, packetId++, Side.SERVER);
    }

    public static PacketSyncContent buildContentSyncPacket() {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        String requiredPacksJson = server != null
                ? PackZipUtils.getClientRequiredPacksJson(server.getDataDirectory())
                : "[]";
        return new PacketSyncContent(ListWorkbenchRecipeManager.toSyncJson(), CustomTabManager.toSyncJson(), requiredPacksJson, EquipmentManager.toSyncJson());
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            PACKET_HANDLER.sendTo(buildContentSyncPacket(), (EntityPlayerMP) event.player);
        }
    }

    @SubscribeEvent
    public static void onConfigChanged(net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(MODID)) {
            ModularContentsConfig.syncConfig();
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
        AirdropLootManager.loadLootTables(event.getServer().getDataDirectory());
        EquipmentManager.loadEquipment(event.getServer().getDataDirectory());

        // Register in-game commands
        event.registerServerCommand(new CommandModularContents());
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(custom_workbench);
        event.getRegistry().register(custom_workbench_part);
        event.getRegistry().register(airdrop);
        event.getRegistry().register(laptop);
        GameRegistry.registerTileEntity(TileEntityListWorkbench.class, "modularcontents:tile_list_workbench");
        GameRegistry.registerTileEntity(TileEntityAirdrop.class, "modularcontents:tile_airdrop");

        // Register JSON Blocks
        for (CustomBlockInfo info : CustomContentManager.CUSTOM_BLOCKS.values()) {
            BlockCustom block = new BlockCustom(info);
            if (info.creativeTab != null && !info.creativeTab.isEmpty()) {
                if (CustomTabManager.CUSTOM_TABS.containsKey(info.creativeTab)) {
                    block.setCreativeTab(CustomTabManager.CUSTOM_TABS.get(info.creativeTab));
                } else {
                    for (net.minecraft.creativetab.CreativeTabs tab : net.minecraft.creativetab.CreativeTabs.CREATIVE_TAB_ARRAY) {
                        if (tab.getTabLabel().equalsIgnoreCase(info.creativeTab)) {
                            block.setCreativeTab(tab);
                            break;
                        }
                    }
                }
            }
            event.getRegistry().register(block);
        }
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().register(AIRDROP_SMOKE);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(custom_workbench_item);
        event.getRegistry().register(airdrop_item);
        event.getRegistry().register(laptop_item);
        event.getRegistry().register(signal_flare);
        event.getRegistry().register(radio);

        // Register Basic Items
        for (modularcontents.custom.item.CustomItemInfo info : CustomContentManager.CUSTOM_ITEMS.values()) {
            Item item = new modularcontents.custom.item.ItemCustom(info);
            event.getRegistry().register(item);
        }

        // Register Food
        for (CustomFoodInfo info : CustomContentManager.CUSTOM_FOODS.values()) {
            Item item = new ItemCustomFood(info);
            // Setup creative tab logic similar to items
            if (info.creativeTab != null && !info.creativeTab.isEmpty()) {
                if (CustomTabManager.CUSTOM_TABS.containsKey(info.creativeTab)) {
                    item.setCreativeTab(CustomTabManager.CUSTOM_TABS.get(info.creativeTab));
                } else {
                    for (net.minecraft.creativetab.CreativeTabs tab : net.minecraft.creativetab.CreativeTabs.CREATIVE_TAB_ARRAY) {
                        if (tab.getTabLabel().equalsIgnoreCase(info.creativeTab)) {
                            item.setCreativeTab(tab);
                            break;
                        }
                    }
                }
            }
            event.getRegistry().register(item);
        }

        // Register ItemBlocks for the Blocks we added
        for (CustomBlockInfo info : CustomContentManager.CUSTOM_BLOCKS.values()) {
            Block block = ForgeRegistries.BLOCKS.getValue(new net.minecraft.util.ResourceLocation("modularcontents", info.id));
            if (block != null) {
                ItemBlock itemBlock = new ItemBlock(block);
                itemBlock.setRegistryName(block.getRegistryName());
                event.getRegistry().register(itemBlock);
            }
        }
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        try {
            ArrayList<Object> params = new ArrayList<>();
            for (String row : ModularContentsConfig.workbenchRecipeShape) {
                params.add(row);
            }
            for (String keyMap : ModularContentsConfig.workbenchRecipeKeys) {
                String[] parts = keyMap.split(":", 3);
                if (parts.length >= 3) {
                    char c = parts[0].charAt(0);
                    params.add(c);
                    if (parts[1].equalsIgnoreCase("ore")) {
                        params.add(parts[2]);
                    } else {
                        Item item = Item.getByNameOrId(parts[1] + ":" + parts[2]);
                        if (item != null) {
                            params.add(new ItemStack(item));
                        } else {
                            Block block = Block.getBlockFromName(parts[1] + ":" + parts[2]);
                            if (block != null) {
                                params.add(new ItemStack(block));
                            }
                        }
                    }
                }
            }

            ShapedOreRecipe recipe = new ShapedOreRecipe(new ResourceLocation(MODID, "custom_workbench"), new ItemStack(custom_workbench_item), params.toArray());
            recipe.setRegistryName(new ResourceLocation(MODID, "custom_workbench"));
            event.getRegistry().register(recipe);
        } catch (Exception e) {
            System.err.println("[ModularContents] Failed to register custom workbench recipe from config: " + e.getMessage());
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(custom_workbench_item, 0, new ModelResourceLocation(custom_workbench_item.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(airdrop_item, 0, new ModelResourceLocation(airdrop_item.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(laptop_item, 0, new ModelResourceLocation(laptop_item.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(signal_flare, 0, new ModelResourceLocation(signal_flare.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(radio, 0, new ModelResourceLocation(radio.getRegistryName(), "inventory"));

        for (modularcontents.custom.item.CustomItemInfo info : CustomContentManager.CUSTOM_ITEMS.values()) {
            Item item = Item.getByNameOrId("modularcontents:" + info.id);
            if (item != null) ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }
        for (CustomFoodInfo info : CustomContentManager.CUSTOM_FOODS.values()) {
            Item item = Item.getByNameOrId("modularcontents:" + info.id);
            if (item != null) ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }
        for (CustomBlockInfo info : CustomContentManager.CUSTOM_BLOCKS.values()) {
            Item item = Item.getByNameOrId("modularcontents:" + info.id);
            if (item != null) ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
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
        if (id == 4) {
            return new ContainerContentCreator(player.inventory);
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
        if (id == 3) {
            return new GuiLaptop(world, new BlockPos(x, y, z));
        }
        if (id == 4) {
            return new GuiContentCreator(player.inventory);
        }
        return null;
    }
}
