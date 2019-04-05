package maquinaVirtual;

class Token {
  public enum Tipo { OPR1, OPR2, OPR3, REG, NUM }

  final int linha;
  final Tipo tipo;
  final String valor;

  Token(int linha, Tipo tipo) {
    this.linha = linha;
    this.tipo = tipo;
    this.valor = null;
  }

  Token(int linha, Tipo tipo, String valor) {
    this.linha = linha;
    this.tipo = tipo;
    this.valor = valor;
  }

  public String toString() {
    return "(" + linha + ", " + tipo.name() + (valor == null ? "" : ", '" + valor + "'") + ")";
  }
}
