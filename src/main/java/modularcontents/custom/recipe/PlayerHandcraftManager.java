package modularcontents.custom.recipe;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class PlayerHandcraftManager {
    private static final UUID SLOWNESS_MODIFIER_UUID = UUID.fromString("B9766B59-9566-4402-BC1F-2EE2A276D836");

    public static class CraftingState {
        public String recipeId;
        public int amount;
        public int progress;
        public int totalTime;
        public float movementModifier;
    }

    private static final Map<UUID, CraftingState> activeCrafts = new HashMap<>();

    public static CraftingState getState(UUID playerId) {
        return activeCrafts.get(playerId);
    }

    private static boolean canFitInInventory(EntityPlayerMP player, ListWorkbenchRecipe recipe) {
        if (player.inventory.getFirstEmptyStack() != -1) {
            return true;
        }
        for (IngredientStack out : recipe.outputs) {
            if (out == null || out.chance < 100.0f) continue;
            ItemStack res = out.toItemStack();
            if (res.isEmpty()) continue;
            boolean fits = false;
            for (ItemStack invStack : player.inventory.mainInventory) {
                if (!invStack.isEmpty() && invStack.isItemEqual(res) && ItemStack.areItemStackTagsEqual(invStack, res)) {
                    if (invStack.getCount() + res.getCount() <= invStack.getMaxStackSize()) {
                        fits = true;
                        break;
                    }
                }
            }
            if (!fits) return false;
        }
        return true;
    }

    public static void startCrafting(EntityPlayerMP player, String recipeId, int amount) {
        ListWorkbenchRecipe recipe = ListWorkbenchRecipeManager.getRecipe(recipeId);
        if (recipe != null && amount > 0) {
            if (!canFitInInventory(player, recipe)) return;
            CraftingState state = new CraftingState();
            state.recipeId = recipeId;
            state.amount = amount;
            state.progress = 0;
            state.totalTime = recipe.craftingTime;
            state.movementModifier = recipe.movementModifier;
            activeCrafts.put(player.getUniqueID(), state);
            applySlowness(player, state.movementModifier);
        }
    }

    public static void cancelCrafting(EntityPlayerMP player) {
        activeCrafts.remove(player.getUniqueID());
        removeSlowness(player);
    }

    private static void applySlowness(EntityPlayerMP player, float modifierMultiplier) {
        IAttributeInstance attr = player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        if (attr != null) {
            attr.removeModifier(SLOWNESS_MODIFIER_UUID);
            if (modifierMultiplier < 1.0f) {
                double val = Math.max(0.0, modifierMultiplier) - 1.0;
                if (val < 0) {
                    attr.applyModifier(new AttributeModifier(SLOWNESS_MODIFIER_UUID, "Handcrafting slowness", val, 2));
                }
            }
        }
    }

    private static void removeSlowness(EntityPlayerMP player) {
        IAttributeInstance attr = player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        if (attr != null) {
            attr.removeModifier(SLOWNESS_MODIFIER_UUID);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.player;
            CraftingState state = activeCrafts.get(player.getUniqueID());
            
            if (state != null) {
                ListWorkbenchRecipe recipe = ListWorkbenchRecipeManager.getRecipe(state.recipeId);
                if (recipe == null || state.amount <= 0) {
                    cancelCrafting(player);
                    return;
                }

                if (state.progress == 0) {
                    applySlowness(player, state.movementModifier);
                }

                state.progress++;

                if (state.progress >= state.totalTime) {
                    finishOneCraft(player, state, recipe);
                }
            } else {
                removeSlowness(player); // Fallback in case of stray modifier
            }
        }
    }

    private static void finishOneCraft(EntityPlayerMP player, CraftingState state, ListWorkbenchRecipe recipe) {
        if (!canFitInInventory(player, recipe)) {
            cancelCrafting(player);
            return;
        }

        boolean hasIngredients = true;
        for (IngredientStack ing : recipe.inputs) {
            if (countItemInInventory(player, ing) < ing.count) {
                hasIngredients = false;
                break;
            }
        }

        if (hasIngredients) {
            for (IngredientStack ing : recipe.inputs) {
                consumeItemFromInventory(player, ing, ing.count);
            }

            List<ItemStack> results = recipe.rollResults(player.world.rand);
            for (ItemStack res : results) {
                if (!res.isEmpty()) {
                    if (!player.inventory.addItemStackToInventory(res.copy())) {
                        player.dropItem(res.copy(), false);
                    }
                }
            }

            state.amount--;
            if (state.amount > 0) {
                state.progress = 0;
            } else {
                cancelCrafting(player);
            }
        } else {
            cancelCrafting(player);
        }
    }

    private static int countItemInInventory(EntityPlayerMP player, IngredientStack requiredIng) {
        int count = 0;
        for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
            ItemStack stack = player.inventory.mainInventory.get(i);
            if (requiredIng.matches(stack)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static void consumeItemFromInventory(EntityPlayerMP player, IngredientStack ing, int amount) {
        int left = amount;
        for (int i = 0; i < player.inventory.mainInventory.size() && left > 0; i++) {
            ItemStack stack = player.inventory.mainInventory.get(i);
            if (ing.matches(stack)) {
                int take = Math.min(left, stack.getCount());
                stack.shrink(take);
                left -= take;
                if (stack.isEmpty()) {
                    player.inventory.mainInventory.set(i, ItemStack.EMPTY);
                }
            }
        }
    }
}
