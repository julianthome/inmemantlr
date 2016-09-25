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
    private String src;
    private String cname;


    public MemorySource(){
        super();
    }

    /**
     * constructor
     * @param name class name
     * @param src source string
     */
    public MemorySource(String name, String src) {
        super(URI.create("file:///" + name + ".java"), Kind.SOURCE);
        this.src = src;
        this.cname = name;
    }

    public String getClassName() {
        return cname;
    }

    /**
     * get character content
     * @param ignoreEncodingErrors
     * @return source as char sequence
     */
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return src;
    }

    /**
     * open new output stream
     * @return output stream
     */
    public OutputStream openOutputStream() {
        throw new IllegalStateException();
    }

    /**
     * open new input stream
     * @return input stream
     */
    public InputStream openInputStream() {
        return new ByteArrayInputStream(src.getBytes());
    }
}
