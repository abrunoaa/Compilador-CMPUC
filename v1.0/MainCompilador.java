import compilador.Compilador;

import util.Mensagem;

import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;

class MainCompilador {
  public static void main(String[] args) {
    Mensagem.printDebug = true;

    String fonte = "source.cmp";
    String dest = "source.scp";

    Mensagem.debug("Iniciando compilador\n");
    try {
      Compilador compilador = new Compilador(fonte);
      String assembly = compilador.geraAssembly();

      System.setOut(new PrintStream(new FileOutputStream(dest)));
      System.out.printf(assembly);
    } catch (Exception e) {
      Mensagem.abort(Mensagem.getStackTrace(e));
    }

    Mensagem.debug("Sucesso!\n");
  }
}
