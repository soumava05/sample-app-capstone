#!/usr/bin/env bash
set -euo pipefail

# Build + test for the Spring Boot Maven project
# - Runs unit/integration tests (TestNG via surefire)
# - Produces an executable jar under target/

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

if ! command -v mvn >/dev/null 2>&1; then
  echo "ERROR: mvn (Maven) is not installed or not on PATH." >&2
  exit 1
fi

mvn -B -U clean verify

echo
echo "Build complete. Artifacts:" 
ls -1 target/*.jar 2>/dev/null || true
