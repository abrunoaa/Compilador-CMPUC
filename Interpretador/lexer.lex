%option noyywrap

%{
#include <string>
#include <iostream>
using namespace std;

#include "parser.tab.h"

extern string stringBuffer;
%}



/* Tokens */
%%

"leia" { return LEIA; }
"escreva" { return ESCREVA; }
[_a-zA-Z][_a-zA-Z0-9]* { stringBuffer = yytext; yylval.stringValue = (char*)stringBuffer.data(); return VAR; }
[0-9]+  { yylval.intValue = atoi(yytext); return NUM; }
"=" { return ATRIB; }
"+" { return ADD; }
"-" { return SUB; }
"*" { return MUL; }
"/" { return DIV; }
"%" { return MOD; }
"^" { return POT; }
"(" { return APAR; }
")" { return FPAR; }
";" { return PV; }
[" "|"\n"|"\t"] { /* ignora espaços */ }
. { printf("Caracter inválido: '%c'\n", *yytext); }



%%
