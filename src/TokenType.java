/**
 * Every token category understood by the scanner and parser.
 */
enum TokenType {
  // Single-character tokens.
  LEFT_PAREN, RIGHT_PAREN, LEFT_BRACKET, RIGHT_BRACKET,
  COMMA, MINUS, PLUS, SEMICOLON, SLASH, STAR, EQUAL,

  // Literals.
  IDENTIFIER, NUMBER,

  // Keywords.
  SOURCE, RIVER, OUTLET,

  // Marks the end of the source file.
  EOF
}
