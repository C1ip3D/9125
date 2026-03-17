#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$repo_root"

javac tools/deploy-ui/src/DeployManagerApp.java
java -cp tools/deploy-ui/src DeployManagerApp
