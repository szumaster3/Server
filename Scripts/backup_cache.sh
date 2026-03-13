#!/usr/bin/env bash
set -e

CACHE_DIR="../Server/data/cache"
BACKUP_DIR="../Server/data/backup"

mkdir -p "$BACKUP_DIR"

BACKUP_FILE="$BACKUP_DIR/cache_backup_$(date +%s).zip"

tar -a -c -f "$BACKUP_FILE" -C "$CACHE_DIR" .