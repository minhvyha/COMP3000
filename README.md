# COMP3000 River Language

Starter code for Assignment 1: a Java recursive-descent parser based on the
scanner, AST, parser, visitor, and panic-mode error handling patterns in
Chapters 4–6 of Robert Nystrom's *Crafting Interpreters*.

Three parser examples and a draft rubric-evidence document are included. This
is still not a finished personal submission: obtain the official
`a1_rubric.md` template from iLearn, preserve its question text, move the draft
answers beneath the matching questions, and rewrite the design reflection in
your own words.

## Current language idea

- `source` names a root river and its flow expression.
- `river` combines or transforms upstream flows.
- `outlet` identifies the output river.
- `[8, 6, 4]` is a flow literal: one value per day.
- `+`, `-`, `*`, and `/` compose flow expressions.
- Calls such as `delay(flow, 1)` and `retain(flow, 0.9)` provide readable
  domain operations. Assignment 1 parses these operations but does not
  evaluate them.

The generic call syntax is intentional: it gives you room to choose useful
river operations without repeatedly changing the parser. You still need to
decide which calls are valid and explain their intended meaning.

## Build and run

Requires Java 17 or later.

```sh
mkdir -p out
javac -d out $(find src -name '*.java')
java -cp out edu.mq.comp3000.river.RiverLox examples/canberra.river
```

Successful parsing prints the syntax tree. Syntax errors are written to
standard error and produce exit code 65.

The three supplied programs are:

- `examples/canberra.river`
- `examples/macquarie_creeks.river`
- `examples/alpine_snowmelt.river`

## Starter grammar (Nystrom notation)

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

## Design decisions to review before submission

1. Should a flow literal permit any number of days, require exactly ten, or
   carry its own duration?
2. Which operations should be built into the language, and what should each
   one mean in Assignment 2?
3. Should the parser reject references to rivers declared later, duplicate
   names, unknown operations, and multiple outlets, or should a later semantic
   pass handle those checks?
4. How will rainfall and catchment size enter source flows in Assignment 2?
5. What syntax could distinguish your language from the class exemplar while
   remaining easy to read?

## Attribution

The overall implementation structure is adapted from the Java implementation
in *Crafting Interpreters* by Robert Nystrom, whose source code is MIT licensed.