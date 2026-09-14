# COMP3000 Learning Log

This log records my weekly preparation, class activities, and independent work
for Assignment 1.

> Entries marked **Confirm** must be checked against my own notes before
> submission. This document should record work I actually completed and should
> not be used to reconstruct attendance or activities that did not occur.

## Week 1 — Team-Based Learning Overview

**Date:** _Add the actual class date._

### Preparation before class

There was no formal pre-class preparation or self-study exercise for Week 1.
I reviewed the Team-Based Learning overview to understand the weekly process:
individual preparation, the Individual Readiness Assurance Test (iRAT), the
Team Readiness Assurance Test (tRAT), peer-led clarification, and the team
application activity. I noted that preparation and completion of the iRAT
would be required before future classes.

### Activities during class

The class introduced Team-Based Learning and explained the responsibilities of
individual students and teams. The application exercise was to develop a team
contract covering:

- the team name and members;
- expectations for each member;
- conditions under which a member could be excluded; and
- any additional agreements needed for the team to work effectively.

**Confirm:** Add the team name, my contribution to the contract, one decision
the team made, and whether the team shared its contract with the class.

### Work at home for the assignment

No Assignment 1 implementation task or self-study exercise was prescribed in
the Week 1 worksheet. I reviewed the unit’s collaborative-work expectations
and considered how the team contract could support later work on the river
language.

**Confirm:** Replace or extend this paragraph with any actual setup, reading,
notes, or assignment work completed at home during Week 1.

## Week 2 — Introduction to Programming Languages

**Date:** _Add the actual class date._

### Preparation before class

The prescribed preparation was Chapters 1 and 2 of *Crafting Interpreters* and
four Echo360 lectures covering how to use the textbook, little languages, the
compiler/interpreter pipeline, and the distinction between compilers and
interpreters. I focused on the idea that a domain-specific “little language”
can provide concise notation for one problem area. I also reviewed the main
pipeline stages: scanning characters into tokens, parsing tokens into a tree,
static analysis, optimisation, and code generation or direct interpretation.

**Confirm:** Record which readings and videos I actually completed before the
class and add any questions I brought to the iRAT.

### Activities during class

After the iRAT and team tRAT, the application exercise used regular expressions
as an example of a little language. The team experimented in Regexr with exact
matches, alternatives, repetition, and capture groups. We then considered
whether regular expressions form a computationally complete language by
looking for variables, conditions, loops, and functions. Capture groups can
act like stored values and `*` or `+` provide repetition, but ordinary regular
expressions do not provide general control flow or functions. This supported
the conclusion that ordinary regex syntax is useful and expressive within its
domain without necessarily being a complete general-purpose language.

The exercise also reinforced that “compiled” and “interpreted” describe a
language implementation rather than the language itself. A compiler produces
code in another language, while an interpreter executes the source directly.

**Confirm:** Add the team’s actual “fun” regular expression, the text it
matched, and the conclusion the team reported to the class.

### Work at home for the assignment

I connected the week’s pipeline to Assignment 1. The river language would be a
domain-specific little language: its scanner would convert source characters
to tokens and its parser would turn those tokens into a tree representing the
river system. This helped establish why the first submission focuses on
scanning, grammar, parsing, and AST output rather than evaluation.

I also reviewed the Chapter 1 self-study questions about little languages and
the implementation pipeline. As an initial design note, I identified that the
river language would need notation for root rivers, combining upstream flows,
and selecting the final output river.

**Confirm:** Keep only the reading, exercises, and assignment notes I actually
completed at home, and add links or filenames if any Week 2 notes were saved.

## Week 3 — Lox and Programs that Generate Programs

**Date:** _Add the actual class date._

### Preparation before class

I reviewed the main features of the Lox language, including dynamic typing,
variables, expressions, control flow, functions, recursion, classes, and
garbage collection. The preparation distinguished compile time from run time:
compile time is when source is translated, whereas run time is when the
resulting program executes. I also noted that dynamic typing allows a variable
to hold values of different types during execution, but type errors may only
be detected at run time.

The self-study material used short Lox exercises to practise `print`, variable
declarations, loops, functions, and recursion. It also raised language-design
questions, such as whether a simpler and more general feature is necessarily
easier for users and which missing features would make a small language
inconvenient in practice.

**Confirm:** Record which Week 3 reading, readiness questions, and self-study
exercises I actually completed before class.

### Activities during class

After the readiness activities, the application exercise explored programs
that generate other programs. The team used Lox as the source language to
produce Logo turtle-graphics commands. The generated Logo program was limited
to `clearscreen`, `fd n`, `rt n`, `pu`, and `pd`, so repeated drawing commands
needed to be produced by loops or functions in the Lox generator rather than
written out manually.

This exercise clarified the difference between the program being executed and
the program being generated. The Lox program controlled the generation
process, while its text output became a separate Logo program interpreted by
the online turtle environment. It demonstrated why generated source code is a
useful intermediate result in language implementation.

**Confirm:** Add what shape my team generated, the Lox construct I contributed,
and whether our Lox and generated Logo programs were shared with the class.

### Work at home for the assignment

I related the exercise to the river-language parser. A parser also transforms
one representation into another: it receives a flat token stream and builds a
nested AST. This encouraged me to make the Assignment 1 result visible with an
AST printer rather than only reporting that parsing succeeded.

I considered which concepts needed dedicated syntax-tree nodes. Root rivers,
derived rivers, and the output location would become statement nodes, while
daily flow values, river names, combinations, and transformations would become
expression nodes. I also considered concise domain operations such as
`delay(flow, days)` instead of expanding every delayed daily value manually.

**Confirm:** Keep only the AST, grammar, coding, or exercise work actually
completed during Week 3 and add filenames or notes that provide evidence.

## Week 4

**Date:** _To be provided._

### Preparation before class

_To be written from the Week 4 material and my actual preparation._

### Activities during class

_To be written from the Week 4 material and my actual class notes._

### Work at home for the assignment

_To be written from my actual Assignment 1 work._

## Week 5

**Date:** _To be provided._

### Preparation before class

_To be written from the Week 5 material and my actual preparation._

### Activities during class

_To be written from the Week 5 material and my actual class notes._

### Work at home for the assignment

_To be written from my actual Assignment 1 work._

## Week 6

**Date:** _To be provided._

### Preparation before class

_To be written from the Week 6 material and my actual preparation._

### Activities during class

_To be written from the Week 6 material and my actual class notes._

### Work at home for the assignment

_To be written from my actual Assignment 1 work._

## Week 7

**Date:** _To be provided._

### Preparation before class

_To be written from the Week 7 material and my actual preparation._

### Activities during class

_To be written from the Week 7 material and my actual class notes._

### Work at home for the assignment

_To be written from my actual Assignment 1 work._

## Week 8

**Date:** _To be provided._

### Preparation before class

_To be written from the Week 8 material and my actual preparation._

### Activities during class

_To be written from the Week 8 material and my actual class notes._

### Work at home for the assignment

_To be written from my actual Assignment 1 work._
