import network.RoomEndpoint;
import org.glassfish.tyrus.server.Server;

import javax.websocket.DeploymentException;
import java.util.concurrent.CountDownLatch;

public class Main {
    static final int DEFAULT_PORT = 8989;

    public static void main(String[] args) throws DeploymentException, InterruptedException {
        Server server = new Server("localhost", DEFAULT_PORT, "", null, RoomEndpoint.class);
        server.start();

        System.out.println("Onyx server launched on localhost:" + DEFAULT_PORT);
        System.out.println("Endpoint = ws://localhost:" + DEFAULT_PORT + "/room/{id}");
        System.out.println();

        new CountDownLatch(1).await();
    }
}
