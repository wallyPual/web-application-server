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

            String line;
            String getURL = "";
            String postURL = "";

            while ((line = br.readLine()) != null) {
                if (line.contains("GET")) {
                    getURL = RequestUtils.getReqURL(line);
                }
                if (line.contains("POST")) {
                    postURL = RequestUtils.getReqURL(line);
                }

                System.out.println(line);
            }

            switch (getURL) {
                case "/":
                case "/index.html":
                    setResBody("/index.html");
                    break;
                case "/user/form.html":
                    setResBody(getURL);
                    break;
                default:
                    if (getURL.contains("/user/create")) {
                        Map<String, String> parsed = RequestUtils.queryToMap(getURL);
                        newUser = new User(parsed.get("userId"), parsed.get("password"), parsed.get("name"), parsed.get("email"));
                        System.out.println(newUser.getUserId());
                        return;
                    }
                    body = "not found".getBytes(StandardCharsets.UTF_8);
            }

            switch (postURL) {
                case "/user/form.html":
                    return;
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
