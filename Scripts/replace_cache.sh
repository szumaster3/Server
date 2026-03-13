#!/usr/bin/env bash
set -e

CACHE_DIR="../Server/data/cache"
BACKUP_DIR="../Server/data/backup"

LATEST_BACKUP=$(find "$BACKUP_DIR" -maxdepth 1 -type f -name 'cache_backup_*.zip' -printf '%T@ %p\n' 2>/dev/null | \
                sort -nr | head -n1 | cut -d' ' -f2-)

[ -z "$LATEST_BACKUP" ] && { echo "No backups"; exit 1; }

rm -rf "$CACHE_DIR"
mkdir -p "$CACHE_DIR"

tar -a -xf "$LATEST_BACKUP" -C "$CACHE_DIR"