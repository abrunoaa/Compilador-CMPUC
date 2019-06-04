%{
#include <cmath>

#include <cassert>
#include <string>
#include <iostream>
#include <unordered_map>
#include <vector>
using namespace std;

unordered_map<string,int> funcoes;
unordered_map<string, string> variaveis;

/* lexer usado pelo parser */
extern int yylex();

/* Fluxo de entrada do lexer */
extern FILE* yyin;

void yyerror(const char* s);

vector<string> codigo;

void add(){}

template<class... Type>
void add(const string& s, const Type&... t){
  codigo.push_back(s);
  add(t...);
}

int reserva(){
  codigo.push_back("");
  return codigo.size() - 1;
}

int currentLine(){
  return codigo.size() - 1;
}

void replace(int pos, string s){
  assert(0 <= pos && pos < codigo.size());
  codigo[pos] = s;
}

string getVarPos(string var){
  if(variaveis.count(var) == 0){
    variaveis[var] = to_string(variaveis.size());
  }
  return variaveis[var];
}

struct Label {
  int jmp_true;
  int jmp_false;
};
%}

%union {
  long intValue;
  char* stringValue;
  struct Label* label;
}

%token MAIN DEF LEIA ESCREVA ELSE
%token<label> IF WHILE
%token IGUAL NIGUAL MENOR MENOR_IGUAL MAIOR MAIOR_IGUAL
%token<intValue> INTEIRO
%token<stringValue> VAR

%left '+' '-'
%left '*' '/' '%'
%right '^'

%type<stringValue> condicao



/* Gramática */
%%
instrucoes: instrucoes instrucao ';'
| instrucoes if_else
| instrucoes while
| instrucoes def
| instrucoes main
|
;

if_else: IF '(' condicao ')'  { $1 = new Label();
                                $1->jmp_false = reserva();  /* posição que vai se der false */
                              }
  '{' instrucoes '}'          { /* pula else se der true */
                                $1->jmp_true = reserva();
                              }
  ELSE                        { /* inicio do else: seta posição do jump se a condição der false */
                                replace($1->jmp_false, (string)$3 + " r0 r1 " + to_string(currentLine() + 1));
                              }
  '{' instrucoes '}'          { /* fim do else: seta valor do jump para ignorar o else se der true */
                                replace($1->jmp_true, "jump " + to_string(currentLine() + 1));
                                delete $1;
                              }
;

while: WHILE          { $1 = new Label();
                        $1->jmp_true = currentLine(); /* salva a posição para repetir a condição */
                      }
  '(' condicao ')'    { $1->jmp_false = reserva(); }  /* reserva espaço para jump se der false */
  '{' instrucoes '}'  { add("jump " + to_string($1->jmp_true)); /* cria o laço voltando para a condição */
                        replace($1->jmp_false, (string)$4 + " r0 r1 " + to_string(currentLine() + 1));
                        delete $1;
                      }
;

def: DEF VAR '(' ')'  { if(funcoes.count($2)) {
                          yyerror("Função já declarada!!!");
                        }
                        funcoes[$2] = currentLine() + 1;
                      }
  '{' instrucoes '}'  { add("ret"); }
;

main: MAIN            { if(!codigo[0].empty()) {
                          yyerror("Dupla definição de main!!!");
                        }
                        replace(0, "jump " + to_string(currentLine() + 1)); }
  '{' instrucoes '}'  {  }

instrucao: VAR '=' expressao    { add("pop r0", "store r0 " + getVarPos($1)); }
| ESCREVA expressao             { add("pop r0", "write r0"); }
| LEIA VAR                      { add("read r0", "store r0 " + getVarPos($2)); }
| VAR '(' ')'                   { if(!funcoes.count($1)) {
                                    yyerror("Função não declarada!!!");
                                  }
                                  add("call " + to_string(funcoes[$1]));
                                }
;

condicao: expressao                 { add("pop r0", "move r1 0"); $$ = "jeq"; }
| expressao IGUAL expressao         { add("pop r1", "pop r0");  $$ = "jne"; }
| expressao NIGUAL expressao        { add("pop r1", "pop r0");  $$ = "jeq"; }
| expressao MENOR expressao         { add("pop r1", "pop r0");  $$ = "jge"; }
| expressao MENOR_IGUAL expressao   { add("pop r1", "pop r0");  $$ = "jgt"; }
| expressao MAIOR expressao         { add("pop r1", "pop r0");  $$ = "jle"; }
| expressao MAIOR_IGUAL expressao   { add("pop r1", "pop r0");  $$ = "jlt"; }
;

expressao: termo        {  }
| expressao '+' termo   { add("pop r2", "pop r1", "add r0 r1 r2", "push r0"); }
| expressao '-' termo   { add("pop r2", "pop r1", "sub r0 r1 r2", "push r0"); }
;

termo: fator        {  }
| termo '*' fator   { add("pop r2", "pop r1", "mul r0 r1 r2", "push r0"); }
| termo '/' fator   { add("pop r2", "pop r1", "div r0 r1 r2", "push r0"); }
| termo '%' fator   { add("pop r2", "pop r1", "mod r0 r1 r2", "push r0"); }
;

fator: base             {  }
| fator '^' expoente    { add("pop r2", "pop r1", "pot r0 r1 r2", "push r0"); }
| '(' expressao ')'     {  }
;

base: VAR       { add("load r0 " + getVarPos($1), "push r0"); }
| INTEIRO       { add("move r0 " + to_string($1), "push r0"); }
;

expoente: base        {  }
| '(' expressao ')'   {  }
;



%%
int main() {
#ifdef DEBUG
  yydebug = 1;
#endif

  /* Faz a leitura do arquivo */
  yyin = fopen("source.cmp", "r");
  reserva();
  yyparse();
  fclose(yyin);

  if(codigo[0].empty()){
    yyerror("Main não definido");
  }

  /* Grava o código "assembly" */
  freopen("source.asm", "w", stdout);
  for(string s : codigo) {
    cout << s << '\n';
  }
  cout << "halt\n";

  return 0;
}

void yyerror(const char* s) {
  cerr << "Erro: " << s << '\n';
  exit(1);
}
