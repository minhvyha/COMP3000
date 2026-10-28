#!/bin/sh
set -eu

cd "$(dirname "$0")/.."
test_dir=$(mktemp -d "${TMPDIR:-/tmp}/riverlox-semantics.XXXXXX")
trap 'rm -rf "$test_dir"' 0
trap 'exit 1' 1 2 3 15

if [ -n "${JAVA_HOME:-}" ]; then
  java_bin="$JAVA_HOME/bin/java"
  javac_bin="$JAVA_HOME/bin/javac"
else
  java_bin=java
  javac_bin=javac
fi

"$javac_bin" -d "$test_dir" src/*.java tests/SemanticsTest.java
"$java_bin" -cp "$test_dir" SemanticsTest

for program in examples/*.river; do
  program_name=$(basename "$program" .river)
  "$java_bin" -cp "$test_dir" RiverLox "$program" > "$test_dir/$program_name.ast"
  diff -u "tests/expected/$program_name.ast" "$test_dir/$program_name.ast"
done
printf '%s\n' 'PASS original parser snapshots (3 programs).'
