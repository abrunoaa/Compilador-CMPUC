package compilador;

import util.Codigo;
import util.Mensagem;

import compilador.Token;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;

class Lexer {
  // operação com símbolo que não é prefixo de outro
  final static Map<Character, Token> simbolo;

  // palavras reservadas
  final static Map<String, Token> keyword;

  static {
    simbolo = new HashMap<Character, Token>();
    keyword = new HashMap<String, Token>();

    // OPR_AR
    simbolo.put('+', new Token(-1, Token.Tipo.OPR_AR, "+"));
    simbolo.put('*', new Token(-1, Token.Tipo.OPR_AR, "*"));
    simbolo.put('/', new Token(-1, Token.Tipo.OPR_AR, "/"));
    simbolo.put('%', new Token(-1, Token.Tipo.OPR_AR, "%"));
    simbolo.put('^', new Token(-1, Token.Tipo.OPR_AR, "^"));

    simbolo.put('=', new Token(-1, Token.Tipo.ATRIB, "="));

    // DELIM
    simbolo.put('(', new Token(-1, Token.Tipo.DELIM, "("));
    simbolo.put(')', new Token(-1, Token.Tipo.DELIM, ")"));
    simbolo.put(';', new Token(-1, Token.Tipo.DELIM, ";"));

    // keywords
    keyword.put("escreva", new Token(-1, Token.Tipo.ID, "escreva"));
    keyword.put("leia", new Token(-1, Token.Tipo.ID, "leia"));
  }

  private Codigo codigo;
  private boolean temOperando;

  public Lexer(Codigo codigo) {
    assert codigo != null : "Unexpected null";
    this.codigo = codigo;
    temOperando = false;
  }

  // cria um token, atualizando o valor de 'temOperando'
  private Token criaToken(int linha, Token token) {
    temOperando = token.tipo == Token.Tipo.ID || token.tipo == Token.Tipo.NUM;
    return new Token(linha, token.tipo, token.valor);
  }

  private Token criaToken(int linha, Token.Tipo tipo) {
    temOperando = tipo == Token.Tipo.ID || tipo == Token.Tipo.NUM;
    return new Token(linha, tipo);
  }

  private Token criaToken(int linha, Token.Tipo tipo, String valor) {
    temOperando = tipo == Token.Tipo.ID || tipo == Token.Tipo.NUM;
    return new Token(linha, tipo, valor);
  }

  private Token criaErro(int linha, String valor) {
    temOperando = false;
    return new Token(linha, Token.Tipo.ERRO, valor);
  }

  // lê um token
  // >> retorna null se não existem mais tokens
  private Token lerToken() {
    // ignora espaços em branco
    while (!codigo.fim() && Character.isWhitespace(codigo.get())) {
      codigo.avanca();
    }
    if (codigo.fim()) {
      return null;
    }

    // linha no código
    int linha = codigo.linha();
    
    // checa por operadores
    if (simbolo.containsKey(codigo.get())) {
      Token t = simbolo.get(codigo.get());
      codigo.avanca();
      return criaToken(linha, t);
    }

    // caso especial para '-': valor - valor
    if (temOperando && codigo.get() == '-') {
      codigo.avanca();
      return criaToken(linha, Token.Tipo.OPR_AR, "-");
    }

    StringBuilder buf = new StringBuilder();

    // checa se é um ID / keyword
    if (Character.isLetter(codigo.get())) {
      do {
        buf.append(codigo.get());
        codigo.avanca();
      } while (!codigo.fim() && Character.isLetterOrDigit(codigo.get()));

      String bufStr = buf.toString();
      if (keyword.containsKey(bufStr)) {
        return criaToken(linha, keyword.get(bufStr));
      }
      return criaToken(linha, Token.Tipo.ID, bufStr);
    }

    /***** se chegou aqui, é um número ou um caracter inválido *****/

    // p/ números negativos
    if (codigo.get() == '-') {
      // caso especial quando não há um número
      Mensagem.debug("buf = '%s'\n", buf.toString());
      codigo.avanca();
      if (codigo.fim() || !Character.isDigit(codigo.get())) {
        return criaToken(linha, Token.Tipo.OPR_AR, "-");
      }
      buf.append('-');
    }
    boolean real = false;                            // flag se o número já tem um ponto
    for (int i = 0; i < 2; ++i) {
      // checa se o número / caracter é inválido
      if (!Character.isDigit(codigo.get())) {
        if (buf.length() == 0) {
          buf.append(codigo.get());
          codigo.avanca();
        }
        return criaErro(linha, buf.toString());
      }

      do {
        buf.append(codigo.get());
        codigo.avanca();
      } while (!codigo.fim() && Character.isDigit(codigo.get()));

      // se não leu um ponto ou já tinha, interrompe a leitura
      if (codigo.fim() || real || codigo.get() != '.') {
        break;
      }
      buf.append('.');
      codigo.avanca();
      real = true;
    }

    return criaToken(linha, Token.Tipo.NUM, buf.toString());
  }

  // lê todos os tokens do código
  public List<Token> leiaTokens() {
    List<Token> tokens = new LinkedList<Token>();
    Token t = lerToken();
    while (t != null) {
      tokens.add(t);
      t = lerToken();
    }
    return tokens;
  }
}
