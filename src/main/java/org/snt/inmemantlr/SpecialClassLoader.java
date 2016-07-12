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

import java.util.HashMap;
import java.util.Map;

class SpecialClassLoader extends ClassLoader {
    private Map<String, MemoryByteCode> m = new HashMap<String, MemoryByteCode>();

    protected Class<?> findClass(String name) throws ClassNotFoundException {
        MemoryByteCode mbc = m.get(name);
        if (mbc == null) {
            mbc = m.get(name.replace(".", "/"));
            if (mbc == null) {
                return super.findClass(name);
            }
        }
        return defineClass(name, mbc.getBytes(), 0, mbc.getBytes().length);
    }

    public void addClass(String name, MemoryByteCode mbc) {
        m.put(name, mbc);
    }
}
