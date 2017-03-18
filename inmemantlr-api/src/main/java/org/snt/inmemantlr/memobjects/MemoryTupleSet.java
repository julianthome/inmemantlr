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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * this object is used to store already compiled antlr lexers
 * and parsers such that they can be used later on without
 * the need to compile the whole stuff again
 */
public class MemoryTupleSet implements Serializable, Iterable<MemoryTuple> {

    private static final long serialVersionUID = -1187957244085829285L;
    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryTupleSet.class);

    private Set<MemoryTuple> mts = new HashSet<>();

    /**
     * add a memory (source, bytecode) tuple to the list
     *
     * @param source   the source code
     * @param bytecode the corresponding bytecode
     */
    public void addMemoryTuple(MemorySource source, Set<MemoryByteCode> bytecode) {
        LOGGER.debug("add tuple {}", source.getClassName());
        mts.add(new MemoryTuple(source, bytecode));
    }

    public int size() {
        return mts.size();
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

    public void addAll(MemoryTupleSet mset) {
        mts.addAll(mset.mts);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        mts.stream().forEach(
                t -> {
                    MemorySource s = t.getSource();
                    Set<MemoryByteCode> bs = t.getByteCodeObjects();
                    sb.append(s.getClassName()).append(":\n");
                    bs.forEach(b -> sb.append(b.getClassName()).append(" "));
                    sb.append("\n");
                }
        );

        return sb.toString();
    }
}
