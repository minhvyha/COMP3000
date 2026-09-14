package edu.mq.comp3000.river;

enum TokenType {
  // Single-character tokens.
  LEFT_PAREN, RIGHT_PAREN, LEFT_BRACKET, RIGHT_BRACKET,
  COMMA, MINUS, PLUS, SEMICOLON, SLASH, STAR, EQUAL,

  // Literals.
  IDENTIFIER, NUMBER,

  // Keywords.
  SOURCE, RIVER, OUTLET,

  EOF
}
