package edu.mq.comp3000.river;

import java.util.ArrayList;
import java.util.List;

import static edu.mq.comp3000.river.TokenType.*;

/**
 * Recursive-descent parser following the structure of Chapter 6 of
 * Crafting Interpreters.
 */
final class Parser {
  private static final class ParseError extends RuntimeException {}

  private final List<Token> tokens;
  private int current = 0;

  Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  // Parse declarations until the scanner's EOF token is reached.
  List<Stmt> parse() {
    List<Stmt> statements = new ArrayList<>();
    while (!isAtEnd()) {
      Stmt statement = declaration();
      if (statement != null) statements.add(statement);
    }
    return statements;
  }

  // Choose a declaration rule from its first keyword.
  private Stmt declaration() {
    try {
      if (match(SOURCE)) return sourceDeclaration();
      if (match(RIVER)) return riverDeclaration();
      if (match(OUTLET)) return outletDeclaration();

      throw error(peek(), "Expected 'source', 'river', or 'outlet'.");
    } catch (ParseError error) {
      synchronize();
      return null;
    }
  }

  // source name = flow;
  private Stmt sourceDeclaration() {
    Token name = consume(IDENTIFIER, "Expected source name.");
    consume(EQUAL, "Expected '=' after source name.");
    Expr flow = expression();
    consume(SEMICOLON, "Expected ';' after source declaration.");
    return new Stmt.Source(name, flow);
  }

  // river name = flow;
  private Stmt riverDeclaration() {
    Token name = consume(IDENTIFIER, "Expected river name.");
    consume(EQUAL, "Expected '=' after river name.");
    Expr flow = expression();
    consume(SEMICOLON, "Expected ';' after river declaration.");
    return new Stmt.River(name, flow);
  }

  // outlet name;
  private Stmt outletDeclaration() {
    Token name = consume(IDENTIFIER, "Expected outlet river name.");
    consume(SEMICOLON, "Expected ';' after outlet declaration.");
    return new Stmt.Outlet(name);
  }

  private Expr expression() {
    return term();
  }

  // Addition and subtraction have lower precedence than multiplication.
  private Expr term() {
    Expr expr = factor();

    while (match(MINUS, PLUS)) {
      Token operator = previous();
      Expr right = factor();
      expr = new Expr.Binary(expr, operator, right);
    }
    return expr;
  }

  // Multiplication and division bind more tightly than + and -.
  private Expr factor() {
    Expr expr = unary();

    while (match(SLASH, STAR)) {
      Token operator = previous();
      Expr right = unary();
      expr = new Expr.Binary(expr, operator, right);
    }
    return expr;
  }

  // Parse a leading minus before parsing basic expressions.
  private Expr unary() {
    if (match(MINUS)) {
      return new Expr.Unary(previous(), unary());
    }
    return primary();
  }

  // Parse numbers, flow lists, names, calls, and grouped expressions.
  private Expr primary() {
    if (match(NUMBER)) return new Expr.Literal(previous().literal);
    if (match(LEFT_BRACKET)) return flowLiteral();

    if (match(IDENTIFIER)) {
      Token name = previous();
      if (match(LEFT_PAREN)) return finishCall(name);
      return new Expr.Variable(name);
    }

    if (match(LEFT_PAREN)) {
      Expr expr = expression();
      consume(RIGHT_PAREN, "Expected ')' after expression.");
      return new Expr.Grouping(expr);
    }

    throw error(peek(), "Expected a flow expression.");
  }

  // Parse the comma-separated arguments after an operation name.
  private Expr finishCall(Token name) {
    List<Expr> arguments = new ArrayList<>();
    if (!check(RIGHT_PAREN)) {
      do {
        if (arguments.size() >= 8) {
          throw error(peek(), "A flow operation supports at most 8 arguments.");
        }
        arguments.add(expression());
      } while (match(COMMA));
    }
    consume(RIGHT_PAREN, "Expected ')' after arguments.");
    return new Expr.Call(name, arguments);
  }

  // Parse daily values between [ and ].
  private Expr flowLiteral() {
    List<Double> days = new ArrayList<>();
    if (!check(RIGHT_BRACKET)) {
      do {
        Token value = consume(NUMBER, "Expected a numeric daily flow.");
        days.add((Double) value.literal);
      } while (match(COMMA));
    }
    consume(RIGHT_BRACKET, "Expected ']' after flow literal.");
    return new Expr.Flow(days);
  }

  // Consume the next token if it is one of the requested types.
  private boolean match(TokenType... types) {
    for (TokenType type : types) {
      if (check(type)) {
        advance();
        return true;
      }
    }
    return false;
  }

  // Require one token type or report a useful syntax error.
  private Token consume(TokenType type, String message) {
    if (check(type)) return advance();
    throw error(peek(), message);
  }

  private boolean check(TokenType type) {
    if (isAtEnd()) return type == EOF;
    return peek().type == type;
  }

  private Token advance() {
    if (!isAtEnd()) current++;
    return previous();
  }

  private boolean isAtEnd() {
    return peek().type == EOF;
  }

  private Token peek() {
    return tokens.get(current);
  }

  private Token previous() {
    return tokens.get(current - 1);
  }

  private ParseError error(Token token, String message) {
    RiverLox.error(token, message);
    return new ParseError();
  }

  /**
   * Continue after a malformed declaration so one error does not hide later
   * errors. This is the Chapter 6 parser's panic-mode recovery strategy.
   */
  private void synchronize() {
    advance();
    while (!isAtEnd()) {
      if (previous().type == SEMICOLON) return;
      if (check(SOURCE) || check(RIVER) || check(OUTLET)) return;
      advance();
    }
  }
}
