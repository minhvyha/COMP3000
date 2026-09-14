package edu.mq.comp3000.river;

abstract class Stmt {
  interface Visitor<R> {
    R visitOutletStmt(Outlet stmt);
    R visitRiverStmt(River stmt);
    R visitSourceStmt(Source stmt);
  }

  abstract <R> R accept(Visitor<R> visitor);

  static final class Outlet extends Stmt {
    final Token name;

    Outlet(Token name) {
      this.name = name;
    }

    @Override <R> R accept(Visitor<R> visitor) {
      return visitor.visitOutletStmt(this);
    }
  }

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
