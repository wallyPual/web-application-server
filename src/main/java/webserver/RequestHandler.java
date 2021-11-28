package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.*;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
    private Socket connection;
    private byte[] body;
    private User newUser;
    private String method;
    private String url;
    private Map<String, String> headers = new HashMap<>();


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

            if (line == null) return;

            String[] firstLine = line.split(" ");

            method = firstLine[0];
            url = firstLine[1];

            while (!"".equals((line = br.readLine()))) {
                String[] tokens = line.split(": ");
                if (tokens.length == 2) headers.put(tokens[0], tokens[1]);
            }

            if (method.equals("GET")) {
                switch (url) {
                    case "/user/login.html":
                        Boolean logined = false;
                        if (headers.get("Cookie") != null) {
                            logined = Boolean.parseBoolean(util.HttpRequestUtils.parseCookies(headers.get("Cookie")).get("logined"));
                        }

                        if (logined) {
                            System.out.println("이미 로그인 된 상태입니다.");
                            response302Header(dos, "/index.html");
                            return;
                        }
                    default:
                        setResBody(url);
                        response200Header(dos, body.length);
                        responseBody(dos, body);
                }
            }
            if (method.equals("POST")) {
                switch (url) {
                    case "/user/create":
                        Map<String, String> parsed = HttpRequestUtils.parseQueryString(IOUtils.readData(br, Integer.parseInt(headers.get("Content-Length"))));
                        newUser = new User(parsed.get("userId"), parsed.get("password"), parsed.get("name"), parsed.get("email"));
                        DataBase.addUser(newUser);
                        response302Header(dos, "/index.html");
                        break;
                    case "/user/login":
                        Map<String, String> loginInfo = HttpRequestUtils.parseQueryString(IOUtils.readData(br, Integer.parseInt(headers.get("Content-Length"))));
                        User findDBUser = DataBase.findUserById(loginInfo.get("userId"));
                        System.out.println("findDBUser: " + findDBUser);
                        if (findDBUser == null) {
                            reponseLoginHeader(dos, false);
                            return;
                        }
                        if (findDBUser.getPassword().equals(loginInfo.get("password"))) {
                            System.out.println("로그인 성공");
                            reponseLoginHeader(dos, true);
                        };
                        break;
                }
            }
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

    private void response302Header(DataOutputStream dos, String url) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: " + url + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    };

    private void reponseLoginHeader(DataOutputStream dos, Boolean isLogin) {
        try {
            String redirectURL = isLogin ? "/index.html" : "/user/login_failed.html";
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: " + redirectURL + "\r\n");
            dos.writeBytes("Set-Cookie: logined=" + isLogin + "\r\n");
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
