%{
#include <cmath>

#include <string>
#include <iostream>
#include <unordered_map>
using namespace std;

unordered_map<string,int> variaveis;

string stringBuffer;

/* lexer usado pelo parser */
extern int yylex();

/* Fluxo de entrada do lexer */
extern FILE* yyin;

void yyerror(const char* s);
%}

%name parse

%union {
  int intValue;
  char* stringValue;
}

%token LEIA
%token ESCREVA
%token<intValue> NUM
%token<stringValue> VAR
%token ATRIB
%token ADD SUB MUL DIV MOD POT
%token APAR FPAR
%token PV

%left ADD SUB
%left MUL DIV MOD
%right POT

%type<intValue> expressao termo fator base expoente



/* Gramática */
%%
instrucoes: instrucoes instrucao PV  {  }
| /*Quando nada é definido, assume-se epsilon*/
;

instrucao: VAR ATRIB expressao  { variaveis[(string)$1] = (int)$3; }
| ESCREVA expressao             { cout << (int)$2 << '\n'; }
| LEIA VAR                      { cin >> variaveis[(string)$2]; }
;

expressao: termo        { $$ = (int)$1; }
| expressao ADD termo   { $$ = (int)$1 + (int)$3; }
| expressao SUB termo   { $$ = (int)$1 - (int)$3; }
;

termo: fator        { $$ = (int)$1; }
| termo MUL fator   { $$ = (int)$1 * (int)$3; }
| termo DIV fator   { $$ = (int)$1 / (int)$3; }
| termo MOD fator   { $$ = (int)$1 % (int)$3; }
;

fator: base             { $$ = (int)$1; }
| fator POT expoente    { $$ = (int)pow((int)$1, (int)$3); }
| APAR expressao FPAR   { $$ = (int)$2; }
;

base: VAR   { $$ = variaveis[(string)$1]; }
| NUM       { $$ = (int)$1; }
;

expoente: base          { $$ = (int)$1; }
| APAR expressao FPAR   { $$ = (int)$2; }
;



%%
int main() {
#ifdef YYDEBUG
  /* 0 = não debugando; 1 = debugando */
  yydebug = 0;
#endif

  /* Faz a leitura do arquivo */
  yyin = fopen("source.cmp", "r");
  yyparse();
  fclose(yyin);
  return 0;
}

void yyerror(const char* s) {
  cerr << s << '\n';
  exit(1);
}
