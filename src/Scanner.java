import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lox-style scanner adapted from Chapter 4 of Crafting Interpreters.
 */
final class Scanner {
  // Words in this map are keywords instead of ordinary identifiers.
  private static final Map<String, TokenType> KEYWORDS = new HashMap<>();

  static {
    KEYWORDS.put("source", TokenType.SOURCE);
    KEYWORDS.put("river", TokenType.RIVER);
    KEYWORDS.put("outlet", TokenType.OUTLET);
  }

  private final String source;
  private final List<Token> tokens = new ArrayList<>();
  private int start = 0;
  private int current = 0;
  private int line = 1;

  Scanner(String source) {
    this.source = source;
  }

  // Scan the entire source file and finish with an EOF token.
  List<Token> scanTokens() {
    while (!isAtEnd()) {
      start = current;
      scanToken();
    }

    tokens.add(new Token(TokenType.EOF, "", null, line));
    return tokens;
  }

  // Read one character and decide which token starts there.
  private void scanToken() {
    char c = advance();
    switch (c) {
      case '(' -> addToken(TokenType.LEFT_PAREN);
      case ')' -> addToken(TokenType.RIGHT_PAREN);
      case '[' -> addToken(TokenType.LEFT_BRACKET);
      case ']' -> addToken(TokenType.RIGHT_BRACKET);
      case ',' -> addToken(TokenType.COMMA);
      case '-' -> addToken(TokenType.MINUS);
      case '+' -> addToken(TokenType.PLUS);
      case ';' -> addToken(TokenType.SEMICOLON);
      case '*' -> addToken(TokenType.STAR);
      case '=' -> addToken(TokenType.EQUAL);
      case '/' -> {
        if (match('/')) {
          while (peek() != '\n' && !isAtEnd()) advance();
        } else {
          addToken(TokenType.SLASH);
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

  // Continue through a name, then check whether it is a keyword.
  private void identifier() {
    while (isAlphaNumeric(peek())) advance();

    String text = source.substring(start, current);
    addToken(KEYWORDS.getOrDefault(text, TokenType.IDENTIFIER));
  }

  // Read an integer or decimal number and store its numeric value.
  private void number() {
    while (isDigit(peek())) advance();

    if (peek() == '.' && isDigit(peekNext())) {
      advance();
      while (isDigit(peek())) advance();
    }

    addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
  }

  // Consume an expected second character without going past the source.
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

  // Add a token that does not need a separate literal value.
  private void addToken(TokenType type) {
    addToken(type, null);
  }

  // Save the token text, optional value, and source line.
  private void addToken(TokenType type, Object literal) {
    tokens.add(new Token(type, source.substring(start, current), literal, line));
  }
}
