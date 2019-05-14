import maquinaVirtual.MaquinaVirtual;

import util.Mensagem;

class MainMaquinaVirtual {
  public static void main(String[] args) {
    Mensagem.printDebug = false;
    String fonte = "source.scp";
    try {
      Mensagem.debug("Iniciando máquina virtual\n");
      new MaquinaVirtual(fonte);
      Mensagem.debug("Execução encerrada\n");
    } catch (Exception e) {
      Mensagem.abort("Exceção inesperada: '%s'\n%s\n", e, Mensagem.getStackTrace(e));
    }
  }
}
