package edu.mq.comp3000.river;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.mq.comp3000.river.TokenType.*;

/**
 * Lox-style scanner adapted from Chapter 4 of Crafting Interpreters.
 */
final class Scanner {
  private static final Map<String, TokenType> KEYWORDS = new HashMap<>();

  static {
    KEYWORDS.put("source", SOURCE);
    KEYWORDS.put("river", RIVER);
    KEYWORDS.put("outlet", OUTLET);
  }

  private final String source;
  private final List<Token> tokens = new ArrayList<>();
  private int start = 0;
  private int current = 0;
  private int line = 1;

  Scanner(String source) {
    this.source = source;
  }

  List<Token> scanTokens() {
    while (!isAtEnd()) {
      start = current;
      scanToken();
    }

    tokens.add(new Token(EOF, "", null, line));
    return tokens;
  }

  private void scanToken() {
    char c = advance();
    switch (c) {
      case '(' -> addToken(LEFT_PAREN);
      case ')' -> addToken(RIGHT_PAREN);
      case '[' -> addToken(LEFT_BRACKET);
      case ']' -> addToken(RIGHT_BRACKET);
      case ',' -> addToken(COMMA);
      case '-' -> addToken(MINUS);
      case '+' -> addToken(PLUS);
      case ';' -> addToken(SEMICOLON);
      case '*' -> addToken(STAR);
      case '=' -> addToken(EQUAL);
      case '/' -> {
        if (match('/')) {
          while (peek() != '\n' && !isAtEnd()) advance();
        } else {
          addToken(SLASH);
        }
      }
      case ' ', '\r', '\t' -> {
        // Ignore horizontal whitespace.
      }
      case '\n' -> line++;
      default -> {
        if (isDigit(c)) {
          number();
        } else if (isAlpha(c)) {
          identifier();
        } else {
          RiverLox.error(line, "Unexpected character '" + c + "'.");
        }
      }
    }
  }

  private void identifier() {
    while (isAlphaNumeric(peek())) advance();

    String text = source.substring(start, current);
    addToken(KEYWORDS.getOrDefault(text, IDENTIFIER));
  }

  private void number() {
    while (isDigit(peek())) advance();

    if (peek() == '.' && isDigit(peekNext())) {
      advance();
      while (isDigit(peek())) advance();
    }

    addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
  }

  private boolean match(char expected) {
    if (isAtEnd() || source.charAt(current) != expected) return false;
    current++;
    return true;
  }

  private char peek() {
    return isAtEnd() ? '\0' : source.charAt(current);
  }

  private char peekNext() {
    return current + 1 >= source.length() ? '\0' : source.charAt(current + 1);
  }

  private boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z')
        || (c >= 'A' && c <= 'Z')
        || c == '_';
  }

  private boolean isAlphaNumeric(char c) {
    return isAlpha(c) || isDigit(c);
  }

  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  private boolean isAtEnd() {
    return current >= source.length();
  }

  private char advance() {
    return source.charAt(current++);
  }

  private void addToken(TokenType type) {
    addToken(type, null);
  }

  private void addToken(TokenType type, Object literal) {
    tokens.add(new Token(type, source.substring(start, current), literal, line));
  }
}
