package maquinaVirtual;

import util.Mensagem;

import maquinaVirtual.MaquinaVirtual;
import util.Codigo;
import maquinaVirtual.Token;
import maquinaVirtual.Parser;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;

class Lexer {
  final private Codigo codigo;

  Lexer(Codigo codigo) {
    this.codigo = codigo;
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
    if (!Character.isLetterOrDigit(codigo.get())) {
      Mensagem.abort("Linha %d: Símbolo desconhecido: %c", codigo.linha(), codigo.get());
    }

    StringBuilder buf = new StringBuilder();
    boolean isNumber = true;
    while (!codigo.fim() && Character.isLetterOrDigit(codigo.get())) {
      buf.append(codigo.get());
      isNumber &= Character.isDigit(codigo.get());
      codigo.avanca();
    }

    String s = buf.toString();
    Mensagem.debug("num? %s, '%s'", isNumber, s);
    if (isNumber) {
      return new Token(codigo.linha(), Token.Tipo.NUM, s);
    }
    if (MaquinaVirtual.operacoes1.contains(s)) {
      return new Token(codigo.linha(), Token.Tipo.OPR1, s);
    }
    if (MaquinaVirtual.operacoes2.contains(s)) {
      return new Token(codigo.linha(), Token.Tipo.OPR2, s);
    }
    if (MaquinaVirtual.operacoes3.contains(s)) {
      return new Token(codigo.linha(), Token.Tipo.OPR3, s);
    }
    if (!MaquinaVirtual.registradores.contains(s)) {
      Mensagem.abort("Linha %d: Operação desconhecida: %s", codigo.linha(), s);
    }
    return new Token(codigo.linha(), Token.Tipo.REG, s);
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
