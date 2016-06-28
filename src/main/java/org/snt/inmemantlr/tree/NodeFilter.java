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

package org.snt.inmemantlr.tree;

import edu.emory.mathcs.backport.java.util.Arrays;

import java.util.HashSet;
import java.util.Set;

public class NodeFilter {

    private Set<String> typset = null;

    public NodeFilter(String [] filter){
        typset = new HashSet<String>();
        typset.addAll(Arrays.asList(filter));
    }

    public NodeFilter(){
        typset = new HashSet<String>();
    }

    public String addType(String s) {
        this.typset.add(s);
        return s;
    }

    public boolean hasType(String s) {
        return this.typset.contains(s);
    }

    public boolean isEmpty() {
        return this.typset.size() == 0;
    }
}

