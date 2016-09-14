# inmemantlr
With inmemantlr you can test your
ANTLR grammars directly without the need to generate and compile .java files first, which is done automatically and in-memory by the tool.

Moreover, it automatically generates an abstract syntax tree (AST) from a parsed file that can be easily visualized using graphviz.

inmemantlr is intended to assist you in the process of developing your context-free grammar by providing automated (re-)generation of Parser and Lexer, and by allowing you to visualize the resulting AST. Note that inmenantlr is using ANTLR v4.

# Status
[![Build Status](https://travis-ci.org/julianthome/inmemantlr.svg?branch=master)](https://travis-ci.org/julianthome/inmemantlr.svg?branch=master)  [![codecov](https://codecov.io/gh/julianthome/inmemantlr/branch/master/graph/badge.svg)](https://codecov.io/gh/julianthome/inmemantlr)  [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.julianthome/inmemantlr/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.julianthome/inmemantlr/badge.svg)  [![Javadoc](https://javadoc-emblem.rhcloud.com/doc/com.github.julianthome/inmemantlr/badge.svg)](http://www.javadoc.io/doc/com.github.julianthome/inmemantlr)

# Integration

inmemantlr is available on maven central. One can integrate it by using the following depenency in the `pom.xml` file.

```xml
<dependency>
    <groupId>com.github.julianthome</groupId>
    <artifactId>inmemantlr</artifactId>
    <version>1.0</version>
</dependency>
```

# Usage

The following code snippet shows an example how to use inmemantlr. The descriptions are provided as source code comments.

``` java
// 1. load grammar
File f = new File("src/test/ressources/Java.g4");
GenericParser gp = new GenericParser(f, "Java");
// 2. load file content into string
String s = FileUtils.loadFileContent("src/test/ressources/HelloWorld.java");
// 3. set listener for checking parse tree elements. Here you could use any ParseTreeListener implementation.
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

If you want to get the AST from a parsed file, the following snippet could be of use:

``` java
File f = new File("src/test/ressources/Java.g4");
GenericParser gp = new GenericParser(f, "Java");
String s = FileUtils.loadFileContent("src/test/ressources/HelloWorld.java");

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

<img src="https://github.com/julianthome/inmemantlr/blob/master/images/ast.png" alt="Example AST" width="400px" align="second">


# Licence
Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence. You may obtain a copy of the Licence at: https://joinup.ec.europa.eu/sites/default/files/eupl1.1.-licence-en_0.pdf

Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.
