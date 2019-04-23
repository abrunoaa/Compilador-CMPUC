package compilador;

class Token {
  public enum Tipo { ATRIB, OPR_AR, DELIM, ID, NUM, ERRO, FIM }

  final public static Token TOKEN_FINAL = new Token(Tipo.FIM, "fim");
  final public Tipo tipo;
  final public String valor;

  Token(Tipo tipo) {
    this.tipo = tipo;
    this.valor = null;
  }

  Token(Tipo tipo, String valor) {
    this.tipo = tipo;
    this.valor = valor;
  }

  public String toString() {
    return "(" + tipo.name() + (valor == null ? "" : ", '" + valor + "'") + ")";
  }
}
