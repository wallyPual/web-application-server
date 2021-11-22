package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.RequestUtils;

public class RequestHandler extends Thread {
    private Socket connection;
    private byte[] body;
    private User newUser;

    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private static final String absolutePath = (new File("").getAbsolutePath()) + "/webapp";

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            DataOutputStream dos = new DataOutputStream(out);
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));

            String line = br.readLine();
            String requestURL = "/";

            if (line == null) return;

            while (!line.equals("")) {
                if (line.contains("GET")) {
                    requestURL = RequestUtils.getReqURL(line);
                }
                line = br.readLine();
            }

            switch (requestURL) {
                case "/":
                case "/index.html":
                    setResBody("/index.html");
                    break;
                case "/user/form.html":
                    setResBody(requestURL);
                    break;
                default:
                    if (requestURL.contains("/user/create")) {
                        Map<String, String> parsed = HttpRequestUtils.parseQueryString(requestURL.split("\\?")[1]);
                        newUser = new User(parsed.get("userId"), parsed.get("password"), parsed.get("name"), parsed.get("email"));
                        System.out.println(newUser.getUserId());
                        return;
                    }
                    body = "not found".getBytes(StandardCharsets.UTF_8);
            }

            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void setResBody(String path) throws IOException {
        body = Files.readAllBytes(new File(absolutePath + path).toPath());
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
