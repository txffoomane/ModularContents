package modularcontents.custom.inventory;

<<<<<<< HEAD
import modularcontents.custom.recipe.ListWorkbenchRecipeManager;
import net.minecraft.entity.player.EntityPlayer;
=======
import modularcontents.ModularcontentsMod;
import modularcontents.custom.network.PacketHandcraftSync;
import modularcontents.custom.recipe.PlayerHandcraftManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
>>>>>>> 842bc66 (Feat: Custom Workbenches, Handcrafting, and Folder-based Recipe separation)
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
<<<<<<< HEAD

public class ContainerHandcraft extends Container {
    private final InventoryPlayer playerInventory;

    public ContainerHandcraft(InventoryPlayer playerInventory) {
        this.playerInventory = playerInventory;
=======
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerHandcraft extends Container {
    private final InventoryPlayer playerInventory;
    private final EntityPlayer player;

    public String activeRecipeId = "";
    public int craftAmount = 0;
    public int progress = 0;
    public int totalTime = 0;

    @SideOnly(Side.CLIENT)
    public int clientProgress;
    @SideOnly(Side.CLIENT)
    public int clientTotalTime;
    @SideOnly(Side.CLIENT)
    public int clientCraftAmount;

    public ContainerHandcraft(InventoryPlayer playerInventory) {
        this.playerInventory = playerInventory;
        this.player = playerInventory.player;
>>>>>>> 842bc66 (Feat: Custom Workbenches, Handcrafting, and Folder-based Recipe separation)

        // Player Inventory
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
<<<<<<< HEAD
                this.addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 142 + i * 18));
=======
                this.addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, 88 + j * 18, 158 + i * 18));
>>>>>>> 842bc66 (Feat: Custom Workbenches, Handcrafting, and Folder-based Recipe separation)
            }
        }
        // Player Hotbar
        for (int k = 0; k < 9; ++k) {
<<<<<<< HEAD
            this.addSlotToContainer(new Slot(playerInventory, k, 8 + k * 18, 200));
=======
            this.addSlotToContainer(new Slot(playerInventory, k, 88 + k * 18, 216));
        }
    }

    public void startCrafting(String recipeId, int amount) {
        if (player instanceof EntityPlayerMP) {
            PlayerHandcraftManager.startCrafting((EntityPlayerMP) player, recipeId, amount);
            detectAndSendChanges();
        }
    }

    public void cancelCrafting() {
        if (player instanceof EntityPlayerMP) {
            PlayerHandcraftManager.cancelCrafting((EntityPlayerMP) player);
            detectAndSendChanges();
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        if (player instanceof EntityPlayerMP) {
            EntityPlayerMP emp = (EntityPlayerMP) player;
            PlayerHandcraftManager.CraftingState state = PlayerHandcraftManager.getState(emp.getUniqueID());
            
            boolean changed = false;
            if (state != null) {
                if (!activeRecipeId.equals(state.recipeId) || craftAmount != state.amount || progress != state.progress || totalTime != state.totalTime) {
                    this.activeRecipeId = state.recipeId;
                    this.craftAmount = state.amount;
                    this.progress = state.progress;
                    this.totalTime = state.totalTime;
                    changed = true;
                }
            } else {
                if (!activeRecipeId.isEmpty()) {
                    this.activeRecipeId = "";
                    this.craftAmount = 0;
                    this.progress = 0;
                    this.totalTime = 0;
                    changed = true;
                }
            }

            if (changed) {
                ModularcontentsMod.PACKET_HANDLER.sendTo(
                    new PacketHandcraftSync(activeRecipeId, craftAmount, progress, totalTime), 
                    emp
                );
            }
>>>>>>> 842bc66 (Feat: Custom Workbenches, Handcrafting, and Folder-based Recipe separation)
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
<<<<<<< HEAD
        return ItemStack.EMPTY; // Add shift-click logic later if needed
=======
        return ItemStack.EMPTY; 
>>>>>>> 842bc66 (Feat: Custom Workbenches, Handcrafting, and Folder-based Recipe separation)
    }
}
