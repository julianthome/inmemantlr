package org.snt.inmemantlr.comp;


import org.snt.inmemantlr.memobjects.MemorySource;

import java.util.Collection;

public interface CunitProvider {
    Collection<MemorySource> getItems();
}
