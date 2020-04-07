package onyx;

import onyx.network.RoomEndpoint;
import org.glassfish.tyrus.server.Server;

import javax.websocket.DeploymentException;
import java.util.concurrent.CountDownLatch;

public class Main {
    static final int DEFAULT_PORT = 8889;
    public static int SERVER_PORT;
    public static String AI_PATH;

    public static void main(String[] args) throws DeploymentException, InterruptedException {
        if(args.length == 0 || args.length > 2) {
            System.err.println("Bad arguments : <ai path> [server port : default 8889]");
            System.exit(1);
        }

        AI_PATH = args[0];
        if(args.length > 1) SERVER_PORT = Integer.parseInt(args[1]);
        else SERVER_PORT = DEFAULT_PORT;

        Server server = new Server("localhost", SERVER_PORT, "", null, RoomEndpoint.class);
        server.start();

        System.out.println("Onyx server launched on localhost:" + SERVER_PORT);
        System.out.println("Endpoint = ws://localhost:" + SERVER_PORT + "/room/{id}");
        System.out.println();

        new CountDownLatch(1).await();
    }
}
