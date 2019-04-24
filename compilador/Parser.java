package compilador;

import compilador.Token;

import util.Mensagem;

import java.util.Iterator;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;

class Parser {
  // posição de memória das variáveis
  private Map<String,Integer> variaveis;
  
  // tokens do código
  private List<Token> tokens;
  
  // token sendo processado
  int posicao;

  public Parser(List<Token> tokens) {
    assert tokens != null : "Unexpected null";
    this.variaveis = new HashMap<String,Integer>();
    this.tokens = tokens;
  }

  private Token get(int k) {
    if (k >= tokens.size()) {
      return Token.TOKEN_FINAL;
    }
    return tokens.get(k);
  }

  // escreve o comando no arquivo
  private void escreva(String format, Object... out) {
    System.out.printf(format + "\n", out);
  }

  private void escrevaOperacao(String op) {
    System.out.println("pop r2");
    System.out.println("pop r1");
    System.out.printf("%s r0 r1 r2\n", op);
    System.out.println("push r0");
  }

  private int posVariavel(String var) {
    if (!variaveis.containsKey(var)) {
      variaveis.put(var, variaveis.size());
    }
    return variaveis.get(var);
  }

  // <programa> -> <listaDeIntrucoes>
  public void programa() {
    listaDeIntrucoes();
  }

  // <listaDeIntrucoes> -> <instrucao>; <listaDeIntrucoes> | E
  private void listaDeIntrucoes() {
    if (posicao < tokens.size()) {
      instrucao();
      if (get(posicao).tipo != Token.Tipo.DELIM || !get(posicao).valor.equals(";")) {
        Mensagem.abort("Espera-se um ';' após uma instrução\n");
      }
      ++posicao;
      listaDeIntrucoes();
    }
  }

  // <instrucao> -> { <atribuicao> | escreva <expressao> | leia ID }
  private void instrucao() {
    if (get(posicao).tipo != Token.Tipo.ID) {
      Mensagem.abort("Espera-se um identificador no início da instrução\n");
    }
    if (get(posicao).valor.equals("escreva")) {
      ++posicao;
      expressao();
      escreva("pop r0");
      escreva("write r0");
    }
    else if (get(posicao).valor.equals("leia")) {
      ++posicao;
      Token var = get(posicao);
      if (var.tipo != Token.Tipo.ID) {
        Mensagem.abort("Espera-se um identificador após 'leia'\n");
      }
      id();
      escreva("read r0");
      escreva("store r0 %d", posVariavel(var.valor));
    }
    else {
      atribuicao(index);
    }
  }

  // <id> -> ID | NUM
  private void id() {
    if (get(posicao).tipo == Token.Tipo.ID) {
      escreva("load r0 %d", posVariavel(get(posicao).valor));
      escreva("push r0");
    }
    else if (get(posicao).tipo == Token.Tipo.NUM) {
      escreva("move r0 %d", Long.valueOf(get(posicao).valor));
      escreva("push r0");
    }
    else {
      assert false : "id chamado com um não id";
    }
    ++posicao;
  }

  // <atribuicao> -> ID = <expressao>
  private void atribuicao() {
    int posVar = posVariavel(get(posicao).valor);
    if (get(posicao).tipo != Token.Tipo.ID) {
      Mensagem.abort("Espera-se um identificador no início da expressão\n");
    }
    if (get(posicao + 1).tipo != Token.Tipo.ATRIB) {
      Mensagem.abort("Espera-se uma atribuição '=' após o identificador\n");
    }
    posicao += 2;
    expressao();
    escreva("pop r0");
    escreva("store r0 %d", posVar);
  }

  // <expressao> -> <termo> <resto1>
  private int expressao(int index) {
    termo();
    resto1();
  }

  // <termo> -> <fator> <resto2>
  private void termo() {
    fator();
    resto2();
  }

  // <fator> -> (<expressao>) <resto3> | <id> <resto3>
  private void fator() {
    Token t = get(posicao);
    if (t.tipo == Token.Tipo.DELIM && t.valor.equals("(")) {
      ++posicao;
      expressao();
      if (get(posicao).tipo != Token.Tipo.DELIM || !get(posicao).valor.equals(")")) {
        Mensagem.abort("Espera-se um ')' na expressão\n");
      }
      ++posicao;
      resto3();
    }
    else{
      if (t.tipo != Token.Tipo.ID && t.tipo != Token.Tipo.NUM) {
        Mensagem.abort("Espera-se uma expressão, número ou variável\n");
      }
      id();
      resto3();
    }
  }

  // <resto1> -> + <termo> <resto1> | - <termo> <resto1> | E
  private void resto1() {
    Token t = get(index);
    if (t.tipo == Token.Tipo.OPR_AR && Arrays.asList("+", "-").contains(t.valor)) {
      ++posicao;
      termo();
      escrevaOperacao(t.valor.equals("+") ? "add" : "sub");
      resto1();
    }
  }

  // <resto2> -> * <fator> <resto2> | / <fator> <resto2> | % <fator> <resto2> | E
  private void resto2() {
    Token t = get(posicao);
    if (t.tipo == Token.Tipo.OPR_AR && Arrays.asList("*", "/", "%").contains(t.valor)) {
      ++posicao;
      fator();
      escrevaOperacao(t.valor.equals("*") ? "mul" : t.valor.equals("/") ? "div" : "mod");
      resto2();
    }
  }

  // <resto3> -> ^ (<expressao>) | ^ <id> | E
  private void resto3() {
    if (get(posicao).tipo == Token.Tipo.OPR_AR && get(posicao).valor.equals("^")) {
      ++posicao;
      if (get(posicao).tipo == Token.Tipo.DELIM && get(posicao).valor.equals("(")) {
        ++posicao;
        expressao();
        if (!get(posicao).equals(")")) {
          Mensagem.abort("Espera-se um ')' na expressão\n");
        }
        ++posicao;
      }
      else {
        if (get(posicao).tipo != Token.Tipo.ID && get(posicao).tipo != Token.Tipo.NUM) {
          Mensagem.abort("Espera-se uma expressão, número ou variável após o operador '^'\n");
        }
        id();
      }
      escrevaOperacao("pot");
    }
  }
}
