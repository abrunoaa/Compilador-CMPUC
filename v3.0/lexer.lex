%option noyywrap

%{
#include <string>
#include <iostream>
using namespace std;

#include "parser.tab.h"

extern void yyerror(const char* s);
%}



/* Tokens */
%%

"leia"                        { return LEIA; }
"escreva"                     { return ESCREVA; }
"if"                          { return IF; }
"else"                        { return ELSE; }
"while"                       { return WHILE; }
"def"                         { return DEF; }
"main"                        { return MAIN; }
[_a-zA-Z][_a-zA-Z0-9]*        { yylval.stringValue = new char[strlen(yytext)+1];
                                strcpy(yylval.stringValue, yytext);
                                return VAR; }
[-]?[0-9]+                    { yylval.intValue = atoi(yytext);
                                return INTEIRO; }
"="                           { return '='; }
"+"                           { return '+'; }
"-"                           { return '-'; }
"*"                           { return '*'; }
"/"                           { return '/'; }
"%"                           { return '%'; }
"^"                           { return '^'; }
"("                           { return '('; }
")"                           { return ')'; }
"{"                           { return '{'; }
"}"                           { return '}'; }
";"                           { return ';'; }
"=="                          { return IGUAL; }
"!="                          { return NIGUAL; }
"<"                           { return MENOR; }
"<="                          { return MENOR_IGUAL; }
">"                           { return MAIOR; }
">="                          { return MAIOR_IGUAL; }
[" "|"\n"|"\t"]               { /* ignora espaços */ }
.                             { yyerror(("Caracter inválido: " + string(yytext) + "\n").data()); }



%%
