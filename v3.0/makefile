DEBUG =
# DEBUG = --debug
EXE = compilador
FLEX = flex $(DEBUG)
BISON = bison -d $(DEBUG)

ifeq ($(DEBUG), --debug)
	CPP = g++ -DDEBUG -std=c++11 -Wall -Wextra -Wconversion -o $(EXE)
else
	CPP = g++ -std=c++11 -w -o $(EXE)
endif

Assembly: Compilador compilador source.cmp
	@./compilador

Compilador: lex.yy.c parser.tab.c parser.tab.h
	@$(CPP) lex.yy.c parser.tab.c

lex.yy.c: lexer.lex parser.tab.h
	@$(FLEX) lexer.lex

parser.tab.c parser.tab.h: parser.y
	@$(BISON) parser.y

clean:
	@rm -f lex.yy.c parser.tab.h parser.tab.c compilador
	@find . -type f -name '*.class' -delete
