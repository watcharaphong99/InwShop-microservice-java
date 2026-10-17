#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
for svc in item player auth inventory payment; do
  echo "Packaging inwza-${svc}-service..."
  mvn -q -f "${ROOT}/inwza-${svc}-service/pom.xml" package -DskipTests
done
