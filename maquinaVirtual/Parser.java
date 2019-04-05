package maquinaVirtual;

import util.Mensagem;

import maquinaVirtual.MaquinaVirtual;
import util.Codigo;
import maquinaVirtual.Token;

import java.util.List;
import java.util.ArrayList;

class Parser {
  MaquinaVirtual vm;

  Parser(MaquinaVirtual vm) {
    this.vm = vm;
  }

  static List<MaquinaVirtual.Instrucao> montaInstrucoes(List<Token> tokens) {
    List<MaquinaVirtual.Instrucao> list = new ArrayList<MaquinaVirtual.Instrucao>();
    int i = 0;
    while (i < tokens.size()) {
      Token tokenOpcode = tokens.get(i);
      String opcode = tokenOpcode.valor;

      int n = 0;
      if (tokenOpcode.tipo == Token.Tipo.OPR3) {
        n = 3;
      }
      else if (tokenOpcode.tipo == Token.Tipo.OPR2) {
        n = 2;
      }
      else if (tokenOpcode.tipo == Token.Tipo.OPR1) {
        n = 1;
      }
      else {
        Mensagem.abort("Linha %d: Impossível determinar o número de operandos: %s", tokenOpcode.linha, opcode);
      }

      Mensagem.debug("Token %d de %d: %s com %d operandos", i, tokens.size(), opcode, n);
      if (i + n >= tokens.size()) {
        Mensagem.abort("Linha %d: Falta operandos: %s", tokenOpcode.linha, opcode);
      }

      String[] operandos = new String[n];
      for (int j = 0; j < n; ++j) {
        Token op = tokens.get(i + j + 1);
        operandos[j] = op.valor;
        if (op.tipo == Token.Tipo.NUM) {
          if (!(j == 2
                || (j == 1 && (MaquinaVirtual.keywordDesvio.contains(opcode) || opcode.matches("move|load|store")))
                || (j == 0 && opcode.equals("jump")))) {
            Mensagem.abort("Linha %d: Espera-se um registrador no %dº operando, número encontrado", op.linha, j + 1);
          }
        }
        else if (op.tipo == Token.Tipo.REG) {
          if (opcode.equals("jump") || (j == 2 && MaquinaVirtual.keywordDesvio.contains(opcode))) {
            Mensagem.abort("Linha %d: Destino do desvio é um registrador", op.linha);
          }
        }
        else {
          Mensagem.abort("Linha %d: %dº operando é inválido", op.linha, j + 1);
        }
      }

      list.add(new MaquinaVirtual.Instrucao(opcode, operandos));
      if (list.size() > MaquinaVirtual.MAX_INSTRUCOES) {
        Mensagem.abort("Programa enorme!");
      }

      i += 1 + n;
    }

    return list;
  }

  public void programa() {
    vm.jump(0);
    while (!vm.fim()) {
      executaInstrucao(vm.proxInstrucao());
    }
  }

  private long buscaOperando(String x) {
    try {
      return Long.parseLong(x);
    } catch (NumberFormatException e) {
      return vm.registrador(x);
    }
  }

  private void executaInstrucao(MaquinaVirtual.Instrucao instrucao) {
    String opcode = instrucao.opcode;
    String[] operandos = instrucao.operandos;

    Mensagem.debug("%s - %d operando(s)", instrucao, operandos.length);
    if (operandos.length == 1) {
      if (opcode.equals("jump")) {
        vm.jump(Integer.parseInt(operandos[0]));
      }
      else if (opcode.equals("push")) {
        vm.push(vm.registrador(operandos[0]));
      }
      else if (opcode.equals("pop")) {
        vm.setaRegistrador(operandos[0], vm.pop());
      }
      else if (opcode.equals("read")) {
        vm.setaRegistrador(operandos[0], vm.read());
      }
      else if (opcode.equals("write")) {
        vm.write(vm.registrador(operandos[0]));
      }
      else {
        Mensagem.abort("Que instrução é essa? '%s'", instrucao);
      }
    }
    else if (operandos.length == 2) {
      if (opcode.equals("not")) {
        vm.setaRegistrador(operandos[0], ~vm.registrador(operandos[1]));
      }
      else if (opcode.equals("move")) {
        vm.setaRegistrador(operandos[0], buscaOperando(operandos[1]));
      }
      else if (opcode.equals("load")) {
        vm.setaRegistrador(operandos[0], vm.memoria((int)buscaOperando(operandos[1])));
      }
      else if (opcode.equals("store")) {
        vm.setaMemoria((int)buscaOperando(operandos[1]), vm.registrador(operandos[0]));
      }
      else {
        Mensagem.abort("Que instrução é essa? '%s'", instrucao);
      }
    }
    else if (operandos.length == 3) {
      if (MaquinaVirtual.keywordDesvio.contains(opcode)) {
        long op1 = vm.registrador(operandos[0]);
        long op2 = vm.registrador(operandos[1]);
        int dest = Integer.parseInt(operandos[2]);
        boolean result = false;
        if (opcode.equals("jeq")) {
          result = op1 == op2;
        }
        else if (opcode.equals("jne")) {
          result = op1 != op2;
        }
        else if (opcode.equals("jlt")) {
          result = op1 < op2;
        }
        else if (opcode.equals("jle")) {
          result = op1 <= op2;
        }
        else if (opcode.equals("jgt")) {
          result = op1 > op2;
        }
        else if (opcode.equals("jge")) {
          result = op1 >= op2;
        }
        else {
          Mensagem.abort("Que instrução é essa? '%s'", instrucao);
        }

        if (result) {
          vm.jump(dest);
        }
      }
      else {
        long op1 = vm.registrador(operandos[1]);
        long op2 = buscaOperando(operandos[2]);
        long x = 0;
        if (opcode.equals("add")) {
          x = op1 + op2;
        }
        else if (opcode.equals("sub")) {
          x = op1 - op2;
        }
        else if (opcode.equals("mul")) {
          x = op1 * op2;
        }
        else if (opcode.equals("div")) {
          if (op2 == 0) {
            Mensagem.abort("Divisão por zero");
          }
          x = op1 / op2;
        }
        else if (opcode.equals("mod")) {
          if (op2 == 0) {
            Mensagem.abort("Divisão por zero");
          }
          x = op1 % op2;
        }
        else if (opcode.equals("pot")) {
          x = (long)(Math.pow(op1, op2) + .5);
        }
        else if (opcode.equals("and")) {
          x = op1 & op2;
        }
        else if (opcode.equals("or")) {
          x = op1 | op2;
        }
        else {
          Mensagem.abort("Que instrução é essa? '%s'", instrucao);
        }

        vm.setaRegistrador(operandos[0], x);
      }
    }
    else {
      Mensagem.abort("Que instrução é essa? '%s'", instrucao);
    }
  }
}
