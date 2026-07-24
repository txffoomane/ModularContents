import os
import json
import re
import requests
from bs4 import BeautifulSoup
from urllib.parse import urljoin

BASE_URL = "https://pzwiki.net"
PACK_DIR = "run/ModularContents/ProjectZomboid"

# Создаем нужные папки
DIRS = [
    "weapons", "items", "food", "recipes/handcraft",
    "assets/modularcontents/textures/items"
]

for d in DIRS:
    os.makedirs(os.path.join(PACK_DIR, d), exist_ok=True)

def clean_id(name):
    # Превращаем "Spiked Baseball Bat" в "spiked_baseball_bat"
    cleaned = name.lower().replace(' ', '_').replace('-', '_')
    return re.sub(r'[^a-z0-9_]', '', cleaned)

def download_image(img_url, item_id):
    if not img_url: return
    full_url = urljoin(BASE_URL, img_url)
    try:
        r = requests.get(full_url, stream=True)
        if r.status_code == 200:
            path = os.path.join(PACK_DIR, "assets/modularcontents/textures/items", f"{item_id}.png")
            with open(path, 'wb') as f:
                for chunk in r.iter_content(1024):
                    f.write(chunk)
            return True
    except Exception as e:
        print(f"Ошибка загрузки картинки для {item_id}: {e}")
    return False

def save_json(folder, item_id, data):
    path = os.path.join(PACK_DIR, folder, f"{item_id}.json")
    with open(path, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2, ensure_ascii=False)

def parse_category(url, category_type):
    print(f"\n--- Парсинг {category_type.upper()} с {url} ---")
    response = requests.get(url)
    soup = BeautifulSoup(response.text, 'html.parser')

    tables = soup.find_all('table', class_='wikitable')
    count = 0

    for table in tables:
        rows = table.find_all('tr')
        for row in rows[1:]: # Пропускаем заголовок
            cols = row.find_all(['td', 'th'])
            if len(cols) < 2: continue

            # Ищем иконку (обычно в первой или второй колонке)
            img_tag = None
            name = ""

            for col in cols:
                if not img_tag:
                    img_tag = col.find('img')

                # Ищем текст (название предмета)
                a_tag = col.find('a')
                if a_tag and a_tag.text.strip() and not name:
                    name = a_tag.text.strip()

            if name and img_tag:
                item_id = clean_id(name)

                # Если такой уже есть, пропускаем
                if os.path.exists(os.path.join(PACK_DIR, category_type, f"{item_id}.json")):
                    continue

                img_url = img_tag.get('src')
                if not img_url: continue

                print(f"Найдено: {name} ({item_id})")

                # Скачиваем картинку
                download_image(img_url, item_id)

                # Генерируем JSON в зависимости от типа
                if category_type == "weapons":
                    data = {
                        "name": name,
                        "damage": 6.0,
                        "attackSpeed": -2.4,
                        "durability": 200,
                        "creativeTab": "modular_contents"
                    }
                elif category_type == "food":
                    data = {
                        "name": name,
                        "hunger": 4,
                        "saturation": 0.5,
                        "maxStackSize": 16,
                        "creativeTab": "modular_contents"
                    }
                else: # items
                    data = {
                        "name": name,
                        "maxStackSize": 64,
                        "creativeTab": "modular_contents"
                    }

                save_json(category_type, item_id, data)
                count += 1

                # Ограничитель для теста (чтобы не парсить сразу тысячи штук и не получить бан от вики)
                if count >= 30:
                    return

parse_category(f"{BASE_URL}/wiki/Weapons", "weapons")
parse_category(f"{BASE_URL}/wiki/Food", "food")
parse_category(f"{BASE_URL}/wiki/Items", "items")

print("\nГотово! Базовые предметы, еда и оружие спарсены. Иконки загружены.")
