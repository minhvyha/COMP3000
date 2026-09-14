package edu.mq.comp3000.river;

import java.util.List;

abstract class Expr {
  interface Visitor<R> {
    R visitBinaryExpr(Binary expr);
    R visitCallExpr(Call expr);
    R visitFlowExpr(Flow expr);
    R visitGroupingExpr(Grouping expr);
    R visitLiteralExpr(Literal expr);
    R visitUnaryExpr(Unary expr);
    R visitVariableExpr(Variable expr);
  }

  abstract <R> R accept(Visitor<R> visitor);

  static final class Binary extends Expr {
    final Expr left;
    final Token operator;
    final Expr right;

    Binary(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override <R> R accept(Visitor<R> visitor) {
      return visitor.visitBinaryExpr(this);
    }
  }

  static final class Call extends Expr {
    final Token name;
    final List<Expr> arguments;

    Call(Token name, List<Expr> arguments) {
      this.name = name;
      this.arguments = arguments;
    }

    @Override <R> R accept(Visitor<R> visitor) {
      return visitor.visitCallExpr(this);
    }
  }

  /** A flow over consecutive days, for example [4, 3, 2, 1]. */
  static final class Flow extends Expr {
    final List<Double> days;

    Flow(List<Double> days) {
      this.days = days;
    }

    @Override <R> R accept(Visitor<R> visitor) {
      return visitor.visitFlowExpr(this);
    }
  }

  static final class Grouping extends Expr {
    final Expr expression;

    Grouping(Expr expression) {
      this.expression = expression;
    }

    @Override <R> R accept(Visitor<R> visitor) {
      return visitor.visitGroupingExpr(this);
    }
  }

  static final class Literal extends Expr {
    final Object value;

    Literal(Object value) {
      this.value = value;
    }

    @Override <R> R accept(Visitor<R> visitor) {
      return visitor.visitLiteralExpr(this);
    }
  }

  static final class Unary extends Expr {
    final Token operator;
    final Expr right;

    Unary(Token operator, Expr right) {
      this.operator = operator;
      this.right = right;
    }

    @Override <R> R accept(Visitor<R> visitor) {
      return visitor.visitUnaryExpr(this);
    }
  }

  static final class Variable extends Expr {
    final Token name;

    Variable(Token name) {
      this.name = name;
    }

    @Override <R> R accept(Visitor<R> visitor) {
      return visitor.visitVariableExpr(this);
    }
  }
}
