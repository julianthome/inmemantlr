grammar SimpleMixed;

fragment A:('a'|'A');
fragment B:('b'|'B');
fragment C:('c'|'C');
fragment D:('d'|'D');
fragment E:('e'|'E');
fragment F:('f'|'F');
fragment G:('g'|'G');
fragment H:('h'|'H');
fragment I:('i'|'I');
fragment J:('j'|'J');
fragment K:('k'|'K');
fragment L:('l'|'L');
fragment M:('m'|'M');
fragment N:('n'|'N');
fragment O:('o'|'O');
fragment P:('p'|'P');
fragment Q:('q'|'Q');
fragment R:('r'|'R');
fragment S:('s'|'S');
fragment T:('t'|'T');
fragment U:('u'|'U');
fragment V:('v'|'V');
fragment W:('w'|'W');
fragment X:('x'|'X');
fragment Y:('y'|'Y');
fragment Z:('z'|'Z');

s: userinput EOF;
userinput: month date year
    | month year date
    | year month date
    | year date month
    | date year month
    | date month year;

month: Month;

date: Date;

year: Year;


Month: J A N |  F E B | M A R | A P R | M A Y | J U N | J U L | A U G | S E P |
O C T | N O V | D E C;

Date: [1-9][0-2]?;

Year: [1-9][0-9][0-9][0-9];


WS  :  [ \t\r\n]+ -> skip;