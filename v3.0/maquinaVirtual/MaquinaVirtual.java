package maquinaVirtual;

import util.Codigo;
import util.Mensagem;
import maquinaVirtual.MaquinaVirtual;
import maquinaVirtual.Token;
import maquinaVirtual.Lexer;
import maquinaVirtual.Parser;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;
import java.util.NoSuchElementException;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.List;
import java.util.Stack;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

public class MaquinaVirtual {
  static class Instrucao {
    final String opcode;
    final String[] operandos;

    Instrucao(String opcode, String[] operandos) {
      this.opcode = opcode;
      this.operandos = operandos;
    }

    public String toString() {
      StringBuilder buf = new StringBuilder();
      buf.append("(" + opcode + ":");
      for (String x : operandos) {
        buf.append(" ");
        buf.append(x);
      }
      buf.append(")");
      return buf.toString();
    }
  }

  public static final int MAX_PILHA = 10000;
  public static final int MAX_REGISTRADORES = 10;
  public static final int MAX_MEMORIA = 10000;
  public static final int MAX_INSTRUCOES = 100000;

  public static Set<String> keywordDesvio = new HashSet<String>();
  static {
    keywordDesvio.add("jeq");
    keywordDesvio.add("jne");
    keywordDesvio.add("jlt");
    keywordDesvio.add("jle");
    keywordDesvio.add("jgt");
    keywordDesvio.add("jge");
    keywordDesvio.add("jump");
  }

  final static public Set<String> operacoes0 = new HashSet<String>();
  final static public Set<String> operacoes1 = new HashSet<String>();
  final static public Set<String> operacoes2 = new HashSet<String>();
  final static public Set<String> operacoes3 = new HashSet<String>();
  final static public Set<String> registradores = new HashSet<String>();
  static {
    for (String s : "halt ret".split(" ")) {
      operacoes0.add(s);
    }
    for (String s : "push pop jump read write call".split(" ")) {
      operacoes1.add(s);
    }
    for (String s : "not load store move".split(" ")) {
      operacoes2.add(s);
    }
    for (String s : "add sub mul div mod pot and or jeq jne jlt jle jgt jge".split(" ")) {
      operacoes3.add(s);
    }

    for (int k = 0; k < MAX_REGISTRADORES; ++k) {
      String s = "r" + k;
      registradores.add(s);
    }
  }

  private Scanner input = new Scanner(System.in);
  private int pc = 0;
  private long[] registrador = new long[MAX_REGISTRADORES];
  private long[] memoria = new long[MAX_MEMORIA];
  private Stack<Long> pilha = new Stack<Long>();
  private List<Instrucao> instrucoes;

  boolean fim() {
    return pc == instrucoes.size();
  }

  Instrucao proxInstrucao() {
    assert pc < instrucoes.size();
    return instrucoes.get(pc++);
  }

  void sigsegv(int x, int min, int max) {
    if (x < min || x > max) {
      Mensagem.abort("Falha de segmentação\n");
    }
  }

  long read() {
    Mensagem.print(" >> ");
    try {
      return input.nextLong();
    } catch (Exception e) {
      Mensagem.abort("Erro inesperado durante leitura\n");
      return 0;
    }
  }

  void write(long x) {
    Mensagem.print("%d\n", x);
  }

  void jump(int to) {
    sigsegv(to, 0, instrucoes.size() - 1);
    pc = to;
  }

  long registrador(String reg) {
    assert registradores.contains(reg);
    return registrador[Integer.parseInt(reg.substring(1))];
  }

  void setaRegistrador(String reg, long x) {
    assert registradores.contains(reg);
    registrador[Integer.parseInt(reg.substring(1))] = x;
  }

  long memoria(int pos) {
    sigsegv(pos, 0, memoria.length - 1);
    return memoria[pos];
  }

  void setaMemoria(int pos, long x) {
    sigsegv(pos, 0, memoria.length - 1);
    memoria[pos] = x;
  }

  void push(long x) {
    sigsegv(pilha.size() + 1, 0, MAX_PILHA);
    pilha.push(x);
  }

  long pop() {
    sigsegv(pilha.size() - 1, 0, MAX_PILHA);
    return pilha.pop();
  }
  
  void call(int pos) {
    push(pc);
    jump(pos);
  }
  
  void ret() {
    int pos = (int)pop();
    jump(pos);
  }

  void halt(){
    System.exit(0);
  }

  public MaquinaVirtual(String source) {
    Codigo codigo = null;
    try {
      codigo = new Codigo(new Scanner(new InputStreamReader(new FileInputStream(source))));
    } catch (FileNotFoundException e) {
      Mensagem.abort("Arquivo '%s' não encontrado\n", source);
    }

    Lexer lexer = new Lexer(codigo);
    List<Token> tokens = lexer.leiaTokens();
    Mensagem.debug("%d tokens = %s", tokens.size(), tokens);

    this.instrucoes = Parser.montaInstrucoes(tokens);
    Parser parser = new Parser(this);
    parser.programa();
  }
}
