# Planned A2 syntax

Status: design only. The current scanner/parser still recognises the unchanged
A1 grammar. This document freezes the intended integration contract; programs
using the new statements below will not yet parse with `RiverLox`.

## Proposed complete grammar in Nystrom notation

```text
program        -> declaration* EOF ;
declaration    -> rainfallDecl | simulateDecl | sourceDecl | riverDecl
               | damDecl | outletDecl ;
rainfallDecl   -> "rainfall" "=" flowLiteral ";" ;
simulateDecl   -> "simulate" NUMBER ";" ;
sourceDecl     -> "source" IDENTIFIER "=" expression ";" ;
riverDecl      -> "river" IDENTIFIER "=" expression ";" ;
outletDecl     -> "outlet" IDENTIFIER ";" ;
damDecl        -> "dam" IDENTIFIER "{" damRule* defaultRule "}" ;
damRule        -> "when" factor comparison NUMBER ":" NUMBER ";" ;
defaultRule    -> "default" ":" NUMBER ";" ;
factor         -> "inflow" | "rain" | "level" ;
comparison     -> "<" | "<=" | "==" | ">=" | ">" ;

expression     -> term ;
term           -> factorExpr ( ( "-" | "+" ) factorExpr )* ;
factorExpr     -> unary ( ( "/" | "*" ) unary )* ;
unary          -> "-" unary | primary ;
primary        -> NUMBER | flowLiteral
               | IDENTIFIER ( "(" arguments? ")" )?
               | "(" expression ")" ;
arguments      -> expression ( "," expression )* ;
flowLiteral    -> "[" ( NUMBER ( "," NUMBER )* )? "]" ;

IDENTIFIER     -> ALPHA ALPHANUMERIC* ;
NUMBER         -> DIGIT+ ( "." DIGIT+ )? ;
ALPHA          -> "a" ... "z" | "A" ... "Z" | "_" ;
ALPHANUMERIC   -> ALPHA | DIGIT ;
DIGIT          -> "0" ... "9" ;
```

In the rainfall declaration, the bracketed literal is interpreted as mm rather
than L/s. It produces a `RainfallSeries`, not a `FlowValue`. `factorExpr` is the
original arithmetic `factor` rule renamed here to distinguish it from the factor
observed by a dam condition. Arithmetic precedence is otherwise unchanged.

Whitespace and `//` comments follow A1 rules. Calls retain the eight-argument
limit. New reserved words are `rainfall`, `simulate`, `dam`, `when`, `default`,
`inflow`, `rain`, and `level`. Comparisons belong to dam conditions only; this
design does not add general boolean expressions, blocks, loops, or assignment.

## Planned built-ins and semantic checks

- `runoff(area_m2)` reads the shared rainfall series and returns flow in L/s.
- `delay(flow, days)`, `retain(flow, fraction)`, and `spread(flow, days)` retain
  the A1 documented meanings and the now-tested implementation.
- `release(dam_name, upstream_flow)` applies a named dam for the global horizon.
- One `rainfall` declaration is required before `runoff` or dam application.
- One `simulate` declaration is required before dam application; it sets the
  shared positive integer horizon. Pure A1 programs need neither declaration.
- Duplicate configuration declarations or names are errors. Names must be
  declared before use. The outlet must name one source or river; exactly one
  outlet is required by the interpreter.
- A physical dam may be applied once, then its named outflow can be reported.
  The interpreter must check the no-splitting and duplicate-input rules and keep
  post-horizon routing tails separately from water arriving within the window.
- All rule thresholds and fractions are literal parameters in the first version.
  Fractions outside [0,1] are errors. The default is required exactly once and last.

These checks are integration requirements, not capabilities already implemented
in the parser or the isolated domain classes.

## Example of the proposed syntax

```text
rainfall = [6, 0, 0];
simulate 3;

source headwaters = runoff(864000);

dam reservoir {
  when rain >= 5: 0.25;
  when level >= 300000: 0.75;
  default: 0.50;
}

river regulated = release(reservoir, headwaters);
outlet regulated;
```

The fractions express release rather than retention. `level` is litres before
today's inflow, and `inflow` is L/s. The preceding Java demonstration uses an
explicit `[8, 0, 0]` L/s inflow to make its expected results easy to calculate;
this syntax example derives its own different inflow from rain and area.

During integration, update tokens, scanner, AST visitors, parser recovery, actual
grammar documentation, and example programs together. Do not put this design
grammar into the final rubric as implemented until those changes pass tests.
