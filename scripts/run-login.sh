#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
if [[ ! -f lib/mysql-connector-j.jar ]]; then echo 'Missing lib/mysql-connector-j.jar. Place the MySQL Connector/J JAR there.' >&2; exit 1; fi
./scripts/compile.sh
java -cp 'build/classes:lib/mysql-connector-j.jar' com.vit.sales.ui.LoginFrame
