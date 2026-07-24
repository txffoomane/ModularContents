import re

with open("src/main/java/modularcontents/custom/block/BlockCustomSlab.java", "r", encoding="utf-8") as f:
    content = f.read()

replacement = """
    @Override
    public void onBlockAdded(World worldIn, net.minecraft.util.math.BlockPos pos, IBlockState state) {
        super.onBlockAdded(worldIn, pos, state);
        if (this.isDouble) {
            String parentId = info.id;
            if (parentId.endsWith("_slab")) {
                parentId = parentId.substring(0, parentId.length() - 5);
            }
            net.minecraft.block.Block parent = net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS.getValue(new net.minecraft.util.ResourceLocation("modularcontents", parentId));
            if (parent != null && parent != net.minecraft.init.Blocks.AIR) {
                worldIn.setBlockState(pos, parent.getDefaultState(), 3);
            }
        }
    }

    @Override
"""

content = content.replace("    @Override\n    public String getUnlocalizedName", replacement + "    public String getUnlocalizedName")

with open("src/main/java/modularcontents/custom/block/BlockCustomSlab.java", "w", encoding="utf-8") as f:
    f.write(content)
