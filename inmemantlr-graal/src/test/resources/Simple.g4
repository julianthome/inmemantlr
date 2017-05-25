grammar Simple;

Opname: [a-zA-Z]+[_a-zA-Z0-9]*;
Operation: '+' | '-';
Datatype: 'INTEGER';
String : '"' (~('"') | '\\"')* '"' | '\'' (~('\'') | '\\\'')* '\'' ;

s: (print | sequence)* EOF;

print: 'PRINT' (expression | string);

string: String;

expression: operand Operation operand;

operand: Opname;

parlist: operand (',' operand)*;

sequence:  Datatype parlist;

WS  :  [ \t\r\n]+ -> skip;