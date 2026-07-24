## Context
The user wants to enhance the `CreativeTabs` feature by allowing custom tab JSON files to specify a list of items (including from other mods) that should be added to that tab. They also requested a comprehensive example pack with a Zombie Apocalypse theme (food, items, tools, weapons, blocks, recipes, workbenches).

## Implementation Plan

1. **Update `CustomTabInfo.java`:**
   - Add a `List<String> items` property to hold the IDs of items to be added to this tab.

2. **Update `CustomTabManager.java`:**
   - Modify the creative tab registration to iterate over the `items` list and find the corresponding `Item` from the registry.
   - However, since standard `Item`s might be registered *after* we load tabs, or we might need to do this carefully without overriding their original tabs entirely. In 1.12.2, an Item only has one `CreativeTabs` field, but we can override `displayAllReleventItems` in our custom tab to dynamically add stacks of those items.
   - We will override the `displayAllReleventItems(NonNullList<ItemStack> p_78018_1_)` method in the `CreativeTabs` anonymous class in `CustomTabManager.registerTab()` to inject the extra items explicitly. This safely allows items from other mods to appear in our tab without messing up their original tab!

3. **Create the Zombie Apocalypse Example Pack:**
   - Instead of standard example stuff, create a pack named `zombie_apocalypse_pack`.
   - Add custom blocks (`barricade.json`).
   - Add items (`canned_beans.json` (food), `baseball_bat.json` (weapon), `fire_axe.json` (tool), `scrap_metal.json` (item)).
   - Add a custom tab (`survivor_tab.json`).
   - Add workbenches (`survivor_workbench.json`).
   - Add recipes (handcraft and workbench).
   - Use textures from the path provided (or placeholders).

4. **Enhance Tools/Weapons:**
   - The user mentioned adding tools/weapons. `ItemCustomTool` and `ItemCustomWeapon` already exist and support attack speed/damage. We just need to thoroughly document and demonstrate them in the example pack, and maybe ensure `ItemCustomTool` properly handles multiple tool types (pickaxe, axe, shovel, etc.).
