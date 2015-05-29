package nanohttp;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerRunner {

    /**
     * logger to log to.
     */
    private static final Logger LOG = Logger.getLogger(ServerRunner.class.getName());

    public static void executeInstance(NanoHTTPD server) {
        try {
            server.start();
        } catch (IOException ioe) {
            System.err.println("Couldn't start server:\n" + ioe);
        }
    }

    public static <T extends NanoHTTPD> void run(Class<T> serverClass) {
        try {
            executeInstance(serverClass.newInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}