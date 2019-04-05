import maquinaVirtual.MaquinaVirtual;

import util.Mensagem;

import java.io.FileNotFoundException;

class MainMaquinaVirtual {
  public static void main(String[] args) {
    Mensagem.printDebug = false;
    String fonte = "source.scp";
    try {
      new MaquinaVirtual(fonte);
    } catch (FileNotFoundException e) {
      Mensagem.abort("Arquivo '%s' não encontrado", fonte);
    }
  }
}
