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

/**
 * An object that is used for serializing a generic
 * parser that is already compiled
 */
public class GenericParserSerialize implements Serializable {

    private static final long serialVersionUID = -8824622790097111310L;

    private MemoryTupleSet mset;
    private String parserName;
    private String lexerName;


    /**
     * constructor
     *
     * @param mset set of source/byte code tuples
     */
    public GenericParserSerialize(MemoryTupleSet mset, String parserName,
                                  String lexerName) {
        assert mset != null && mset.size() > 0;
        this.mset = mset;
        this.parserName = parserName;
        this.lexerName = lexerName;
    }

    public MemoryTupleSet getMemoryTupleSet() {
        return mset;
    }

    public String getParserName() {
        return parserName;
    }

    public String getLexerName() {
        return lexerName;
    }

}
