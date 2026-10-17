#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
./scripts/compile.sh
mkdir -p build/test-classes
javac --release 17 -encoding UTF-8 -cp build/classes -d build/test-classes tests/java/com/vit/sales/security/PasswordHasherSmokeTest.java
java -cp build/classes:build/test-classes com.vit.sales.security.PasswordHasherSmokeTest
