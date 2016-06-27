package org.snt.inmemantlr.tree;


import edu.emory.mathcs.backport.java.util.Arrays;
import edu.emory.mathcs.backport.java.util.Collections;

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

