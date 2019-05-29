from rply import LexerGenerator

def build_lexer():
  lexer = LexerGenerator()

  # keywords
  lexer.add('ESCREVA', 'escreva(?!\w)')
  lexer.add('LEIA', 'leia(?!\w)')
  lexer.add('DEF', 'def(?!\w)')
  lexer.add('IF', 'if(?!\w)')
  lexer.add('ELSE', 'else(?!\w)')
  lexer.add('WHILE', 'while(?!\w)')

  lexer.add('VAR', '[a-zA-Z_][a-zA-Z0-9_]')

  lexer.add('INTEIRO', r'-?\d+')
  lexer.add('REAL', r'-?\d+.\d+')
  lexer.add('STRING', r'(".?")')

  lexer.add('(', '(')
  lexer.add(')', ')')
  lexer.add('{', '{')
  lexer.add('}', '}')
  lexer.add(';', ';')

  lexer.add('=', '=')
  lexer.add('==', '==')
  lexer.add('!=', '!=')
  lexer.add('<', '<')
  lexer.add('<=', '<=')
  lexer.add('>', '>')
  lexer.add('>=', '>=')
  lexer.add('&&', '&&')
  lexer.add('||', '||')

  lexer.add('SOMA', '+')
  lexer.add('SUB', '-')
  lexer.add('MUL', '*')
  lexer.add('DIV', '/')
  lexer.add('MOD', '%')
  lexer.add('POT', '^')

  lexer.ignore('\s+')

  return lexer.build()
