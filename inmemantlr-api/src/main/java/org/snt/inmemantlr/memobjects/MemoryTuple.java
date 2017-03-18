/**
 * Inmemantlr - In memory compiler for Antlr 4
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Julian Thome <julian.thome.de@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 **/

package org.snt.inmemantlr.memobjects;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * this class maps an ANTLR source file to a set of byte code
 * objects (note that this is an 1:n mapping because every inner class
 * is mapped to a distinct byte code object
 */
public class MemoryTuple implements Serializable {

    private static final long serialVersionUID = 3586252835232647406L;

    private MemorySource source;
    private Set<MemoryByteCode> bytecodeObjects = new HashSet<>();
    ;
    private String cname;

    public MemoryTuple() {
    }

    /**
     * constructor
     *
     * @param source   the source object of a give class
     * @param bytecode the bytecodeObjects derived from the source object
     */
    public MemoryTuple(MemorySource source, Set<MemoryByteCode> bytecode) {
        this();
        if (bytecode == null || bytecode.isEmpty())
            throw new IllegalArgumentException("bytecode set must not be null or empty");

        this.source = source;
        bytecodeObjects.addAll(bytecode);
        cname = source.getClassName();
    }

    /**
     * get source code
     *
     * @return source code representation of this object
     */
    public MemorySource getSource() {
        return source;
    }

    /**
     * get byte code
     *
     * @return byte code representation of this object
     */
    public Set<MemoryByteCode> getByteCodeObjects() {
        return bytecodeObjects;
    }

    /**
     * return class name
     *
     * @return get class name of this object
     */
    public String getClassName() {
        return cname;
    }

    @Override
    public int hashCode() {
        return cname.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MemoryTuple))
            return false;
        MemoryTuple mo = (MemoryTuple) o;
        return mo.cname.equals(cname);
    }

    /**
     * check if this object represents the parser class
     *
     * @return true, if this class is a parser, false otherwise
     */
    public boolean isParser() {
        return cname.endsWith("Parser");
    }

    /**
     * check if this object represents a base listener
     *
     * @return true, if this class is a base listener, false otherwise
     */
    public boolean isBaseListener() {
        return cname.endsWith("BaseListener");
    }

    /**
     * check if this object represents a listener class
     *
     * @return true, if this class is a listener, false otherwise
     */
    public boolean isListener() {
        return cname.endsWith("Listener");
    }

    /**
     * check if this object represents a lexer class
     *
     * @return true, if this class is a lexer, false otherwise
     */
    public boolean isLexer() {
        return cname.endsWith("Lexer");
    }
}
