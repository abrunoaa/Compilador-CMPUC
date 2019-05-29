from rply import ParserGenerator
from ast import Number, Sum, Sub, Mul, Div, Print


class Parser():
    def __init__(self, module, builder, printf):
        self.pg = ParserGenerator(
            # A list of all token names accepted by the parser.
            [
              'NUMERO',
              'ESCREVA',
              'APAR',
              'FPAR',
              'PONTO_VIRGULA',
              'SOMA',
              'SUB',
              'MUL',
              'DIV',
            ],

            precedence = [
              ('left', ['SOMA', 'SUB', ]),
              ('left', ['MUL', 'DIV', ]),
            ],
        )
        self.module = module
        self.builder = builder
        self.printf = printf

    def parse(self):
        @self.pg.production('programa : ESCREVA APAR expressao FPAR PONTO_VIRGULA')
        def programa(p):
            return Print(self.builder, self.module, self.printf, p[2])

        @self.pg.production('expressao : expressao SOMA expressao')
        @self.pg.production('expressao : expressao SUB expressao')
        @self.pg.production('expressao : expressao MUL expressao')
        @self.pg.production('expressao : expressao DIV expressao')
        def expressao(p):
            left = p[0]
            right = p[2]
            operator = p[1]
            if operator.gettokentype() == 'SOMA':
                return Sum(self.builder, self.module, left, right)
            elif operator.gettokentype() == 'SUB':
                return Sub(self.builder, self.module, left, right)
            elif operator.gettokentype() == 'MUL':
                return Mul(self.builder, self.module, left, right)
            elif operator.gettokentype() == 'DIV':
                return Div(self.builder, self.module, left, right)

        @self.pg.production('expressao : NUMERO')
        def number(p):
            return Number(self.builder, self.module, p[0].value)

        @self.pg.error
        def error_handle(token):
            raise ValueError(token)

    def get_parser(self):
        return self.pg.build()
