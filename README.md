# inmemantlr

inmemantlr is an [ANTLR v4](http://www.antlr.org/) wrapper that automates the manual generation and compilation steps that have to be done when using vanilla ANTLR. Instead, inmemantlr does all of these steps automatically in-memory while keeping all of the original ANTLR objects accessible through its `GenericParser` class which is serializable, and hence, can be reused at a later point in time or across different applications.

Moreover, one can easily generate an abstract syntax tree (AST) from a parsed file that can be both visualized using [graphviz](http://www.graphviz.org/) and processed/translated by means of inmemantlr's `AstProcessor` class.

All of the above-mentioned inmemantlr features are illustrated by [examples](#toc).

# Status
[![Build Status](https://travis-ci.org/julianthome/inmemantlr.svg?branch=master)](https://travis-ci.org/julianthome/inmemantlr.svg?branch=master)  [![codecov](https://codecov.io/gh/julianthome/inmemantlr/branch/master/graph/badge.svg)](https://codecov.io/gh/julianthome/inmemantlr)  [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.julianthome/inmemantlr/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.julianthome/inmemantlr/badge.svg)  [![Javadoc](https://javadoc-emblem.rhcloud.com/doc/com.github.julianthome/inmemantlr/badge.svg)](http://www.javadoc.io/doc/com.github.julianthome/inmemantlr)

# TOC

[Integration](#integration)

[Usage Scenarios](#usage-scenarios)
  * [Simple parsing](#simple-parsing)
  * [AST generation](#ast-generation)
  * [AST processing](#ast-processing)
  * [Incremental parsing](#incremental-parsing)
  * [Non-combined grammars](#non-combined-grammars)
  * [Accessing ANTLR objects](#accessing-antlr-objects)
  * [Parser serialization](#parser-serialization)

[Licence](#licence)


# Integration

inmemantlr is available on maven central. One can integrate it by using the following dependency in the `pom.xml` file. Note, that the maven releases do not necessarily contain the newest changes that are available in the repository. The maven releases are kept in sync with the tagged [releases](https://github.com/julianthome/inmemantlr/releases). The API documentation for every release is avalable [here](http://www.javadoc.io/doc/com.github.julianthome/inmemantlr). However, the content of this documentation, in particular the code examples and usage scenarios, is always aligned with the master branch of this repository.

```xml
<dependency>
    <groupId>com.github.julianthome</groupId>
    <artifactId>inmemantlr</artifactId>
    <version>1.1</version>
</dependency>
```

# Usage Scenarios

The following code snippet shows an example how to use inmemantlr. The descriptions are provided as source code comments.

## Simple parsing

``` java
// 1. load grammar
File f = new File("Java.g4");
GenericParser gp = new GenericParser(f);
// 2. load file content into string
String s = FileUtils.loadFileContent("HelloWorld.java");
// 3. set listener for checking parse tree elements. Here you could use any ParseTreeListener implementation. The default listener is used per default
gp.setListener(new DefaultListener());
// 4. compile Lexer and parser in-memory
gp.compile();
try {
  // 5. parse the string that represents the content of HelloWorld.java
  ctx = gp.parse(s);
} catch (IllegalWorkflowException e) {
// ...
}
```

## AST generation

If you would like to get the derived AST from a parsed file, the following snippet could be of use:

``` java
File f = new File("Java.g4");
GenericParser gp = new GenericParser(f);
String s = FileUtils.loadFileContent("HelloWorld.java");

// this listener will create an AST from the java file
DefaultTreeListener dlist = new DefaultTreeListener();

gp.setListener(dlist);
gp.compile();

ParserRuleContext ctx = null;
try {
  ctx = gp.parse(s);
} catch (IllegalWorkflowException e) {}

// get access to AST
Ast ast = dlist.getAst();

// print AST in dot format
System.out.println(ast.toDot());
```

By providing the output of `ast.toDot()` to graphviz, one could visualize the AST as illustrated in the picture below.

<img src="https://github.com/julianthome/inmemantlr/blob/master/images/ast.png" alt="Example AST" width="400px" align="second">

## Ast processing

With inmemantlr, one can easily process or translate a given AST by means of an `AstProcessor`. The following example illustrates how one can process a simple AST that represents a mathematical expression. Given the grammar definition below, parsing the string `'3+100'` would yield this parse tree:

<img src="https://github.com/julianthome/inmemantlr/blob/master/images/simpleop.png" alt="Ast derived from simple expressoin '3+100'" width="200px" align="second">

```
grammar Ops;

Plus: '+';
Minus: '-';
Number: '-'?([0-9]|[1-9][0-9]+);

s: (expression)* EOF;
plus: Plus;
minus: Minus;
operation: plus | minus;
expression: operand operation operand;
operand: Number;

WS  :  [ \t\r\n]+ -> skip;
```

The following code example illustrates how one could compute the result of a mathematical expression based on the above-mentioned grammar.


```java
// ...
gp.compile();
// this example shows you how one could use inmemantlr for incremental parsing
Ast ast;
gp.parse("3+100");
ast = t.getAst();
// Process the AST bottom-up starting from the leafs up to the root node
AstProcessor<String, String> processor = new AstProcessor<String, String>(ast) {
  @Override
  public String getResult() {
    // when all nodes have been processed, the result is available in the smap
    // value of the root node which is returned here
    return smap.get(ast.getRoot());
  }
  @Override
  protected void initialize() {
    // initialize smap - a data structure that keeps track of the intermediate
    // values for every node
    ast.getNodes().forEach(n -> smap.put(n, n.getLabel()));
  }
  // This operation is executed for each and every node in left to right and
  // bottom up order. Non-leaf nodes are processed only if all of their siblings
  // have been already processed
  @Override
  protected void process(AstNode n) {
    if(n.getRule().equals("expression")){
      int n0 = Integer.parseInt(smap.get(n.getChild(0)));
      int n1 = Integer.parseInt(smap.get(n.getChild(2)));
      int result = 0;
      switch(smap.get(n.getChild(1))) {
        case "+":
          result = n0 + n1;
        break;
        case "-":
          result = n0 - n1;
        break;
      }
      // store computation result of addition subtraction for current node
      smap.put(n, String.valueOf(result));
    } else {
      // when node is no expression NT, propate child node value 1:1
      // to parent
      simpleProp(n);
    }
  }
};
// compute the result
processor.process();
// print the computation results which is 103
System.out.println(processor.getResult());
```


## Incremental parsing
If you have multiple strings to parse one after another, the following code snippet might be useful:

```java
File f = new File("Simple.g4");
GenericParser gp = new GenericParser(f);

// note that the listener should always be set before
// the compilation. Otherwise, the listener cannot
// capture the parsing information.
gp.setListener(new DefaultTreeListener());
gp.compile();
try {
  Ast ast;
  gp.parse("PRINT a+b");
  ast = t.getAst();
  // do something with parsing result
  gp.parse("PRINT \"test\"");
  ast = t.getAst();
  // do something with parsing resulting
  // ...
} catch (IllegalWorkflowException e) {
// ...
}
```

## Non-combined grammars

```java
// define array of ANTLR files to consider -- inmemantlr will automatically
// analyses their interdependencies
File files [] = {
  new File("MySQLLexer.g4"),
  new File("MySQLParser.g4")
};
// simply pass files to constructor
GenericParser gp = new GenericParser(files);
// parser is ready to use
```

## Accessing ANTLR objects

For accessing the ANTLR parser/lexer objects, one could use the
`getAllCompiledObjects` method which will return the source and byte code of
the source files that were generated by ANTLR and the corresponding byte code generated by inmenantlr.

```java
// get access to ANTLR objects
MemoryTupleSet set = gp.getAllCompiledObjects();
// memory tuple contains the generated source code of ANTLR
// and the associated byte code
for(MemoryTuple tup : set) {
  // get source code object
  MemorySource = tup.getSource();
  // get byte code objects
  Set<MemoryByteCode> bcode = tup.getByteCodeObjects();
}
```

## Parser serialization

For avoiding unnecessary compilation and for enabling
the re-use of a generic parser across different Java applications or runs, it is possible to serialize a generic parser.

A generic parser could be serialized to a file with the following code:
```java
try {
  // store a generic parser in the file "/tmp/gp.out" and
  // overwrite the file if it already exists
  gp.store("/tmp/gp.out", true);
} catch (SerializationException e) {
  // ...
}
```

A generic parser can be loaded from a file with the following
code:

```java
GenericParser gp = null;
try {
  // load generic parser from file /tmp/gp.out
  gp = GenericParser.load("/tmp/gp.out");
} catch (DeserializationException e) {
  // ...
}
```

# Licence
Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence. You may obtain a copy of the Licence at: https://joinup.ec.europa.eu/sites/default/files/eupl1.1.-licence-en_0.pdf

Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.
