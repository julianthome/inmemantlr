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

package org.snt.inmemantlr.comp;

import org.snt.inmemantlr.memobjects.MemoryByteCode;

import javax.tools.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * file manager for in-memory compilation
 */
class SpecialJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {

    private SpecialClassLoader xcl;
    private HashMap<String, MemoryByteCode> mb = new HashMap<>();

    /**
     * constructor
     *
     * @param sjfm a StandardJavaFileManager
     * @param xcl  a SpecialClassLoader
     */
    public SpecialJavaFileManager(StandardJavaFileManager sjfm, SpecialClassLoader xcl) {
        super(sjfm);
        this.xcl = xcl;
    }

    /**
     * get a java file (memory byte code)
     *
     * @param location path
     * @param name     filename
     * @param kind     file kind
     * @param sibling  file sibling
     * @return memory byte code object
     * @throws IOException if an error occurs getting the java file
     */
    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String name,
                                               JavaFileObject.Kind kind,
                                               FileObject sibling) throws IOException {
        MemoryByteCode mbc = new MemoryByteCode(name);
        // bookkeeping of memory bytecode
        mb.put(mbc.getClassName(), mbc);
        xcl.addClass(mbc);
        return mbc;
    }

    /**
     * get special class loader
     *
     * @param location file location
     * @return class loader
     */
    public ClassLoader getClassLoader(Location location) {
        return xcl;
    }

    /**
     * get the bytecode of a class
     *
     * @param cname the name of the class for which one would like to get the bytecode
     * @return the bytecode of class cname
     */
    public Set<MemoryByteCode> getByteCodeFromClass(String cname) {
        Set<MemoryByteCode> ret = mb.values().stream()
                .filter(m -> m.getClassName().matches("(([a-zA-Z_0-9]+)/)*" + cname + "(\\$.*)?"))
                .collect(toSet());
        if (ret.isEmpty())
            throw new IllegalArgumentException("bytecode of class " + cname + " is empty");

        return ret;
    }
}
