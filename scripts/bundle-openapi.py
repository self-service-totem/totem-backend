#!/usr/bin/env python3
from pathlib import Path
try:
    import yaml
except ModuleNotFoundError as exc:
    raise SystemExit(
        "Missing dependency 'PyYAML'. Install it with: python3 -m pip install -r scripts/requirements.txt"
    ) from exc

PROJECT_ROOT = Path(__file__).resolve().parents[1]
SOURCE_ROOT = PROJECT_ROOT / "openapi-src"
ROOT_FILE = SOURCE_ROOT / "openapi-root.yaml"
OUTPUT_FILE = PROJECT_ROOT / "openapi.yaml"

root = yaml.safe_load(ROOT_FILE.read_text())
manifest = root.pop("x-openapi-source")

paths = {}
for relative_file in manifest["paths"]:
    fragment = yaml.safe_load((SOURCE_ROOT / relative_file).read_text())
    paths.update(fragment)

schemas = {}
for relative_file in manifest["schemas"]:
    fragment = yaml.safe_load((SOURCE_ROOT / relative_file).read_text())
    schemas.update(fragment)

responses = {}
for relative_file in manifest["responses"]:
    fragment = yaml.safe_load((SOURCE_ROOT / relative_file).read_text())
    responses.update(fragment)

root["paths"] = paths
root["components"] = {
    "schemas": schemas,
    "responses": responses,
}

OUTPUT_FILE.write_text(yaml.safe_dump(root, sort_keys=False, allow_unicode=True))
print(f"Generated {OUTPUT_FILE.relative_to(PROJECT_ROOT)} from openapi-src fragments")
