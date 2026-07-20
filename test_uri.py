import os
import urllib.request
from pathlib import Path
root = Path("recipes").absolute()
file = Path("recipes/handcraft/vanilla_book.json").absolute()
print(root)
print(file)
# java URI relativize roughly does this:
print(file.relative_to(root).as_posix())
