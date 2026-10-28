# COMP3000 River Language - Submission 2

Submission 2 is developed directly in this repository, building on the Java
Lox-style parser from Submission 1. Your A1 result was 100%; its Git baseline
is recorded in `docs/baseline.md`.

## Current implementation

The units, flow behaviour, and dam rules are implemented and tested in Java:

- Daily average flow in L/s, daily rainfall in mm, catchment area in m2, and
  dam storage in litres.
- Immutable flow values with zero-padded arithmetic and water-preserving
  `delay`, `retain`, and `spread` operations.
- Rainfall and catchment conversion to flow.
- Ordered dam conditions over inflow, rainfall, and previous-day storage,
  with a mandatory default and release fractions in [0, 1].
- Daily dam balances, dry-day releases, finite simulation windows, and
  explicit accounting for water still in transit.

The original `source`, `river`, and `outlet` parser is retained. The `RiverLox`
entry point currently prints syntax trees. Connecting it to the new evaluation
model and adding dam declarations to the grammar are the next milestones.
This is an in-progress A2 implementation, not the completed submission.

## Project layout

- `src/`: original scanner/parser/AST plus the Submission 2 model classes.
- `examples/`: the three original `.river` programs.
- `tests/`: semantic checks, original parser snapshots, and the test runner.
- `docs/semantics.md`: implemented units, flow behaviour, dam rules, and limits.
- `docs/a2-syntax-design.md`: planned syntax, clearly marked as not yet parsed.
- `docs/baseline.md` and `docs/a1-baseline.sha256`: A1 origin and fingerprints.
- `docs/verification.md`: results from the current milestone.
- `a1_rubric.md` and `LOGBOOK.md`: existing A1 explanation and learning record.

All Java files use the default package and remain directly inside `src/`.
No separate Submission 2 project or source tree is required.

## Build and run

Requires Java 17 or later, including `javac`. No additional libraries or build
system are needed. Run these commands from this repository's root:

```sh
mkdir -p out
javac -d out src/*.java
java -cp out SemanticsDemo
```

`SemanticsDemo` prints rainfall conversion, routing examples, closed/half/open
release results, and the adaptive dam trace using the Java model.

To run an existing `.river` program through the current parser:

```sh
java -cp out RiverLox examples/canberra.river
```

The other original programs are `examples/macquarie_creeks.river` and
`examples/alpine_snowmelt.river`. Parsing errors use standard error and exit
status 65. Usage errors use exit status 64.

## Verify changes

```sh
sh tests/run-tests.sh
```

The test runner builds into a temporary directory and returns non-zero on
failure. It checks the implemented semantics and compares all three original
programs with syntax-tree output captured from the A1 code. Checks run without
requiring Java's `-ea` flag.

The script honours `JAVA_HOME` when set. On this Mac, a shell selecting Apple's
Java launcher can use the installed Homebrew JDK:

```sh
JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home sh tests/run-tests.sh
```

That location is machine-specific. Other machines should use their own Java 17
installation or put its `bin` directory on `PATH`.

## Current parser grammar

This is the implemented A1 grammar. New A2 syntax is designed separately in
`docs/a2-syntax-design.md` until scanner/parser integration is complete.

```text
program        -> declaration* EOF ;
declaration    -> sourceDecl | riverDecl | outletDecl ;
sourceDecl     -> "source" IDENTIFIER "=" expression ";" ;
riverDecl      -> "river" IDENTIFIER "=" expression ";" ;
outletDecl     -> "outlet" IDENTIFIER ";" ;
expression     -> term ;
term           -> factor ( ( "-" | "+" ) factor )* ;
factor         -> unary ( ( "/" | "*" ) unary )* ;
unary          -> "-" unary | primary ;
primary        -> NUMBER | flowLiteral
               | IDENTIFIER ( "(" arguments? ")" )?
               | "(" expression ")" ;
arguments      -> expression ( "," expression )* ;
flowLiteral    -> "[" ( NUMBER ( "," NUMBER )* )? "]" ;
```

Identifiers start with a letter or underscore and continue with letters,
digits, or underscores. Number literals are non-negative decimals; unary
minus is parsed separately. `//` begins a line comment. Calls accept at most
eight arguments. The runtime model separately rejects negative physical flows,
non-finite values, invalid fractions, and invalid operation parameters.

## Remaining milestones

Add a tree-walking interpreter and source-located runtime errors; connect
rainfall/catchment inputs and dam syntax; implement name and network checks;
then prepare the reproducible A2 examples and rubric explanation. Historical
logbook entries and A1 rubric answers have not been rewritten.

## Attribution

The Lox-style foundation is adapted from Robert Nystrom's MIT-licensed
*Crafting Interpreters*. The implementation and documentation were prepared
with generative-AI assistance.
