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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * this class maps an antrl source file to a set of byte code
 * objects (note that this is an 1:n mapping because every inner class
 * is mapped to a distinct byte code object
 */
public class MemoryTuple implements Serializable {

    private MemorySource source;
    private Set<MemoryByteCode> bytecodeObjects;
    private String cname;

    public MemoryTuple(){
        this.bytecodeObjects = new HashSet<MemoryByteCode>();
    }

    /**
     * construct
     * @param source the source object of a give class
     * @param bytecode the bytecodeObjects derived from the source object
     */
    public MemoryTuple(MemorySource source, Set<MemoryByteCode> bytecode){
        this();
        assert(bytecode != null && bytecode.size() > 0);
        this.source = source;
        this.bytecodeObjects.addAll(bytecode);
        this.cname = source.getClassName();
    }

    /**
     * get source code
     * @return source code representation of this object
     */
    public MemorySource getSource() {
        return this.source;
    }

    /**
     * get byte code
     * @return byte code representation of this object
     */
    public Set<MemoryByteCode> getByteCodeObjects() {
        return this.bytecodeObjects;
    }

    /**
     * return class name
     * @return get class name of this object
     */
    public String getClassName() {
        return this.cname;
    }

    @Override
    public int hashCode() {
        return this.cname.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof MemoryTuple))
            return false;
        MemoryTuple mo = (MemoryTuple)o;
        return(mo.cname.equals(this.cname));
    }

    /**
     * check if this object represents the parser class
     * @return true, if this class is a parser, false otherwise
     */
    public boolean isParser() {
        return this.cname.endsWith("Parser");
    }

    /**
     * check if this object represents a base listener
     * @return true, if this class is a base listener, false otherwise
     */
    public boolean isBaseListener() {
        return this.cname.endsWith("BaseListener");
    }

    /**
     * check if this object represents a listener class
     * @return true, if this class is a listener, false otherwise
     */
    public boolean isListener() {
        return this.cname.endsWith("Listener");
    }


    /**
     * check if this object represents a lexer class
     * @return true, if this class is a lexer, false otherwise
     */
    public boolean isLexer() {
        return this.cname.endsWith("Lexer");
    }

}
