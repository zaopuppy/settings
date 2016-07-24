package com.zero.util.log;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Created by zhaoyi on 7/21/16.
 */
public class Log {

    private static final Charset DEFAULT_LOCALE = Charset.forName("UTF-8");

    private static volatile OutputStream mOutStream = System.out;

    public static void setLogger(OutputStream out) {
        mOutStream = out;
    }

    public static void d(String msg) {
        log("DEBUG|" + msg);
    }

    public static void i(String msg) {
        log("INFO|" + msg);
    }

    public static void w(String msg) {
        log("WARN|" + msg);
    }

    public static void e(String msg) {
        log("ERRO|" + msg);
    }

    private static void log(String msg) {
        // System.out.println(msg);
        try {
            mOutStream.write(msg.getBytes(DEFAULT_LOCALE));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
