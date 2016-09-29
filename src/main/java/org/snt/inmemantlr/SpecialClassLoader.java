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

package org.snt.inmemantlr;

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
