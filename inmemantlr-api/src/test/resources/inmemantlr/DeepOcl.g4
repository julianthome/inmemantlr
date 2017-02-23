/**
 * Define a grammar called DeepOcl
 * an example OCL grammar
 * https://melanee2.informatik.uni-mannheim.de/stash/projects/MEL
 */
grammar DeepOcl;

contextDeclCS
:
	(
		propertyContextDeclCS
		| classifierContextCS
		| operationContextCS
	)+
;

operationContextCS
:
	CONTEXT
	(
		ID ':'
	)?
	(
		ID '::'
		(
			ID '::'
		)* ID
		| ID
	) '('
	(
		parameterCS
		(
			',' parameterCS
		)*
	)? ')'
	(
		':' typeExpCS
	)?
	(
		preCS
		| postCS
		| bodyCS
	)*
;

CONTEXT
:
	'context'
;

bodyCS
:
	'body' ID? ':' specificationCS
;

postCS
:
	'post' ID? ':' specificationCS
;

preCS
:
	'pre' ID? ':' specificationCS
;

defCS
:
	'def' ID? ':' ID
	(
		(
			'(' parameterCS
			(
				',' parameterCS
			)* ')'
		)? ':' typeExpCS? '=' specificationCS
	)
;

typeExpCS
:
	typeNameExpCS
	| typeLiteralCS
;

typeLiteralCS
:
	primitiveTypeCS
	| collectionTypeCS
	| tupleTypeCS
;

tupleTypeCS
:
	'Tuple'
	(
		'(' tuplePartCS
		(
			',' tuplePartCS
		)* ')'
		| '<' tuplePartCS
		(
			',' tuplePartCS
		)* '>'
	)?
;

tuplePartCS
:
	ID ':' typeExpCS
;

collectionTypeCS
:
	collectionTypeIDentifier
	(
		'(' typeExpCS ')'
		| '<' typeExpCS '>'
	)?
;

collectionTypeIDentifier
:
	'Collection'
	| 'Bag'
	| 'OrderedSet'
	| 'Sequence'
	| 'Set'
;

primitiveTypeCS
:
	'Boolean'
	| 'Integer'
	| 'Real'
	| 'ID'
	| 'UnlimitedNatural'
	| 'OclAny'
	| 'OclInvalID'
	| 'OclVoID'
;

typeNameExpCS
:
	ID '::'
	(
		ID '::'
	)* ID
	| ID
;

specificationCS
:
	expCS
;

expCS
:
	infixedExpCS
;

infixedExpCS
:
	prefixedExpCS
	(
		binaryOperatorCS* prefixedExpCS
	)*
;

binaryOperatorCS
:
	'+'
	| '*'
	| '-'
	| '/'
	| '<'
	| '<'
	| '<='
	| '>='
	| '='
	| 'and'
	| 'or'
	| 'xor'
	| 'implies'
	| NavigationOperatorCS
;

NavigationOperatorCS
:
	'.'
	| '->'
;

prefixedExpCS
:
	primaryExpCS
	| UnaryOperatorCS+ primaryExpCS
;

UnaryOperatorCS
:
	'-'
	| 'not'
;

primaryExpCS
:
	navigatingExpCS
	| SelfExpCS
	| primitiveLiteralExpCS
	| tupleLiteralExpCS
	| collectionLiteralExpCS
	| typeLiteralExpCS
	| letExpCS
	| ifExpCS
	| nestedExpCS
;

nestedExpCS
:
	'(' expCS ')'
;

ifExpCS
:
	'if' expCS 'then' expCS 'else' expCS 'endif'
;

letExpCS
:
	'let' letVariableCS
	(
		',' letVariableCS
	)* 'in' expCS
;

letVariableCS
:
	ID
	(
		':' typeExpCS
	)? '=' expCS
;

typeLiteralExpCS
:
	typeLiteralCS
;

collectionLiteralExpCS
:
	collectionTypeCS '{'
	(
		collectionLiteralPartCS
		(
			',' collectionLiteralPartCS
		)*
	)? '}'
;

collectionLiteralPartCS
:
	expCS
	(
		'..' expCS
	)?
;

tupleLiteralExpCS
:
	'Tuple' '{' tupleLiteralPartCS
	(
		',' tupleLiteralPartCS
	)* '}'
;

tupleLiteralPartCS
:
	ID
	(
		':' typeExpCS
	)? '=' expCS
;

SelfExpCS
:
	'self'
;

primitiveLiteralExpCS
:
	NumberLiteralExpCS
	| ID
	| BooleanLiteralExpCS
	| InvalIDLiteralExpCS
	| NullLiteralExpCS
;

InvalIDLiteralExpCS
:
	'invalid'
;

NumberLiteralExpCS
:
	INT
	(
		'.' INT
	)?
	(
		(
			'e'
			| 'E'
		)
		(
			'+'
			| '-'
		)? INT
	)?
;

fragment
DIGIT
:
	[0-9]
;

INT
:
	DIGIT+
;

BooleanLiteralExpCS
:
	'true'
	| 'false'
;

NullLiteralExpCS
:
	'null'
;

navigatingExpCS
:
	indexExpCS
	(
		'@' 'pre'
	)?
	(
		'(' navigatingArgCS* navigatingCommaArgCS* navigatingBarAgrsCS*
		navigatingCommaArgCS* navigatingSemiAgrsCS* navigatingCommaArgCS* ')'
	)*
;

navigatingSemiAgrsCS
:
	';' navigatingArgExpCS
	(
		':' typeExpCS
	)?
	(
		'=' expCS
	)?
;

navigatingCommaArgCS
:
	',' navigatingArgExpCS
	(
		':' typeExpCS
	)?
	(
		'=' expCS
	)?
;

navigatingArgExpCS
:
	expCS
;

navigatingBarAgrsCS
:
	'|' navigatingArgExpCS
	(
		':' typeExpCS
	)?
	(
		'=' expCS
	)?
;

navigatingArgCS
:
	navigatingArgExpCS
	(
		':' typeExpCS
	)?
	(
		'=' expCS
	)?
;

indexExpCS
:
	nameExpCS
	(
		'[' expCS
		(
			',' expCS
		)* ']'
	)?
;

nameExpCS
:
	(
		ID '::'
		(
			ID '::'
		)* ID
	)
	| ID
;

parameterCS
:
	(
		ID ':'
	)? typeExpCS
;

invCS
:
	'inv'
	(
		ID
		(
			'(' specificationCS ')'
		)?
	)? ':' specificationCS
;

classifierContextCS
:
	CONTEXT
	(
		ID ':'
	)?
	(
		(
			ID '::'
			(
				ID '::'
			)* ID
		)
		| ID
	)
	(
		invCS
		| defCS
	)*
;

propertyContextDeclCS
:
	CONTEXT
	(
		(
			ID '::'
			(
				ID '::'
			)* ID
		)
		| ID
	) ':' typeExpCS
	(
		(
			initCS derCS?
		)?
		| derCS initCS?
	)
;

derCS
:
	'derive' ':' specificationCS
;

initCS
:
	'init' ':' specificationCS
;

ID
:
	[a-zA-Z] [a-zA-Z0-9]*
;

WS
:
	[ \t\n\r]+ -> skip
;