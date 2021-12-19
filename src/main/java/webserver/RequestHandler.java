package webserver;

import java.io.*;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler extends Thread {
    private Socket connection;

    private HttpRequest request;
    private HttpResponse response;

    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            request = new HttpRequest(in);
            response = new HttpResponse(out);

            String path = request.getPath();


            Controller controller = RequestMapping.getController(path);

            if (controller == null) {
                response.forward(getDefaultPath(path));
                return;
            }
            controller.service(request, response);
        } catch (IOException | RuntimeException e) {
            log.error(e.getMessage());
        }
    }

    private static String getDefaultPath(String path) {
        if ("/".equals(path)) {
            return "/index.html";
        }
        return path;
    }
}
