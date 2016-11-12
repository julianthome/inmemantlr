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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URI;

/**
 * a representation of byte code in memory
 */
public class MemoryByteCode extends MemoryFile implements Serializable {

    private static final long serialVersionUID = 2268365481259432191L;

    private transient ByteArrayOutputStream baos;
    private String cname;
    private byte[] bytebuf = null;

    public MemoryByteCode() {
    }

    /**
     * constructor
     *
     * @param name class name
     */
    public MemoryByteCode(String name) {
        super(URI.create("byte:///" + name + ".class"), Kind.CLASS);
        cname = name;
    }

    /**
     * get byte code content as character sequence
     *
     * @param ignoreEncodingErrors flag if encoding errors should be ignored
     * @return character sequence of memory byte code
     */
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return new String(getBytes());
    }

    /**
     * open new output stream
     *
     * @return output stream
     */
    public OutputStream openOutputStream() {
        baos = new ByteArrayOutputStream();
        return baos;
    }

    /**
     * open new input stream
     *
     * @return input stream
     */
    public InputStream openInputStream() {
        throw new IllegalStateException();
    }

    /**
     * return byte code as byte sequence
     *
     * @return byte array
     */
    public byte[] getBytes() {
        if (bytebuf == null)
            bytebuf = baos.toByteArray();

        return bytebuf;
    }

    /**
     * return the class name of this object
     *
     * @return the class name
     */
    public String getClassName() {
        return cname;
    }

    public boolean isInnerClass() {
        return cname.contains("$");
    }

    @Override
    public int hashCode() {
        return getClassName().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MemoryByteCode))
            return false;

        MemoryByteCode mb = (MemoryByteCode) o;
        return mb.getClassName().equals(cname);
    }
}
