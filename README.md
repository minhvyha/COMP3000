# COMP3000 River Language

Assignment 1 implementation: a Java recursive-descent parser based on the
scanner, AST, parser, visitor, and panic-mode error handling patterns in
Chapters 4–6 of Robert Nystrom's *Crafting Interpreters*.

The submission includes three example programs, the rubric explanation in
`a1_rubric.md`, and the learning log in `LOGBOOK.md`.

## Project layout

The working folder is the repository root, containing `README.md`,
`a1_rubric.md`, `LOGBOOK.md`, `src/`, and `examples/`. All eight Java source
files are directly inside `src/` (for example, `src/Parser.java` and
`src/RiverLox.java`). The three `.river` programs are in `examples/`.
The Java files use the default package, so `javac -d out` places the compiled
classes directly in `out/`.

## Language overview

- `source` names a root river and its flow expression.
- `river` combines or transforms upstream flows.
- `outlet` identifies the output river.
- `[8, 6, 4]` is a flow literal: one value per day.
- `+`, `-`, `*`, and `/` compose flow expressions.
- Calls such as `delay(flow, 1)` and `retain(flow, 0.9)` provide readable
  domain operations. Assignment 1 parses these operations but does not
  evaluate them.

The generic call syntax lets the language express domain operations without
making each operation a reserved keyword. The intended built-in operations
are `delay`, `retain`, and `spread`, defined below.

## Flow values and duration

A flow is a sequence of daily average flow rates in L/s. The first entry is
day 1 after the rainfall event. Because every entry covers the same one-day
interval, preserving the sum of the entries preserves total water volume.
A flow is zero before its first entry and after its last entry. When two
flows are added, entries on the same day are added; the shorter sequence is
padded with zeros.

The grammar accepts any number of entries, including `[]`, which denotes no
flow in the intended evaluation model. The examples use ten-day source
hydrographs to follow the assignment's rainfall-arrival assumption. Downstream
operations can extend the output beyond day 10; their final days must not be
truncated. A sequence's stored length can exceed the last day of actual flow
when it contains trailing zeros.

## Intended flow operations

Assignment 1 parses calls into `Expr.Call` nodes and prints their structure.
The following definitions specify the intended Assignment 2 behaviour; no
flow evaluation or operation-specific argument validation is implemented yet.
Each operation takes a flow followed by one numeric parameter. Flow entries
must be finite and non-negative under this model.

### `delay(flow, days)`

Shift every daily value forward by `days`, a non-negative integer. Prepend
that many zeros and keep all original values. A zero-day delay is the identity.

```text
delay([8, 4], 1) = [0, 8, 4]
```

This represents a fixed travel time. It preserves total water and extends a
non-empty sequence of length `L` to `L + days`.

### `retain(flow, fraction)`

Temporarily hold `fraction` of each day's incoming water and release that held
portion on the following day. Release the remaining `1 - fraction` on the
arrival day. The fraction must be a finite number between 0 and 1 inclusive.
Previously held water is released in full; it is not retained again.

For input `x`, the output on day `t` is:

```text
y[t] = (1 - fraction) * x[t] + fraction * x[t - 1]
retain([8, 4], 0.25) = [6, 5, 1]
```

Day 1 releases 6 and holds 2. Day 2 releases 3 from the new inflow plus the
held 2, and holds 1. Day 3 releases the final 1. Input and output both sum
to 12. A fraction of 0 leaves the flow unchanged; a fraction of 1 is a one-day
delay. A positive fraction extends a non-empty sequence by one day.

This models brief storage along a river reach while respecting the assumption
that all water entering a river eventually leaves it. In the Canberra example,
`retain(..., 0.9)` holds 90% of each day's inflow until the next day. It does
not remove 10% of the water. The fixed one-day rule gives a finite end time;
human-controlled dam rules remain a separate Assignment 2 feature.

### `spread(flow, days)`

Distribute each day's incoming water equally across `days` consecutive days,
starting on its arrival day. The parameter must be a positive integer.
Contributions from different arrival days are added where they overlap.

```text
y[t] = (x[t] + x[t - 1] + ... + x[t - (days - 1)]) / days
spread([6, 3], 3) = [2, 3, 3, 1]
```

The first 6 contributes 2 on days 1, 2, and 3. The next 3 contributes 1 on
days 2, 3, and 4. Both sequences sum to 9. A one-day spread is the identity;
a larger spread extends a non-empty sequence of length `L` to `L + days - 1`.
All tail values are kept, using zeros for input days outside the sequence.

This models water arriving through paths with different travel times. In the
alpine example, `spread(..., 3)` distributes each day's snowmelt over three
days, smoothing the peak while preserving total water. Unlike `delay`, it
changes the hydrograph's shape. Unlike `retain`, it distributes water evenly
over a chosen duration rather than choosing the fraction held for one day.

### End of a river system's flow

An empty input remains empty under each operation. For a non-empty source,
start with its sequence length and add each operation's duration extension
along its path: `days` for `delay`, one for a positive `retain` fraction, and
`days - 1` for `spread`. At a confluence, take the longer duration. The longest
source-to-outlet result is an upper bound; trailing zeros can make the actual
last day of flow earlier.

For the ten-day source lists in the examples, the output-length upper bounds
are 13 days for Canberra (two delays and one retain), 12 for Macquarie (one
delay and one retain), and 13 for alpine snowmelt (a three-day spread and one
retain). With the actual Canberra values, the final non-zero output is on
day 10. These are consequences of the intended rules, not interpreter output.

## Build and run

Requires Java 17 or later.

```sh
mkdir -p out
javac -d out src/*.java
java -cp out RiverLox examples/canberra.river
```

Successful parsing prints the syntax tree. Syntax errors are written to
standard error and produce exit code 65.

The three supplied programs are:

- `examples/canberra.river`
- `examples/macquarie_creeks.river`
- `examples/alpine_snowmelt.river`

## Grammar (Nystrom notation)

```text
program        → declaration* EOF ;
declaration    → sourceDecl | riverDecl | outletDecl ;
sourceDecl     → "source" IDENTIFIER "=" expression ";" ;
riverDecl      → "river" IDENTIFIER "=" expression ";" ;
outletDecl     → "outlet" IDENTIFIER ";" ;

expression     → term ;
term           → factor ( ( "-" | "+" ) factor )* ;
factor         → unary ( ( "/" | "*" ) unary )* ;
unary          → "-" unary | primary ;
primary        → NUMBER
               | flowLiteral
               | IDENTIFIER ( "(" arguments? ")" )?
               | "(" expression ")" ;
arguments      → expression ( "," expression )* ;
flowLiteral    → "[" ( NUMBER ( "," NUMBER )* )? "]" ;
```

Lexically, identifiers begin with a letter or underscore and continue with
letters, digits, or underscores. Numbers are non-negative decimal literals;
negative values are represented by the unary `-` grammar rule. `//` begins a
line comment.

## Assignment 2 implementation work

The interpreter will implement the operation definitions above and validate
argument counts, parameter ranges, flow values, and operation names. The
current parser checks syntax and the generic eight-argument call limit only.
Name resolution, duplicate declarations, network validity, and selection of
one outlet also need semantic checks. Rainfall and catchment size inputs and
human-controlled dams will be added for Assignment 2.

## Attribution

The overall implementation structure is adapted from the Java implementation
in *Crafting Interpreters* by Robert Nystrom, whose source code is MIT licensed.

The implementation and documentation were prepared with generative-AI assistance.
