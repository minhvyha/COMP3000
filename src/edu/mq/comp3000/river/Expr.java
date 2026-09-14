package edu.mq.comp3000.river;

import java.util.List;

/**
 * Base class for every expression that can describe or transform a flow.
 */
abstract class Expr {
  // The visitor keeps operations such as printing separate from the AST data.
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

  // An expression with two operands, such as joes + mahers.
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

  // A named flow operation, such as delay(googong, 1).
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

  // A flow over consecutive days, for example [4, 3, 2, 1].
  static final class Flow extends Expr {
    final List<Double> days;

    Flow(List<Double> days) {
      this.days = days;
    }

    @Override <R> R accept(Visitor<R> visitor) {
      return visitor.visitFlowExpr(this);
    }
  }

  // An expression placed inside parentheses.
  static final class Grouping extends Expr {
    final Expr expression;

    Grouping(Expr expression) {
      this.expression = expression;
    }

    @Override <R> R accept(Visitor<R> visitor) {
      return visitor.visitGroupingExpr(this);
    }
  }

  // A single value, which is currently a number.
  static final class Literal extends Expr {
    final Object value;

    Literal(Object value) {
      this.value = value;
    }

    @Override <R> R accept(Visitor<R> visitor) {
      return visitor.visitLiteralExpr(this);
    }
  }

  // An expression with one operator, such as -5.
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

  // A reference to a previously named source or river.
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
