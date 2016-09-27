/*
* Inmemantlr - In memory compiler for Antlr 4
*
* Copyright 2016, Julian Thomé <julian.thome@uni.lu>
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved by
* the European Commission - subsequent versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence. You may
* obtain a copy of the Licence at:
*
* https://joinup.ec.europa.eu/sites/default/files/eupl1.1.-licence-en_0.pdf
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the Licence is distributed on an "AS IS" basis, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and
* limitations under the Licence.
*/

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

    private transient ByteArrayOutputStream baos;
    private String cname;
    private byte [] bytebuf = null;


    public MemoryByteCode(){
        super();
    }

    /**
     * constructor
     * @param name class name
     */
    public MemoryByteCode(String name) {
        super(URI.create("byte:///" + name + ".class"), Kind.CLASS);
        this.cname = name;
    }

    /**
     * get byte code content as character sequence
     * @param ignoreEncodingErrors flag if encoding errors should be ignored
     * @return character sequence of memory byte code
     */
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return new String(this.getBytes());
    }

    /**
     * open new output stream
     * @return output stream
     */
    public OutputStream openOutputStream() {
        baos = new ByteArrayOutputStream();
        return baos;
    }

    /**
     * open new input stream
     * @return input stream
     */
    public InputStream openInputStream() {
        throw new IllegalStateException();
    }

    /**
     * return byte code as byte sequence
     * @return byte array
     */
    public byte[] getBytes() {
        if(bytebuf == null)
            bytebuf = baos.toByteArray();

        return bytebuf;
    }

    /**
     * return the class name of this object
     * @return the class name
     */
    public String getClassName() {
        return this.cname;
    }

    public boolean isInnerClass() {
        return this.cname.contains("$");
    }

    @Override
    public int hashCode() {
        return this.getClassName().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof MemoryByteCode))
            return false;

        MemoryByteCode mb = (MemoryByteCode)o;
        return mb.getClassName().equals(this.cname);
    }


}
