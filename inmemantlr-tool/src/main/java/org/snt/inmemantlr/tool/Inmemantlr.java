 /**
 * org.snt.inmemantlr.tool.Inmemantlr - In memory compiler for Antlr 4
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

 package org.snt.inmemantlr.tool;

 import org.apache.commons.cli.*;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.FilenameUtils;
 import org.slf4j.LoggerFactory;
 import org.snt.inmemantlr.GenericParser;
 import org.snt.inmemantlr.exceptions.CompilationException;
 import org.snt.inmemantlr.exceptions.IllegalWorkflowException;
 import org.snt.inmemantlr.exceptions.ParsingException;
 import org.snt.inmemantlr.listener.DefaultTreeListener;
 import org.snt.inmemantlr.tree.Ast;

 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.stream.Collectors;

/**
 * inmemantlr command line tool
 */
public class Inmemantlr {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Inmemantlr.class);

    private static Set<File> getFilesForOption(CommandLine cmd, String opt) {
        Set<File> ret = new HashSet<>();
        if (cmd.hasOption(opt)) {
            Set<String> us = new HashSet<>();
            us.addAll(Arrays.asList(cmd.getOptionValues(opt)));
            ret.addAll(us.stream().map(File::new).collect(Collectors.toSet()));
        }
        return ret;
    }

    public static void main(String[] args) {
        LOGGER.info("org.snt.inmemantlr.tool.Inmemantlr tool");

        HelpFormatter hformatter = new HelpFormatter();

        Options options = new Options();

        // Binary arguments
        options.addOption("h", "print this message");

        Option grmr = Option.builder()
                .longOpt("grmrfiles")
                .hasArgs()
                .desc("comma-separated list of ANTLR files")
                .required(true)
                .argName("grmrfiles")
                .type(String.class)
                .valueSeparator(',')
                .build();

        Option infiles = Option.builder()
                .longOpt("infiles")
                .hasArgs()
                .desc("comma-separated list of files to parse")
                .required(true)
                .argName("infiles")
                .type(String.class)
                .valueSeparator(',')
                .build();

        Option utilfiles = Option.builder()
                .longOpt("utilfiles")
                .hasArgs()
                .desc("comma-separated list of utility files to be added for " +
                        "compilation")
                .required(false)
                .argName("utilfiles")
                .type(String.class)
                .valueSeparator(',')
                .build();

        Option odir = Option.builder()
                .longOpt("outdir")
                .desc("output directory in which the dot files will be " +
                        "created")
                .required(false)
                .hasArg(true)
                .argName("outdir")
                .type(String.class)
                .build();

        options.addOption(infiles);
        options.addOption(grmr);
        options.addOption(utilfiles);
        options.addOption(odir);

        CommandLineParser parser = new DefaultParser();

        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption('h')) {
                hformatter.printHelp("java -jar inmemantlr.jar", options);
                System.exit(0);
            }
        } catch (ParseException e) {
            hformatter.printHelp("java -jar inmemantlr.jar", options);
            LOGGER.error(e.getMessage());
            System.exit(-1);
        }

        // input files
        Set<File> ins = getFilesForOption(cmd, "infiles");
        // grammar files
        Set<File> gs = getFilesForOption(cmd, "grmrfiles");
        // utility files
        Set<File> uf = getFilesForOption(cmd, "utilfiles");
        // output dir
        Set<File> od = getFilesForOption(cmd, "outdir");

        if (od.size() > 1) {
            LOGGER.error("output directories must be less than or equal to 1");
            System.exit(-1);
        }

        if (ins.size() <= 0) {
            LOGGER.error("no input files were specified");
            System.exit(-1);
        }

        if (gs.size() <= 0) {
            LOGGER.error("no grammar files were specified");
            System.exit(-1);
        }

        LOGGER.info("create generic parser");
        GenericParser gp = null;
        try {
            gp = new GenericParser(gs.toArray(new File[gs.size()]));
        } catch (FileNotFoundException e) {
            LOGGER.error(e.getMessage());
            System.exit(-1);
        }

        if (!uf.isEmpty()) {
            try {
                gp.addUtilityJavaFiles(uf.toArray(new String[uf.size()]));
            } catch (FileNotFoundException e) {
                LOGGER.error(e.getMessage());
                System.exit(-1);
            }
        }

        LOGGER.info("create and add parse tree listener");
        DefaultTreeListener dt = new DefaultTreeListener();
        gp.setListener(dt);

        LOGGER.info("compile generic parser");
        try {
            gp.compile();
        } catch (CompilationException e) {
            LOGGER.error("cannot compile generic parser: {}", e.getMessage());
            System.exit(-1);
        }

        String fpfx = "";
        for (File of : od) {
            if (!of.exists() || !of.isDirectory()) {
                LOGGER.error("output directory does not exist or is not a " +
                        "directory");
                System.exit(-1);
            }
            fpfx = of.getAbsolutePath();
        }

        Ast ast;
        for (File f : ins) {
            try {
                gp.parse(f);
            } catch (IllegalWorkflowException | FileNotFoundException |
                    ParsingException e) {
                LOGGER.error(e.getMessage());
                System.exit(-1);
            }
            ast = dt.getAst();

            if (!fpfx.isEmpty()) {
                String of = fpfx + "/" + FilenameUtils.removeExtension(f.getName()) + ".dot";

                LOGGER.info("write file {}", of);

                try {
                    FileUtils.writeStringToFile(new File(of), ast.toDot(), "UTF-8");
                } catch (IOException e) {
                    LOGGER.error(e.getMessage());
                    System.exit(-1);
                }
            } else {
                LOGGER.info("Tree for {} \n {}", f.getName(), ast.toDot());
            }
        }

        System.exit(0);
    }
}
