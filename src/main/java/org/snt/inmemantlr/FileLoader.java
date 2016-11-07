package org.snt.inmemantlr;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;


public class FileLoader extends URLClassLoader {
    public FileLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public FileLoader(URL[] urls) {
        super(urls);
    }

    public FileLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
    }

    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }

}
