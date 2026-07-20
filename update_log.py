with open('src/main/java/modularcontents/custom/block/BlockCustomLog.java', 'r') as f:
    code = f.read()

code = code.replace('public static final PropertyEnum<EnumFacing.Axis> AXIS = PropertyEnum.create("axis", EnumFacing.Axis.class);', 'public static final PropertyEnum<net.minecraft.block.BlockLog.EnumAxis> AXIS = net.minecraft.block.BlockLog.LOG_AXIS;')
code = code.replace('EnumFacing.Axis', 'net.minecraft.block.BlockLog.EnumAxis')
code = code.replace('facing.getAxis()', 'net.minecraft.block.BlockLog.EnumAxis.fromFacingAxis(facing.getAxis())')

with open('src/main/java/modularcontents/custom/block/BlockCustomLog.java', 'w') as f:
    f.write(code)
