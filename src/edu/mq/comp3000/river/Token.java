package edu.mq.comp3000.river;

/**
 * One meaningful piece of source code produced by the scanner.
 */
final class Token {
  // Category, original text, parsed value, and location of the token.
  final TokenType type;
  final String lexeme;
  final Object literal;
  final int line;

  Token(TokenType type, String lexeme, Object literal, int line) {
    this.type = type;
    this.lexeme = lexeme;
    this.literal = literal;
    this.line = line;
  }

  @Override
  public String toString() {
    return type + " " + lexeme + " " + literal;
  }
}
