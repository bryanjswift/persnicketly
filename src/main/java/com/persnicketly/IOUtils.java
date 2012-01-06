package com.persnicketly;

import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author bryanjswift */
public class IOUtils {
    private static Logger log = LoggerFactory.getLogger(IOUtils.class);
    private static ClassLoader cl = IOUtils.class.getClassLoader();

    private IOUtils() { }

    public static String slurp(InputStream in) {
        final StringBuilder out = new StringBuilder();
        try {
            byte[] b = new byte[4096];
            for (int n; (n = in.read(b)) != -1;) {
                out.append(new String(b, 0, n));
            }
        } catch (IOException ioe) {
            log.error("Unable to read stream contents", ioe);
        }
        return out.toString();
    }

    public static String read(String path) {
        return slurp(cl.getResourceAsStream(path));
    }

}
