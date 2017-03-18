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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snt.inmemantlr.memobjects.MemoryByteCode;

import java.util.HashMap;
import java.util.Map;

/**
 * extended class loader
 */
class SpecialClassLoader extends ClassLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpecialClassLoader.class);

    private Map<String, MemoryByteCode> m = new HashMap<>();

    public SpecialClassLoader(ClassLoader parent) {
        super(parent);
    }

    /**
     * find a class that is already loaded
     *
     * @param name class name
     * @return the actual class
     * @throws ClassNotFoundException if the class could not be found
     */
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        MemoryByteCode mbc = m.get(name);
        if (mbc == null) {
            mbc = m.get(name.replace(".", "/"));
            if (mbc == null) {
                LOGGER.error("Could not find {}", name);
                return super.findClass(name);
            }
        }
        byte[] bseq = mbc.getBytes();
        return defineClass(name, bseq, 0, bseq.length);
    }

    /**
     * add class to class loader
     *
     * @param mbc representation
     */
    public void addClass(MemoryByteCode mbc) {
        m.put(mbc.getClassName(), mbc);
    }

}
