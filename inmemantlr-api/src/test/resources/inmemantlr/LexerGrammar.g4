lexer grammar LexerGrammar;

RULE : '[a-z]';

DIGIT: '[0-9]';

WS:  [ \t\r\n\u000C]+ -> skip
  ;
