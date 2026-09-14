package edu.mq.comp3000.river;

import java.util.List;
import java.util.stream.Collectors;

final class AstPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {
  String print(List<Stmt> statements) {
    return statements.stream()
        .map(statement -> statement.accept(this))
        .collect(Collectors.joining("\n"));
  }

  @Override
  public String visitOutletStmt(Stmt.Outlet stmt) {
    return "(outlet " + stmt.name.lexeme + ")";
  }

  @Override
  public String visitRiverStmt(Stmt.River stmt) {
    return "(river " + stmt.name.lexeme + " " + stmt.flow.accept(this) + ")";
  }

  @Override
  public String visitSourceStmt(Stmt.Source stmt) {
    return "(source " + stmt.name.lexeme + " " + stmt.flow.accept(this) + ")";
  }

  @Override
  public String visitBinaryExpr(Expr.Binary expr) {
    return parenthesize(expr.operator.lexeme, expr.left, expr.right);
  }

  @Override
  public String visitCallExpr(Expr.Call expr) {
    String arguments = expr.arguments.stream()
        .map(argument -> argument.accept(this))
        .collect(Collectors.joining(" "));
    return "(" + expr.name.lexeme + (arguments.isEmpty() ? "" : " " + arguments) + ")";
  }

  @Override
  public String visitFlowExpr(Expr.Flow expr) {
    return expr.days.stream()
        .map(this::formatNumber)
        .collect(Collectors.joining(" ", "[flow ", "]"));
  }

  @Override
  public String visitGroupingExpr(Expr.Grouping expr) {
    return parenthesize("group", expr.expression);
  }

  @Override
  public String visitLiteralExpr(Expr.Literal expr) {
    return expr.value == null ? "nil" : formatNumber((Double) expr.value);
  }

  @Override
  public String visitUnaryExpr(Expr.Unary expr) {
    return parenthesize(expr.operator.lexeme, expr.right);
  }

  @Override
  public String visitVariableExpr(Expr.Variable expr) {
    return expr.name.lexeme;
  }

  private String parenthesize(String name, Expr... expressions) {
    String values = java.util.Arrays.stream(expressions)
        .map(expression -> expression.accept(this))
        .collect(Collectors.joining(" "));
    return "(" + name + " " + values + ")";
  }

  private String formatNumber(Double number) {
    if (number == Math.rint(number)) return Long.toString(number.longValue());
    return number.toString();
  }
}
