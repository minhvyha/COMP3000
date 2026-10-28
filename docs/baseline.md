# A1 baseline and in-place A2 development

Submission 2 is now developed directly in this repository at
`/Users/salvi/MQ/COMP3000`, using the existing `src/`, `examples/`, README, and
Git history. The separate working copy used during the first semantics step
has been retired after transferring and verifying its changes here.

The user reported 100% for Submission 1. The starting Git revision was
`3bcc870420c9d87ea35f8bd40691435eb80e3d87`, with a clean working tree before
these A2 additions. That revision preserves the prior tracked code and README.
No new commit or branch was created for this migration.

The original eight Java files and three `.river` examples remain unchanged
at this milestone. Their fingerprints are recorded in `a1-baseline.sha256`.
This protects accepted parser behaviour while new model classes are added
alongside the existing source. Confirm the actual submitted zip against the
baseline if needed; the reported grade is not being re-evaluated here.

`tests/expected/` holds syntax-tree output captured from the A1 source.
The test runner checks those outputs in this same repository.

The original A1 files are `AstPrinter.java`, `Expr.java`, `Parser.java`,
`RiverLox.java`, `Scanner.java`, `Stmt.java`, `Token.java`, and `TokenType.java`,
plus the Canberra, Macquarie creeks, and alpine snowmelt programs.

`a1_rubric.md` and the historical `LOGBOOK.md` remain in place and unchanged.
