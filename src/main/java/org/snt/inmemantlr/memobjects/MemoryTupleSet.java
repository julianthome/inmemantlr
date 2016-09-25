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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * This object is used to store already compiled antlr lexers
 * and parsers such that they can be used later on without
 * the need to compile the whole stuff again
 */
public class MemoryTupleSet implements Serializable, Iterable <MemoryTuple> {

    final static Logger logger = LoggerFactory.getLogger(MemoryTupleSet.class);

    private Set<MemoryTuple> mts = null;

    /**
     * constructor
     */
    public MemoryTupleSet(){
        this.mts = new HashSet<MemoryTuple>();
    }

    /**
     * add a memory (source,bytecode) tuple to the list
     * @param source the source code
     * @param bytecode the corresponding bytecode
     */
    public void addMemoryTuple(MemorySource source, Set<MemoryByteCode> bytecode) {
        logger.debug("add tuple " + source.getClassName());
        this.mts.add(new MemoryTuple(source, bytecode));
    }

    public int size() {
        return this.mts.size();
    }

    @Override
    public Iterator<MemoryTuple> iterator() {
        return mts.iterator();
    }

    @Override
    public void forEach(Consumer<? super MemoryTuple> action) {
        mts.forEach(action);
    }

    @Override
    public Spliterator<MemoryTuple> spliterator() {
        return mts.spliterator();
    }


    public void addAll(MemoryTupleSet mset){
        assert(mset != null);
        this.mts.addAll(mset.mts);
    }

}
