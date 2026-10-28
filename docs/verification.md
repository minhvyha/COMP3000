# Semantics milestone verification

Verified on 28 October 2026 with OpenJDK 17.0.20.

## Executed checks

From the existing `/Users/salvi/MQ/COMP3000` repository on this Mac:

```sh
JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home sh tests/run-tests.sh
```

Observed result:

```text
PASS 30 semantic test groups (6593 checks).
PASS original parser snapshots (3 programs).
```

The semantic groups include hand-calculated conversions and routing outputs,
closed/half/open dams, independent rain/level policy changes, threshold boundaries,
first-match ordering, previous-day level timing, finite horizons, overflow and
invalid parameters, immutability, isolated runs, downstream propagation, two dams
with travel delay and pending water, and 100 seeded conservation scenarios.

All eight original A1 Java files and three `.river` examples were checked against
`docs/a1-baseline.sha256` in the existing repository. They remain unchanged. The
working tree now contains the new Submission 2 model, documentation, and tests;
all development continues in this same codebase.

## Observed demonstration values

`SemanticsDemo` was built from `src/*.java` and run successfully. It produced:

```text
Rainfall [1, 0, 0] mm over 864000 m2 -> [10, 0, 0] L/s
delay([8, 4], 1) -> [0, 8, 4]
retain([8, 4], 0.25) -> [6, 5, 1]
spread([6, 3], 3) -> [2, 3, 3, 1]

For [8, 0, 0] L/s inflow over three days:
fraction 0   -> [0, 0, 0] L/s; final storage 691200 L
fraction 0.5 -> [4, 2, 1] L/s; final storage 86400 L
fraction 1   -> [8, 0, 0] L/s; final storage 0 L

Adaptive rain/level rules with [6, 0, 0] mm rain:
outflow [2, 4.5, 0.75] L/s; final storage 64800 L
selected rules: rain rule, level rule, default
```

## Scope of this evidence

These are executed Java domain-model results. They establish the definitions
needed for the interpreter, but do not establish that `.river` programs already
evaluate, that the new dam syntax parses, or that the whole A2 rubric is complete.
The original `RiverLox` entry point still prints the A1 syntax tree. New syntax,
name/topology checks, source-located runtime errors, network orchestration, final
marker examples, and rubric evidence remain later integration milestones.
