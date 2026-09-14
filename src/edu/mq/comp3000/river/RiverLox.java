package edu.mq.comp3000.river;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class RiverLox {
  private static boolean hadError = false;

  private RiverLox() {}

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.err.println("Usage: java edu.mq.comp3000.river.RiverLox <program.river>");
      System.exit(64);
    }

    String source = Files.readString(Path.of(args[0]), StandardCharsets.UTF_8);
    run(source);
    if (hadError) System.exit(65);
  }

  private static void run(String source) {
    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.scanTokens();

    Parser parser = new Parser(tokens);
    List<Stmt> statements = parser.parse();

    if (hadError) return;
    System.out.println(new AstPrinter().print(statements));
  }

  static void error(int line, String message) {
    report(line, "", message);
  }

  static void error(Token token, String message) {
    String where = token.type == TokenType.EOF
        ? " at end"
        : " at '" + token.lexeme + "'";
    report(token.line, where, message);
  }

  private static void report(int line, String where, String message) {
    System.err.println("[line " + line + "] Error" + where + ": " + message);
    hadError = true;
  }
}
