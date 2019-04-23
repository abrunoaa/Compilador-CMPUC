import compilador.Compilador;

import util.Mensagem;

class MainCompilador {
  public static void main(String[] args) {
    Mensagem.printDebug = true;
    String fonte = "source.cmp";
    System.setOut(new PrintStream(new FileOutputStream(OUTPUT)));
    try {
      Mensagem.debug("Iniciando compilador\n");
      new Compilador(fonte);
      Mensagem.debug("Execução encerrada\n");
    } catch (Exception e) {
      Mensagem.abort("Exceção inesperada: '%s'\n", e);
    }
  }
}
