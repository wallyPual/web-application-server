package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class HttpResponse {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private DataOutputStream dos;
    private Map<String, String> headers = new HashMap<>();

    HttpResponse(OutputStream out) {
        this.dos = new DataOutputStream(out);
    }

    public void forward(String path) {
        try {
            byte[] body = Files.readAllBytes(new File("./webapp" + path).toPath());

            if (path.endsWith(".html")) {
                addHeader("Content-Type", "text/html");
            } else if (path.endsWith(".css")) {
                addHeader("Content-Type", "text/css");
            } else if (path.endsWith(".js")) {
                addHeader("Content-Type", "text/javascript");
            }
            addHeader("Content-Length", Integer.toString(body.length));
            response200Header();
            responseBody(body);
        } catch (IOException e) {
        }
    }

    public void sendRedirect(String path) {
        addHeader("Location", path);
        response302Header();
    }

    public void forwardBody(String body) {
        byte[] html = body.getBytes(StandardCharsets.UTF_8);
        addHeader("Content-Type: ", "text/html");
        addHeader("Content-Length: ", Integer.toString(html.length));
        response200Header();
        responseBody(html);
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    private void processHeader() {
        headers.forEach((k, v) -> {
            try {
                dos.writeBytes(k + ": " + v + "\r\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void response200Header() {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            processHeader();
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header() {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            processHeader();
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
