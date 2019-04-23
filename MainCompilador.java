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
    try {
      System.setOut(new PrintStream(new FileOutputStream(dest)));
    } catch(FileNotFoundException e) {
      Mensagem.abort("Não foi possível abrir o arquivo '%s'.\n", dest);
    }
    try {
      Mensagem.debug("Iniciando compilador\n");
      new Compilador(fonte);
      Mensagem.debug("Sucesso!\n");
    } catch (Exception e) {
      Mensagem.abort(Mensagem.getStackTrace(e));
    }
  }
}
