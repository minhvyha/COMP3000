package edu.mq.comp3000.river;

/**
 * Base class for the declarations that make up a river program.
 */
abstract class Stmt {
  // A visitor performs an operation on each kind of statement.
  interface Visitor<R> {
    R visitOutletStmt(Outlet stmt);
    R visitRiverStmt(River stmt);
    R visitSourceStmt(Source stmt);
  }

  abstract <R> R accept(Visitor<R> visitor);

  // Selects the final river whose flow leaves the system.
  static final class Outlet extends Stmt {
    final Token name;

    Outlet(Token name) {
      this.name = name;
    }

    @Override <R> R accept(Visitor<R> visitor) {
      return visitor.visitOutletStmt(this);
    }
  }

  // Names a downstream river and stores the expression that supplies its flow.
  static final class River extends Stmt {
    final Token name;
    final Expr flow;

    River(Token name, Expr flow) {
      this.name = name;
      this.flow = flow;
    }

    @Override <R> R accept(Visitor<R> visitor) {
      return visitor.visitRiverStmt(this);
    }
  }

  // Names a root river and stores its starting flow expression.
  static final class Source extends Stmt {
    final Token name;
    final Expr flow;

    Source(Token name, Expr flow) {
      this.name = name;
      this.flow = flow;
    }

    @Override <R> R accept(Visitor<R> visitor) {
      return visitor.visitSourceStmt(this);
    }
  }
}
