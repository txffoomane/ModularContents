import sys
import re

def resolve_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # We want to replace <<<<<<< HEAD ... ======= with nothing, and remove >>>>>>> <commit>
    # The pattern should match non-greedily from <<<<<<< HEAD to =======
    
    # Replace <<<<<<< HEAD ... =======
    pattern_head = r'<<<<<<< HEAD.*?=======\n'
    content = re.sub(pattern_head, '', content, flags=re.DOTALL)
    
    # Replace >>>>>>> 842bc66...
    pattern_tail = r'>>>>>>> [^\n]+\n'
    content = re.sub(pattern_tail, '', content)
    
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

resolve_file("D:/CONTRACT/modularcontents/src/main/java/modularcontents/custom/inventory/ContainerHandcraft.java")
resolve_file("D:/CONTRACT/modularcontents/src/main/java/modularcontents/custom/network/PacketHandcraftAction.java")
