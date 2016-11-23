package org.snt.inmemantlr.comp;


import org.apache.commons.io.FilenameUtils;
import org.slf4j.LoggerFactory;
import org.snt.inmemantlr.memobjects.MemorySource;
import org.snt.inmemantlr.utils.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

public class FileProvider implements CunitProvider {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(FileProvider.class);

    private List<MemorySource> ret = new Vector();

    public void addFiles(File... files) throws FileNotFoundException {
        for (File f : files) {

            if (!f.exists())
                throw new FileNotFoundException("file " + f.getAbsolutePath()
                        + " does not exist");

            String name = FilenameUtils.removeExtension(f.getName());

            name = f.getAbsolutePath().replace("/", ".") + name;
            LOGGER.debug("NAME {} ", name);

            System.exit(-1);

            ret.add(new MemorySource(FilenameUtils.removeExtension(f.getName
                    ()), FileUtils
                    .loadFileContent
                            (f)));
        }
    }

    @Override
    public Collection<MemorySource> getItems() {
        return ret;
    }
}
