/*
 ANTLR4 grammar taken from TypeScript Documentation:
 https://github.com/Microsoft/TypeScript/blob/master/doc/spec.md#12.1.2
 https://www.typescriptlang.org/docs/tutorial.html
 https://basarat.gitbooks.io/typescript/content/
 https://github.com/Microsoft/TypeScript/wiki/What's-new-in-TypeScript

 IntelliJ with antlr4 plugin used: http://plugins.jetbrains.com/plugin/7358?pr=
 to develop this grammar
*/


grammar Fail;


s : RULE | STRICT;

RULE : '[a-z]';

DIGIT: '[0-9]';

STRICT:
 {!strictMode}? '0' DIGIT+
 ;


//
// Whitespace and comments
//

WS:  [ \t\r\n\u000C]+ -> skip
  ;
