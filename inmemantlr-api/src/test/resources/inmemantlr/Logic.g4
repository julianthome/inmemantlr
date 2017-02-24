grammar Logic;

rule_set : expression EOF ;

conclusion : IDENTIFIER ;

expression
 : not expression
 | expression and expression
 | expression or expression
 | expression implies expression
 | LPAREN expression RPAREN
 | atom;

atom : IDENTIFIER;

not : 'not';
and : 'and' ;
or : 'or' ;
xor : 'xor';
implies : 'implies';

LPAREN : '(' ;
RPAREN : ')' ;

IDENTIFIER : [a-zA-Z_][a-zA-Z_0-9]* ;

WS : [ \r\t\u000C\n]+ -> skip ;