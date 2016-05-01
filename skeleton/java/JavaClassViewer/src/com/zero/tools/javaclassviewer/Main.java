package com.zero.tools.javaclassviewer;

import com.zero.tools.javaclassviewer.ui.MainForm;

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
    }
}
