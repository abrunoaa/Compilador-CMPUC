package util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Mensagem {
  static public boolean printDebug = false;

  final private static String RED = (char)27 + "[91m";
  final private static String NO_COLOR = (char)27 + "[0m";

  private static void debug(boolean show, boolean stackTrace, String format, Object... out) {
    if (!show) {
      return;
    }

    System.err.printf(RED); // imprime na cor vermelha
    StackTraceElement s = new Exception().getStackTrace()[2];
    if (stackTrace) {
      System.err.printf("Linha " + s.getLineNumber() + " em " + s.getClassName() + "." + s.getMethodName() + "(): ");
    }
    System.err.printf(format, out); // util
    System.err.printf(NO_COLOR);  // remove a cor vermelha
  }

  public static void print(String format, Object... out) {
    System.out.printf(format, out);
  }

  public static void debug(String format, Object... out) {
    debug(printDebug, true, format, out);
  }

  public static void abort() {
    debug(true, printDebug, "Abortando.\n");
    System.exit(0);
  }

  public static void abort(String format, Object... out) {
    debug(true, false, format, out);
    abort();
  }

  public static String getStackTrace(Exception exception) {
    StringWriter writer = new StringWriter();
    PrintWriter printWriter = new PrintWriter(writer);
    exception.printStackTrace(printWriter);
    printWriter.flush();
    return writer.toString();
  }
}
