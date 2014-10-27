import sys
import gc

from util import (web, db, parser, util)

file_export_json = "data/export.json.txt"

data = util.json_read(file_export_json, "{}")

util.json_write(data, file_export_json)
