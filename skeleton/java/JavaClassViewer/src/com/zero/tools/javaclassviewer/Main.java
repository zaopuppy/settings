package com.zero.tools.javaclassviewer;

import com.zero.tools.javaclassviewer.ui.MainForm;
import org.apache.commons.javaflow.bytecode.transformation.asm.AsmClassTransformer;
import org.apache.commons.javaflow.utils.RewritingUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

/**
 * Created by zero on 5/1/16.
 */
public class Main {

    private static void log(String msg) {
        System.out.println(msg + '\n');
    }

    public static void main(String[] args) throws Exception {
        log("Just a test");
        log("hascode: " + "".hashCode());

        MainForm form = new MainForm("Java Class Viewer");

        // rewrite();
    }

    private static void rewrite() {

        String srcDir = "a";
        String dstDir = "b";

        String[] fileNames = new String[] { "TestContinuation.class" };

        AsmClassTransformer transformer = new AsmClassTransformer();
        try {
            for (final String fileName : fileNames) {
                final File source = new File(srcDir, fileName);
                final File destination = new File(dstDir, fileName);

                if (!destination.getParentFile().exists()) {
                    log("Creating dir: " + destination.getParentFile());
                    destination.getParentFile().mkdirs();
                }

                if (source.lastModified() < destination.lastModified()) {
                    log("Omitting " + source + " as " + destination + " is up to date");
                    continue;
                }

                if (fileName.endsWith(".class")) {
                    log("Rewriting " + source + " to " + destination);
                    // System.out.println("Rewriting " + source);

                    RewritingUtils.rewriteClassFile( source, transformer, destination );
                }

                if (fileName.endsWith(".jar")
                        || fileName.endsWith(".ear")
                        || fileName.endsWith(".zip")
                        || fileName.endsWith(".war")) {

                    log("Rewriting " + source + " to " + destination);

                    RewritingUtils.rewriteJar(
                            new JarInputStream(new FileInputStream(source)),
                            transformer,
                            new JarOutputStream(new FileOutputStream(destination))
                    );

                }
            }
        } catch (IOException e) {
            log(Arrays.toString(e.getStackTrace()));
        }
    }
}
