package compilador;

class Token {
  public enum Tipo { ATRIB, OPR_AR, DELIM, ID, NUM, ERRO, FIM }

  final public static Token TOKEN_FINAL = new Token(Tipo.FIM, "fim");
  
  final public int linha;
  final public Tipo tipo;
  final public String valor;

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
    return "(" + linha + ": " + tipo.name() + (valor == null ? "" : ", '" + valor + "'") + ")";
  }
}
