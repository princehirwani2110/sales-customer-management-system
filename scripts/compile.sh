#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p build/classes
find src/main/java -name '*.java' -print0 | xargs -0 javac --release 17 -encoding UTF-8 -d build/classes
printf 'Compilation succeeded. Add MySQL Connector/J to lib/ and use scripts/run-login.sh to connect to MySQL.\n'
