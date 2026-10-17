#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
./scripts/compile.sh
mkdir -p build/test-classes
javac --release 17 -encoding UTF-8 -cp build/classes -d build/test-classes src/test/java/com/vit/sales/Stage3SmokeTest.java
java -cp build/classes:build/test-classes com.vit.sales.Stage3SmokeTest
./scripts/test-password-hasher.sh
