# A1 Rubric Explanation

This document is part of your first submission. Complete it and include it in your submission zip, alongside your parser and example programs.

## How it works

The rubric explanation is the set of questions below. The first set, the basic
questions, is graded directly and is worth 10% of your marks for this
submission. Answer them accurately to earn those marks.

The remaining sections ask one question for each of the other rubric items.
These are not graded directly, but the answers help the marker award marks for
each rubric item. Each answer points to evidence in the submission.

## Basic questions (10)

1. Which chapter of the book did you use as the starting point for your solution?

### Your answer

Chapter 6, “Parsing Expressions”, of Robert Nystrom’s *Crafting Interpreters*
was the parser starting point. The scanner also follows Chapter 4 and the AST
visitor structure follows Chapter 5.

2. What is the "working folder", and what command(s) compile your parser?

### Your answer

The working folder is the top-level folder of the unzipped submission: the
folder containing `README.md`, `a1_rubric.md`, `LOGBOOK.md`, `src`, and
`examples`. The Java files are directly in `src/`, while the example programs
are in `examples/`.

From that folder, using Java 17 or later:

```sh
mkdir -p out
javac -d out src/*.java
```

For example, parse the Canberra program with:

```sh
java -cp out RiverLox examples/canberra.river
```

3. What literal in your language represents a river that gets 10L/s of flow on the first day after 1mm of rainfall?

### Your answer

The ten-day flow literal is:

```text
[10, 0, 0, 0, 0, 0, 0, 0, 0, 0]
```

For example:

```text
source example = [10, 0, 0, 0, 0, 0, 0, 0, 0, 0];
```

Each position is the flow for one consecutive day. Rainfall is external
“magic” in Submission One, as permitted by the specification, so this literal
describes the resulting flow after the 1mm rainfall event.

4. What symbol in your language shows two rivers combine, and is it a "unary", "binary", or "literal"?

### Your answer

The `+` symbol combines two river-flow expressions. It is a binary operator
because it has a left operand and a right operand, for example `joes + mahers`.
It parses to `Expr.Binary`.

5. Does your language include statements, or is it an expression language?

### Your answer

It includes both statements and expressions. `source`, `river`, and `outlet`
are declaration statements represented by classes in `Stmt.java`. The
right-hand sides of `source` and `river` declarations are expressions
represented by classes in `Expr.java`.

6. In your language, how long does it take all the water to work through a river system after 1 day of rain?

### Your answer

There is no single fixed duration for every program. The grammar permits
variable-length source flow lists; the examples use ten days. Under the
intended evaluation rules in `README.md`, `delay(flow, d)` adds `d` days,
`retain(flow, r)` adds one day when `r > 0`, and `spread(flow, n)` adds `n - 1`
days. Retain releases the held fraction on the next day, and spread releases
each day's input evenly over `n` days. Both preserve total water.

An upper bound is the source-list length plus the extensions along its path
to the outlet, taking the maximum over all source paths. Canberra therefore
has a 13-day upper bound: ten source days, two one-day delays, and one day of
retention. Its actual last non-zero flow is on day 10 because the source lists
end in zeros. The Macquarie and alpine examples have upper bounds of 12 and
13 days respectively. Empty or all-zero flows contribute no water.

Submission One only parses these operations. These durations describe the
intended Assignment 2 semantics, documented under “Intended flow operations”
in `README.md`, rather than results produced by the current parser.

## Log-book submissions (10)

Which file in the zip are your log-book entries and when did you make them? Your teacher needs to have seen them during the semester.

### Your answer

The entries are in `LOGBOOK.md`, one complete week from Week 1 to Week 8. Each week has preparation, the class activity, and the assignment work that followed. The entry dates are 30 July, 7 August, 14 August, 17 August, 28 August, 4 September, 10 September, and 19 September 2026.

## Grammar given in the document in Nystrom's notation (20)

Provide the grammar for your language, and how does each of your example programs parse according to it?

### Your answer

The complete grammar is:

```text
program        → declaration* EOF ;
declaration    → sourceDecl
               | riverDecl
               | outletDecl ;

sourceDecl     → "source" IDENTIFIER "=" expression ";" ;
riverDecl      → "river" IDENTIFIER "=" expression ";" ;
outletDecl     → "outlet" IDENTIFIER ";" ;

expression     → term ;
term           → factor ( ( "-" | "+" ) factor )* ;
factor         → unary ( ( "/" | "*" ) unary )* ;
unary          → "-" unary
               | primary ;
primary        → NUMBER
               | flowLiteral
               | IDENTIFIER ( "(" arguments? ")" )?
               | "(" expression ")" ;
arguments      → expression ( "," expression )* ;
flowLiteral    → "[" ( NUMBER ( "," NUMBER )* )? "]" ;

IDENTIFIER     → ALPHA ALPHANUMERIC* ;
NUMBER         → DIGIT+ ( "." DIGIT+ )? ;
ALPHA          → "a" ... "z" | "A" ... "Z" | "_" ;
ALPHANUMERIC   → ALPHA | DIGIT ;
DIGIT          → "0" ... "9" ;
```

Spaces, carriage returns, and tabs are ignored. Newlines increment the scanner
line number. `//` starts a comment continuing to the end of the line.
`source`, `river`, and `outlet` are reserved keywords. The implementation
limits calls to eight arguments.

### How `canberra.river` parses

The three `source` lines match `sourceDecl`. Each right-hand side matches
`flowLiteral` and creates an `Expr.Flow` with ten values.

`river queanbeyan = delay(googong, 1);` matches `riverDecl`. The `delay`
identifier is followed by parentheses, so the `primary` rule parses a call
with the variable `googong` and number `1` as arguments.

In the central river, `queanbeyan + upper_molongolo + jerrabomberra` is parsed
by the repeated `+` part of `term`. It is left-associative and becomes
`(queanbeyan + upper_molongolo) + jerrabomberra`. That expression and `0.9`
are arguments to `retain`. The final line matches `outletDecl`.

### How `macquarie_creeks.river` parses

Each source matches `sourceDecl`. Its right-hand side is a flow literal
multiplied by a numeric runoff coefficient. The `*` is parsed in `factor` and
creates an `Expr.Binary` with an `Expr.Flow` left child and numeric right child.

In `delay((joes + mahers), 1)`, the inner names and `+` match `term`, the
parentheses produce `Expr.Grouping`, and the group is the first call argument.
`university_lake + devlins` is another binary confluence nested inside the
`retain` call. The final line selects `lower_lachlan` using `outletDecl`.

### How `alpine_snowmelt.river` parses

The three roots match `sourceDecl`. The expression
`west_snowfield + east_snowfield` forms an `Expr.Binary` nested as the first
argument to `spread`; `3` is the second argument. `delay(valley_rain, 1)` is
another call expression.

The two derived forks are combined with `+`, then that complete expression is
the first argument of `retain(..., 0.92)`. The program ends with an
`outletDecl` naming `snowgum_river`.

## Three example programs (20)

Provide your three example programs here and identify which files in your zip contain them.

### Your answer

The parser accepts all three programs below.

### `examples/canberra.river`

```text
source googong = [8, 6, 4, 3, 2, 1, 1, 0, 0, 0];
source upper_molongolo = [4, 5, 7, 6, 4, 3, 2, 1, 0, 0];
source jerrabomberra = [2, 3, 5, 4, 3, 2, 1, 1, 0, 0];

river queanbeyan = delay(googong, 1);
river central_molongolo =
    retain(queanbeyan + upper_molongolo + jerrabomberra, 0.9);
river lower_molongolo = delay(central_molongolo, 1);

outlet lower_molongolo;
```

### `examples/macquarie_creeks.river`

```text
source joes = [12, 8, 5, 3, 2, 1, 0, 0, 0, 0] * 0.80;
source mahers = [7, 6, 4, 2, 1, 0, 0, 0, 0, 0] * 0.65;
source devlins = [4, 7, 6, 4, 2, 1, 0, 0, 0, 0] * 0.55;

river university_lake = delay((joes + mahers), 1);
river lower_lachlan = retain(university_lake + devlins, 0.85);

outlet lower_lachlan;
```

### `examples/alpine_snowmelt.river`

```text
source west_snowfield = [1, 2, 5, 9, 12, 10, 7, 4, 2, 1];
source east_snowfield = [0, 1, 3, 6, 10, 11, 8, 5, 3, 1];
source valley_rain = [6, 5, 4, 3, 2, 1, 1, 0, 0, 0];

river alpine_fork =
    spread(west_snowfield + east_snowfield, 3);
river valley_fork = delay(valley_rain, 1);
river snowgum_river =
    retain(alpine_fork + valley_fork, 0.92);

outlet snowgum_river;
```

## Parser written in Java based on Lox codebase (20)

Which chapter of the book is your parser based on? What did you add beyond the Chapter 6 code, and where is that explained?

### Your answer

The parser is based on Chapter 6 of *Crafting Interpreters*. Its recursive
descent structure, token navigation helpers, precedence methods, AST visitor
pattern, and error-reporting style follow Lox. The relationship is also stated
in `README.md` and in comments at the top of `Scanner.java` and `Parser.java`.

The following features go beyond the Chapter 6 expression parser:

1. `Parser.parse()` consumes a complete program containing multiple
   declarations instead of only one expression.
2. `Stmt.java` provides domain-specific `Source`, `River`, and `Outlet` nodes.
3. `Expr.Flow` represents a multi-day flow directly.
4. `Expr.Call` supports reusable hydrological operations such as `delay`,
   `retain`, and `spread`.
5. `Parser.synchronize()` recovers at a semicolon or the next declaration so
   one malformed declaration does not prevent parsing later declarations.
6. `AstPrinter.java` prints every statement and nested expression, making the
   parser result visible before an interpreter exists.
7. `RiverLox.java` accepts a program filename, reports scanner/parser errors
   with line numbers, and uses a non-zero exit status for invalid input.

The implementations are in
`src/Parser.java`, `src/Expr.java`, `src/Stmt.java`,
`src/AstPrinter.java`, and `src/RiverLox.java`. These source files now use
the default Java package, and compilation places their class files directly
in `out/`.

## Uniqueness and Creativity (20)

What did you do beyond the in-class work? Point your marker to where it lives in your submission.

### Your answer

I was the sole student contributor to this project, including the river-language
design, grammar, Java parser, example programs, and submission documentation.
No other team member contributed to the project. I developed it using the
course material and Nystrom's Lox architecture, with generative-AI assistance
as disclosed below. The project work described in my logbook includes my own
work during workshops as well as work outside class.

The language separates river topology from flow transformations.
`source`, `river`, and `outlet` make each location’s role in the network
explicit, while expressions describe how water is combined or transformed.
These constructs are implemented by the domain AST classes in `Stmt.java`.

Multi-day hydrographs are first-class bracketed literals implemented by
`Expr.Flow` and `Parser.flowLiteral()`. This is more concise than representing
each day as a separate variable.

Hydrological operations use generic call syntax implemented by `Expr.Call` and
`Parser.finishCall()`. This lets programs use readable operations including
`delay`, `retain`, and `spread` without turning every operation into a reserved
keyword or changing the grammar each time an operation is added.

The three examples demonstrate different combinations of these features:
Canberra uses chained travel delays, Macquarie uses runoff scaling and explicit
grouping, and the alpine system uses spread and independently delayed
tributaries.

Compared with the Chapter 6 expression parser, this submission parses complete
river systems with named sources, derived rivers, and an explicit outlet.
Compared with the exemplar's compact start/spread/magnitude literal, the daily
list exposes the hydrograph directly. Generic calls let the grammar represent
different flow transformations through one expression form. These differences
are visible in the grammar above, `Stmt.java`, `Expr.java`, `Parser.java`, and
the three example files. The calls are parsed here; their evaluation belongs
to Submission Two.

## Attribution

The architecture is adapted from Robert Nystrom’s Java Lox implementation in
*Crafting Interpreters*. The implementation and this explanation
were prepared with generative-AI assistance.
