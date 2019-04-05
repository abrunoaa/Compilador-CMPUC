package util;

public class Mensagem {
  static public boolean printDebug = true;
  final private static String RED = (char)27 + "[91m";
  final private static String NO_COLOR = (char)27 + "[0m";

  private static void debug(int depth, String format, Object... out) {
    System.err.printf(RED); // imprime na cor vermelha
    StackTraceElement s = new Exception().getStackTrace()[depth];
    System.err.printf("Linha " + s.getLineNumber() + " em " + s.getClassName() + "." + s.getMethodName() + "():\n");
    System.err.printf(" > " + format + "\n", out); // util
    System.err.printf(NO_COLOR);  // remove a cor vermelha
  }

  public static void print(String format, Object... out) {
    System.out.printf(format, out);
  }

  public static void debug(String format, Object... out) {
    if (printDebug) {
      debug(2, format, out);
    }
  }

  public static void abort() {
    System.exit(0);
  }

  public static void abort(String format, Object... out) {
    debug(2, format, out);
    abort();
  }
}
