sed -i '/ModularcontentsMod.radio.setTileEntityItemStackRenderer/d' src/main/java/modularcontents/proxy/ClientProxy.java
sed -i '/ModelBakery.registerItemVariants(radio,/d' src/main/java/modularcontents/ModularcontentsMod.java
rm src/main/java/modularcontents/custom/client/render/RenderRadioItem.java
cp src/main/resources/assets/modularcontents/models/item/radio_base.json src/main/resources/assets/modularcontents/models/item/radio.json
rm src/main/resources/assets/modularcontents/models/item/radio_base.json
rm src/main/resources/assets/modularcontents/models/item/radio_glow.json
