from rply import LexerGenerator


class Lexer():
    def __init__(self):
        self.lexer = LexerGenerator()

    def _add_tokens(self):
        # Print
        self.lexer.add('ESCREVA', r'escreva')
        # Parenthesis
        self.lexer.add('APAR', r'\(')
        self.lexer.add('FPAR', r'\)')
        # Semi Colon
        self.lexer.add('PONTO_VIRGULA', r'\;')
        # Operators
        self.lexer.add('SOMA', r'\+')
        self.lexer.add('SUB', r'\-')
        self.lexer.add('MUL', r'\*')
        self.lexer.add('DIV', r'\/')
        # Number
        self.lexer.add('NUMERO', r'\d+')
        # Ignore spaces
        self.lexer.ignore('\s+')

    def get_lexer(self):
        self._add_tokens()
        return self.lexer.build()
