import re
json = '{"ido":691994,"gfullname":"mun. Chișinău, str. Valea Dicescu 34/A"}'
def get_prop(json_str, prop):
    match = re.search(f'"{prop}"\s*:\s*(?:"([^"]*)"|([0-9]+))', json_str)
    if match: return match.group(1) if match.group(1) is not None else match.group(2)
    return None
print("ido:", get_prop(json, "ido"))
print("gfullname:", get_prop(json, "gfullname"))
print("missing:", get_prop(json, "missing"))
