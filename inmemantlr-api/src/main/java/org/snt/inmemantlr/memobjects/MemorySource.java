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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URI;

/**
 * memory source object that represents a source file
 * for in-memory compilation
 */
public class MemorySource extends MemoryFile implements Serializable {

    private static final long serialVersionUID = 898301300090559769L;

    private String src;
    private String cname;

    public MemorySource() {
    }

    /**
     * constructor
     *
     * @param name class name
     * @param src  source string
     */
    public MemorySource(String name, String src) {
        super(URI.create("file:///" + name + ".java"), Kind.SOURCE);
        this.src = src;
        cname = name;
    }

    public String getClassName() {
        return cname;
    }

    /**
     * get character content
     *
     * @param ignoreEncodingErrors true to ignore encoding errors, otherwise false
     * @return source as char sequence
     */
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return src;
    }

    /**
     * open new output stream
     *
     * @return output stream
     */
    public OutputStream openOutputStream() {
        throw new IllegalStateException();
    }

    /**
     * open new input stream
     *
     * @return input stream
     */
    public InputStream openInputStream() {
        return new ByteArrayInputStream(src.getBytes());
    }

    @Override
    public String toString() {
        return cname;
    }
}
