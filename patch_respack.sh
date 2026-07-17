#!/bin/bash
cat src/main/java/modularcontents/custom/client/ModularResourcePack.java | sed 's/if (path.startsWith("textures\/") && path.endsWith("\.png")) {/InputStream packStream = findTexture(path);\n        if (packStream != null) return packStream;\n\n        if (path.startsWith("textures\/") \&\& path.endsWith(".png")) {/' > ModularResourcePack.java.tmp
