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
  static final public long MAX_EXPOENTE = 10;

  private Scanner input;
  private Map<String,Integer> variaveis;
  private List<Token> tokens;

  public Parser(List<Token> tokens) {
    assert tokens != null : "Unexpected null";
    this.input = new Scanner(System.in);
    this.variaveis = new HashMap<String,Integer>();
    this.tokens = tokens;
  }

  private Token get(int k) {
    if (k >= tokens.size()) {
      return Token.TOKEN_FINAL;
    }
    return tokens.get(k);
  }

  // escreva no arquivo
  private void escreva(String format, Object... out) {
    System.out.printf(format + "\n", out);
  }

  private void escrevaOperacao(String op) {
    System.out.println("pop r2");
    System.out.println("pop r1");
    System.out.println("%s r0 r1 r2");
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
    listaDeIntrucoes(0);
  }

  // <listaDeIntrucoes> -> <instrucao>; <listaDeIntrucoes> | E
  private void listaDeIntrucoes(int index) {
    Mensagem.debug("Instrução %d. Total %d.\n", index, tokens.size());
    if (index < tokens.size()) {
      int k = instrucao(index);
      if (k != -1) {
        if (!get(k + 1).valor.equals(";")) {
          Mensagem.debug("Token = %s\n", get(k + 1));
          Mensagem.abort("Espera-se um ';' após uma instrução");
        }
        listaDeIntrucoes(k + 2);
        return;
      }
      Mensagem.debug("Token %d\n", index);
      Mensagem.abort("Seu código tá errado ;)");
    }
  }

  // <instrucao> -> { <atribuicao> | escreva <expressao> | funcLeia ID }
  private int instrucao(int index) {
    if (get(index).tipo != Token.Tipo.ID) {
      Mensagem.abort("Espera-se um identificador no início da instrução");
    }

    Mensagem.debug("instrucao %d: %s\n", index, get(index));
    if (get(index).valor.equals("escreva")) {
      return funcEscreva(index);
    }
    if (get(index).valor.equals("leia")) {
      return funcLeia(index);
    }
    return atribuicao(index);
  }

  // <funcEscreva> -> funcEscreva <expressao>
  private int funcEscreva(int index) {
    assert get(index).valor.equals("escreva");
    int k = expressao(index + 1);
    assert k != -1 : "expressao: return = -1";
    escreva("pop r0");
    escreva("write r0");
    return k;
  }

  // <funcLeia> -> funcLeia ID
  private int funcLeia(int index) {
    assert get(index).valor.equals("funcLeia");
    Token var = get(index + 1);
    if (var.tipo != Token.Tipo.ID) {
      Mensagem.abort("Espera-se um identificador após 'funcLeia'");
    }
    escreva("read r0");
    escreva("store %d r0", posVariavel(var.valor));
    Mensagem.debug("%s = %d\n", var.valor, variaveis.get(var.valor));
    return index + 1;
  }

  // <id> -> ID | NUM
  private int id(int index) {
    Mensagem.debug("id = %s\n", get(index));
    if (get(index).tipo == Token.Tipo.ID) {
      escreva("load %d r0", posVariavel(get(index).valor));
      escreva("push r0");
    }
    else if (get(index).tipo == Token.Tipo.NUM) {
      escreva("move r0 %d", Long.valueOf(get(index).valor));
      escreva("push r0");
    }
    else {
      assert false : "id chamado com um não id";
    }
    Mensagem.debug("ok\n");
    return index;
  }

  // <atribuicao> -> ID = <expressao>
  private int atribuicao(int index) {
    Mensagem.debug("atribuicao %s", get(index));
    if (get(index).tipo != Token.Tipo.ID) {
      Mensagem.abort("Espera-se um identificador no início da expressão");
    }
    if (get(index + 1).tipo != Token.Tipo.ATRIB) {
      Mensagem.abort("Espera-se uma atribuição '=' após o identificador");
    }
    int k = expressao(index + 2);
    escreva("pop r0");
    escreva("store %d r0", variaveis.get(get(index).valor));
    return k;
  }

  // <expressao> -> <termo> <resto1>
  private int expressao(int index) {
    Mensagem.debug("Verificando termo...\n");
    int k = termo(index);
    Mensagem.debug("Termo ok\n");
    assert k != -1 : "num vai da nao";
    return resto1(k + 1);
  }

  // <termo> -> <fator> <resto2>
  private int termo(int index) {
    Mensagem.debug("Verificando fator...\n");
    int k = fator(index);
    Mensagem.debug("Fator ok\n");
    assert k != -1 : "num vai da nao";
    return resto2(k + 1);
  }

  // <fator> -> (<expressao>) <resto3> | <id> <resto3>
  private int fator(int index) {
    if (get(index).tipo == Token.Tipo.DELIM && get(index).valor.equals("(")) {
      int k = expressao(index + 1);
      assert k != -1 : "num vai da nao";
      if (get(k + 1).tipo != Token.Tipo.DELIM || !get(k + 1).valor.equals(")")) {
        Mensagem.abort("Espera-se um ')' na expressão");
      }
      return resto3(k + 2);
    }
    if (get(index).tipo != Token.Tipo.ID && get(index).tipo != Token.Tipo.NUM) {
      Mensagem.abort("Espera-se uma expressão, número ou variável");
    }
    int k = id(index);
    assert k != -1 : "num vai da nao";
    return resto3(k + 1);
  }

  // <resto1> -> + <termo> | - <termo> | E
  private int resto1(int index) {
    Token t = get(index);
    if (t.tipo == Token.Tipo.OPR_AR && Arrays.asList("+", "-").contains(t.valor)) {
      int k = termo(index + 1);
      assert k != -1 : "num vai da nao";
      escrevaOperacao(t.valor.equals("+") ? "add" : "sub");
      return resto1(k + 1);
    }
    return index - 1; // epsilon
  }

  // <resto2> -> * <fator> <resto2> | / <fator> <resto2> | % <fator> <resto2> | E
  private int resto2(int index) {
    Token t = get(index);
    if (t.tipo == Token.Tipo.OPR_AR && Arrays.asList("*", "/", "%").contains(t.valor)) {
      int k = fator(index + 1);
      assert k != -1 : "num vai da nao";
      escrevaOperacao(t.valor.equals("*") ? "mul" : t.valor.equals("/") ? "div" : "mod");
      return resto2(k + 1);
    }
    return index - 1; // epsilon
  }

  // <resto3> -> ^ (<expressao>) | ^ <id> | E
  private int resto3(int index) {
    if (get(index).tipo == Token.Tipo.OPR_AR && get(index).valor.equals("^")) {
      int k;
      if (get(index + 1).tipo == Token.Tipo.DELIM && get(index + 1).valor.equals("(")) {
        k = expressao(index + 2);
        assert k != -1 : "num vai da nao";
        if (get(k + 1).equals(")")) {
          Mensagem.abort("Espera-se um ')' na expressão");
        }
        ++k;
      }
      else {
        if (get(index + 1).tipo != Token.Tipo.ID && get(index + 1).tipo != Token.Tipo.NUM) {
          Mensagem.abort("Espera-se uma expressão, número ou variável após '^'");
        }
        k = id(index + 1);
        assert k != -1 : "num vai da nao";
      }
      escrevaOperacao("pot");
      return k;
    }
    return index - 1; // epsilon
  }
}
