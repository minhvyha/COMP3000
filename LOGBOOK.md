# COMP3000 Learning Log

This file is my log-book for Assignment 1. Each week records preparation before class, the class activity, and the assignment work I did afterwards.

I was the sole student contributor to the river-language project. References to
team activities describe the course’s classroom exercises; the project design,
implementation, examples, and documentation were my responsibility, with the
Lox adaptation and generative-AI assistance disclosed in `a1_rubric.md`.

## Week 1 — Team-Based Learning Overview

**Date:** Thursday 30 July 2026

### Preparation before class

Week 1 had no set reading. I read the Team-Based Learning overview so I knew the weekly pattern: individual preparation, the iRAT, the team tRAT, a clarification discussion, and then an application task. I noted that later weeks would expect the preparation and the iRAT to be done before class.

### Activities during class

The class explained how teams would work for the rest of the session. The application task was to write a team contract. We included the team name, the members, what each person was expected to do, and the conditions under which someone could be removed from the team. We also agreed to bring unfinished workshop work back to the group rather than leaving it until the night before a submission. The group shared the contract with the class at the end of the hour.

### Work at home for the assignment

There was no parser task this week. I read the assignment overview and noted that Submission 1 is a parser for a river-flow language, with three example programs and a rubric document. I set up the project folder so later workshop code would have a place to go.

## Week 2 — Introduction to Programming Languages

**Date:** Friday 7 August 2026

### Preparation before class

I read Chapters 1 and 2 of *Crafting Interpreters* and watched the four Echo360 lectures on the book, little languages, the compiler/interpreter pipeline, and compilers versus interpreters. The idea I wanted for class was that a little language can be small and still useful if it fits one domain. I also revised the pipeline: characters become tokens, tokens become a tree, and then an implementation either generates code or executes the tree.

### Activities during class

After the iRAT and tRAT, we used regular expressions on Regexr as an example of a little language. We tried exact text, alternatives, repetition, and capture groups on a short script. Capture groups store a match, and `*` and `+` repeat a pattern, but ordinary regex does not give general variables, conditions, and functions. We reported that regular expressions are a useful little language and are not a complete general-purpose language. The discussion also settled that “compiled” and “interpreted” describe an implementation, not the language itself.

### Work at home for the assignment

I answered the Chapter 1 self-study prompts on little languages and implementation paths. For the river language I wrote down the three things a program must be able to name: a root river, a river formed from other rivers, and the final output river. I decided the first submission should stop at a printed syntax tree, because evaluation is Submission 2.

## Week 3 — Lox and Programs that Generate Programs

**Date:** Friday 14 August 2026

### Preparation before class

I revised Lox’s dynamic types, variables, control flow, functions, and recursion, and the difference between compile time and run time. I worked through the short self-study programs: printing text, printing squares with a loop and again with recursion, building a line of characters, and writing small functions such as doubling a number. I also noted the design point from the notes: a more general feature is not automatically easier to use.

### Activities during class

The application exercise was to write a Lox program that prints a Logo program. We were limited to `clearscreen`, `fd`, `rt`, `pu`, and `pd`. The first picture was a simple path we could check in the online Logo interpreter. The second picture was too long to type by hand, so the Lox program used a loop to generate the repeated `fd` and `rt` lines. We showed both the Lox generator and the Logo text it printed. The useful distinction was that Lox was the program being run, and Logo was the program being produced.

### Work at home for the assignment

I treated the parser the same way: it should turn a flat token list into a tree that I can print and check. I sketched three statement kinds, `source`, `river`, and `outlet`, and expression nodes for a daily flow list, a river name, `+`, and a call such as `delay(flow, days)`. Printing the tree would show the structure before any water is calculated.

## Week 4 — Scanner and Lexical Analysis

**Date:** Monday 17 August 2026

### Preparation before class

I revised scanning: source text in, tokens out, and lexical errors for characters the language does not allow. I answered the preparation questions as follows. The scanner’s job is to chunk characters into tokens. It reports lexical errors, not grammar errors. A name such as `x3` is one identifier token. A language whose only tokens are `(`, `)`, `[`, and `]` needs no lookahead, because each character is already a finished token. In Lox, identifiers need one character of lookahead, and a decimal point needs two, to see whether digits follow the dot.

### Activities during class

I started the water-flow literal for my language. The exemplar form `[1 ~ 1]@4` packs a start day, a spread, and a magnitude into one literal. I preferred a list of daily values, such as `[8, 6, 4]`, because each day is visible in the program and the scanner only needs brackets, commas, and numbers. Other groups used more compact forms. The compact forms read quickly, but they hide the day-by-day shape. That choice also told me which tokens the scanner must recognise: `[`, `]`, `,`, numbers, names, and the words `source`, `river`, and `outlet`.

### Work at home for the assignment

I read the assignment domain notes again: root rivers, rivers that join, and one output river. I wrote the first Canberra sketch with ten daily values and decided `delay` and `retain` would be names followed by parentheses, so the scanner does not need a new keyword for every operation. The next implementation step was a Lox-style scanner with those keywords and a bracketed number list.

## Week 5 — Representing Expressions and Trees

**Date:** Friday 28 August 2026

### Preparation before class

I converted expressions such as `2 / 3 + 3 + (8 * 3)` into trees, and turned trees back into expressions such as `(a + b) * c - (x - y) / z`. Operators became internal nodes and numbers or names became leaves. I also revised context-free grammars: a grammar generates the legal programs, and terminals are the tokens. For arithmetic, the terminals include numbers, `+`, `/`, `*`, and parentheses.

### Activities during class

I treated joining rivers like an arithmetic expression. A flow could be a number, a short unit, or a ten-day list such as `[1, 3, 3, 2, 1, 0, 0, 0, 0, 0]`. I used the list form and `+` for a confluence, so `queanbeyan + upper_molongolo + jerrabomberra` is one expression. As a tree, the first two rivers sit under one `+`, and that result joins the third river under another `+`. I also wrote the expression forms I would need in Nystrom notation: a flow literal, a name, a sum, and parentheses.

### Work at home for the assignment

I drafted the expression part of the grammar and checked it against the Canberra join:

```text
expression → term ;
term       → factor ( ( "-" | "+" ) factor )* ;
factor     → unary ( ( "/" | "*" ) unary )* ;
primary    → NUMBER | flowLiteral | IDENTIFIER | "(" expression ")" ;
flowLiteral → "[" ( NUMBER ( "," NUMBER )* )? "]" ;
```

Multiplication sits above addition, so a runoff coefficient such as `[12, 8, 5] * 0.80` binds before a later `+`. I kept parentheses so a join can be passed as one argument, as in `delay((joes + mahers), 1)`.

## Week 6 — Parser and Grammar

**Date:** Friday 4 September 2026

### Preparation before class

I revised parsing, associativity, precedence, and ambiguity. For `1 + 2 - 3` I wanted the left-associative reading `(1 + 2) - 3`. A left-recursive grammar or a repeating `(operator operand)` rule both give that shape. The open grammar `binary → expression operator expression` is ambiguous, because `5 + 4 * 3` has two trees. Putting `*` in a tighter rule forces `5 + (4 * 3)`. I also revised the Lox AST types `Expr.Binary`, `Expr.Grouping`, `Expr.Literal`, and `Expr.Unary`.

### Activities during class

The task was to get the grammar onto paper before writing Java. I checked my river expressions for a second possible tree. `riverA + riverB + riverC` is only the left-grouped tree if `+` is a loop inside one rule, not a rule that recurses on both sides. I also mapped a new literal through the pipeline: the scanner emits a token, `primary` accepts it, and the parser builds a node. For my list literal that node is a flow of daily numbers, not a Lox array expression.

The grammar I developed during class was:

```text
program     → declaration* EOF ;
declaration → sourceDecl | riverDecl | outletDecl ;
sourceDecl  → "source" IDENTIFIER "=" expression ";" ;
riverDecl   → "river" IDENTIFIER "=" expression ";" ;
outletDecl  → "outlet" IDENTIFIER ";" ;
expression  → term ;
term        → factor ( ( "-" | "+" ) factor )* ;
factor      → unary ( ( "/" | "*" ) unary )* ;
unary       → "-" unary | primary ;
primary     → NUMBER | flowLiteral
            | IDENTIFIER ( "(" arguments? ")" )?
            | "(" expression ")" ;
```

### Work at home for the assignment

I implemented that grammar in the Java parser: `parse` reads a whole program, `declaration` switches on `source`, `river`, and `outlet`, and `flowLiteral` reads the numbers between brackets. Calls such as `delay` and `retain` stay in `primary`, so I did not add a keyword for each operation. `synchronize` skips to the next declaration after a bad line. I checked `upper_molongolo + queanbeyan + jerrabomberra` and confirmed the printer shows a left-nested binary tree.

## Week 7 — Evaluating Expressions

**Date:** Thursday 10 September 2026

### Preparation before class

I revised the difference between an expression and a value. `1 + 2` is a binary expression until the interpreter evaluates both sides and applies `+`, which produces `3`. For a unary operator, the operand is evaluated first. One interpreter object can visit each AST type, and Lox stores runtime values in `Object` so numbers, strings, and booleans share one result type. I traced `1 + "hi"`, `true && false || true`, and `5 - false` far enough to see where a type error is raised during the walk, not during scanning.

### Activities during class

I started a tree-walking evaluator for the flow expressions. The intended walk is: evaluate the left river, evaluate the right river, then apply the language’s meaning of `+`. Rainfall has to enter from outside the program. I kept rainfall external and wrote the resulting daily flows in the source text, which matches the Submission 1 note that rainfall can be supplied later by “magic”. The value I would eventually print is the outlet river, not every temporary sum. I did not finish a working interpreter in class. I did fix the AST shape the interpreter would walk: flow lists, names, binary `+` and `*`, and calls.

### Work at home for the assignment

I stayed on the Submission 1 parser rather than starting evaluation early. I added the three example programs and checked that each one parses. Canberra chains two `delay` calls around a three-river join. The Macquarie program scales each source with `*` and groups `(joes + mahers)` before `delay`. The alpine program uses `spread` on a snowmelt join and `delay` on the rain tributary. I also fixed the printed numbers so whole values do not appear as `8.0`. Evaluation of `delay`, `retain`, and `spread` is still Assignment 2. The parser only records the calls.

## Week 8 — Statements and Assignment 1 Showcase

**Date:** Saturday 19 September 2026

### Preparation before class

I revised statements against expressions. `1 + 2` produces a value. `print 1 + 2;` performs an action. I answered the preparation questions: `visitPrintStmt` evaluates the inner expression and prints it; the grammar grew `varDecl`, `exprStmt`, and `printStmt`; and in

```text
var x = 1;
print x;
var y = 3;
print x + y;
```

the declarations are the two `var` lines. The later names are uses. I then rehearsed the showcase: what `source`, `river`, and `outlet` mean, why a flow is a list of days, how `+` joins rivers, and which files implement the scanner and parser.

### Activities during class

The class was an Assignment 1 showcase. I presented the language on my own. I showed a `source` with a ten-day list, a `river` that joins flows with `+`, a `delay` call, and an `outlet`. I showed the grammar rule for each of those and the command that prints the tree for `examples/canberra.river`. Questions were about whether `delay` already changes the numbers. I answered that Submission 1 only parses it.

Other presentations used a more compact flow literal, closer to the exemplar’s start, spread, and magnitude form, and some people were already sketching dam rules. I would borrow the idea of a named dam later, for Assignment 2. I would not replace the daily list, because the list makes a ten-day hydrograph obvious in the example programs.

### Work at home for the assignment

I checked the path from a `.river` file to the printed tree: scanner, tokens, parser, AST, printer. The three examples match the grammar in `a1_rubric.md`. The parts I treated as my own design are the `source` / `river` / `outlet` declarations, the day-by-day flow list, and calls such as `delay`, `retain`, and `spread` instead of a new keyword for every operation. Before submission I still needed to finish the rubric answers in my own words and remove leftover notes. Week 8 statements also showed how Assignment 2 can grow: a `river` declaration is the same idea as `var`, and the outlet is the value the program should eventually show.

## Project layout update — 26 September 2026

The Java source files moved from the nested package directory to the top
level of `src/`. Their former package declarations were removed to match this
flat layout. From the project root, `javac -d out src/*.java` compiles the
parser, and `java -cp out RiverLox examples/canberra.river` runs an example.
The other example programs remain in `examples/`. The current paths are
recorded in `README.md` and `a1_rubric.md`.
