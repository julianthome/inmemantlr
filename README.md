# inmemantlr
In Memory compilation of ANTLR grammars. With inmemantlr, you can test your
ANTLR grammars without the need to generate .java files first. Hence, it is
especially useful for testing purposes. Note that inmenantlr is using ANTLR v4.

# Status
[![Build Status](https://travis-ci.org/julianthome/inmemantlr.svg?branch=master)](https://travis-ci.org/julianthome/inmemantlr.svg?branch=master)

# Example

The following code snippet shows an example how to use inmemantlr. The descriptions are provided as source code comments.

``` java
// 1. load grammar
GenericParser gp = new GenericParser("src/test/java/org/snt/inmemantlr/Java.g4", "Java");
// 2. load file (HelloWorld.java) to parse
byte[] bytes = null;
try {
  RandomAccessFile f = new RandomAccessFile("src/test/java/org/snt/inmemantlr/HelloWorld.java", "r");
  bytes = new byte[(int)f.length()];
  f.read(bytes);
} catch (IOException e) {
  e.printStackTrace();
}
// 3. create a string
String s = new String(bytes);
// 4. set listener for checking parse tree elements. Here you could use any ParseTreeListener implementation.
gp.setListener(new DefaultListener());
// 5. compile lexer and parser in-memory
gp.compile();
try {
  // 6. parse the string that represents the content of HelloWorld.java
  ctx = gp.parse(s);
} catch (IllegalWorkflowException e) {
// ...
}
```
