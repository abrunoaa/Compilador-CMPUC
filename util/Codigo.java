package util;

import java.util.Scanner;

public class Codigo {
  private int linha;
  private int posicao;
  private final String codigo;

  // lê um código até EOF
  public Codigo(Scanner in) {
    StringBuilder codigo = new StringBuilder();
    while (in.hasNext()) {
      codigo.append(in.nextLine());
      codigo.append('\n');
    }
    this.codigo = codigo.toString();
  }

  // checa se ainda tem símbolos de entrada
  public boolean fim() {
    return posicao == codigo.length();
  }

  // avança para o próximo símbolo
  public void avanca() {
    assert !fim() : "Fim inesperado";
    if (get() == '\n') {
      ++linha;
    }
    ++posicao;
  }

  public int linha() {
    return linha;
  }

  // retorna o símbolo atual
  public char get() {
    return codigo.charAt(posicao);
  }
}
