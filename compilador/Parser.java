package compilador;

import compilador.Token;

import util.Mensagem;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;

class Parser {
  // posição de memória das variáveis
  private Map<String,Integer> variaveis = new HashMap<String,Integer>();

  // tokens do código
  private List<Token> tokens;

  // token sendo processado
  private int posicao;

  // código em assembly
  private StringWriter writer = new StringWriter();
  private PrintWriter printWriter = new PrintWriter(writer);

  public Parser(List<Token> tokens) {
    assert tokens != null;
    this.tokens = tokens;
  }

  public String geraAssembly() {
    programa();
    printWriter.flush();
    return writer.toString();
  }

  private Token get(int k) {
    if (k >= tokens.size()) {
      return Token.TOKEN_FINAL;
    }
    return tokens.get(k);
  }

  private void abort(int linha, String format, Object... out) {
    Mensagem.abort("Linha %d: " + format + "Encontrado: '" + get(posicao).valor + "'\n", linha, out);
  }

  // escreve a instrução no arquivo
  private void instrucao(String format, Object... out) {
    printWriter.printf(format + "\n", out);
  }

  private void instrucaoOperacao(String op) {
    printWriter.println("pop r2");
    printWriter.println("pop r1");
    printWriter.printf("%s r0 r1 r2\n", op);
    printWriter.println("push r0");
  }

  private int posVariavel(String var) {
    if (!variaveis.containsKey(var)) {
      variaveis.put(var, variaveis.size());
    }
    return variaveis.get(var);
  }

  // <programa> -> <listaDeIntrucoes>
  private void programa() {
    listaDeIntrucoes();
  }

  // <listaDeIntrucoes> -> <instrucao>; <listaDeIntrucoes> | E
  private void listaDeIntrucoes() {
    if (posicao < tokens.size()) {
      instrucao();
      if (get(posicao).tipo != Token.Tipo.DELIM || !get(posicao).valor.equals(";")) {
        abort(get(posicao - 1).linha, "Espera-se um ';' após instrução\n");
      }
      ++posicao;
      listaDeIntrucoes();
    }
  }

  // <instrucao> -> { <atribuicao> | instrucao <expressao> | leia ID }
  private void instrucao() {
    if (get(posicao).tipo != Token.Tipo.ID) {
      abort(get(posicao).linha, "Espera-se um identificador no início da instrução\n");
    }
    if (get(posicao).valor.equals("escreva")) {
      ++posicao;
      expressao();
      instrucao("pop r0");
      instrucao("write r0");
    }
    else if (get(posicao).valor.equals("leia")) {
      ++posicao;
      Token var = get(posicao);
      if (var.tipo != Token.Tipo.ID) {
        abort(get(posicao - 1).linha, "Espera-se um identificador após 'leia'\n");
      }
      id();
      instrucao("read r0");
      instrucao("store r0 %d", posVariavel(var.valor));
    }
    else {
      atribuicao();
    }
  }

  // <id> -> ID | NUM
  private void id() {
    if (get(posicao).tipo == Token.Tipo.ID) {
      instrucao("load r0 %d", posVariavel(get(posicao).valor));
      instrucao("push r0");
    }
    else if (get(posicao).tipo == Token.Tipo.NUM) {
      instrucao("move r0 %d", Long.valueOf(get(posicao).valor));
      instrucao("push r0");
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
      abort(get(posicao).linha, "Espera-se um identificador no início da expressão\n");
    }
    if (get(posicao + 1).tipo != Token.Tipo.ATRIB) {
      abort(get(posicao).linha, "Espera-se uma atribuição '=' após o identificador\n");
    }
    posicao += 2;
    expressao();
    instrucao("pop r0");
    instrucao("store r0 %d", posVar);
  }

  // <expressao> -> <termo> <resto1>
  private void expressao() {
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
        abort(get(posicao - 1).linha, "Espera-se um ')' após expressão\n");
      }
      ++posicao;
      resto3();
    }
    else{
      if (t.tipo != Token.Tipo.ID && t.tipo != Token.Tipo.NUM) {
        abort(get(posicao).linha, "Espera-se uma expressão, número ou variável\n");
      }
      id();
      resto3();
    }
  }

  // <resto1> -> + <termo> <resto1> | - <termo> <resto1> | E
  private void resto1() {
    Token t = get(posicao);
    if (t.tipo == Token.Tipo.OPR_AR && Arrays.asList("+", "-").contains(t.valor)) {
      ++posicao;
      termo();
      instrucaoOperacao(t.valor.equals("+") ? "add" : "sub");
      resto1();
    }
  }

  // <resto2> -> * <fator> <resto2> | / <fator> <resto2> | % <fator> <resto2> | E
  private void resto2() {
    Token t = get(posicao);
    if (t.tipo == Token.Tipo.OPR_AR && Arrays.asList("*", "/", "%").contains(t.valor)) {
      ++posicao;
      fator();
      instrucaoOperacao(t.valor.equals("*") ? "mul" : t.valor.equals("/") ? "div" : "mod");
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
          abort(get(posicao - 1).linha, "Espera-se um ')' após expressão\n");
        }
        ++posicao;
      }
      else {
        if (get(posicao).tipo != Token.Tipo.ID && get(posicao).tipo != Token.Tipo.NUM) {
          abort(get(posicao - 1).linha, "Espera-se uma expressão, número ou variável após o operador '^'\n");
        }
        id();
      }
      instrucaoOperacao("pot");
    }
  }
}
