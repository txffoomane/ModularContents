import json
import os
import glob

def remove_id(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    if 'id' in data:
        del data['id']
    
    if 'type' in data:
        del data['type']
        
    if 'workbench' in data:
        del data['workbench']
        
    with open(file_path, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=4, ensure_ascii=False)

for file_path in glob.glob('src/main/resources/assets/modularcontents/example_pack/recipes/**/*.json', recursive=True):
    try:
        remove_id(file_path)
    except Exception as e:
        print(f"Error processing {file_path}: {e}")

