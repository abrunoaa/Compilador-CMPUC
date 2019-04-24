package compilador;

import util.Codigo;
import util.Mensagem;

import compilador.Token;
import compilador.Lexer;
import compilador.Parser;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

public class Compilador {
  public Compilador(String source) {
    Codigo codigo = null;
    try {
      codigo = new Codigo(new Scanner(new InputStreamReader(new FileInputStream(source))));
    } catch (FileNotFoundException e) {
      Mensagem.abort("Arquivo '%s' não encontrado\n", source);
    }

    Lexer lexer = new Lexer(codigo);
    List<Token> tokens = lexer.leiaTokens();
    Mensagem.debug("%d Tokens = %s\n", tokens.size(), tokens);

    Parser parser = new Parser(tokens);
    String assembly = parser.geraAssembly();
  }
}
