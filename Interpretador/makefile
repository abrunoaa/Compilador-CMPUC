EXE=compilador
FLEX=flex
BISON=bison -d
CPP=g++ -std=c++11 -o $(EXE)

Compilador: lex.yy.c parser.tab.c parser.tab.h
	$(CPP) lex.yy.c parser.tab.c

lex.yy.c: lexer.lex parser.tab.h
	$(FLEX) lexer.lex

parser.tab.c parser.tab.h: parser.y
	$(BISON) parser.y

clean:
	@rm lex.yy.c parser.tab.h parser.tab.c compilador
