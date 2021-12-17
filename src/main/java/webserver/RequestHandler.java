package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler extends Thread {
    private Socket connection;
    private byte[] body;
    private User newUser;
    private Boolean logined = false;
    private String contentType;
    private String path;
    private String methods;


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
            HttpRequest request = new HttpRequest(in);

            this.logined = isLoggedIn(request.getHeader("Cookie"));
            this.contentType = getContentType(request.getPath());
            this.path = request.getPath();
            this.methods = request.getMethods();

            if (methods.equals("GET")) {
                switch (path) {
                    case "/user/login.html":
                        // 로그인 한 경우 홈으로 보냄
                        if (logined) {
                            log.warn("이미 로그인 된 상태입니다.");
                            response302Header(dos, "/index.html");
                            return;
                        }
                        setResBody(request.getPath());
                        response200Header(dos, body.length);
                        responseBody(dos, body);
                        break;
                    case "/user/list":
                        // 로그인 하지 않은 경우 로그인 페이지로 리다이렉트
                        if (!logined) response302Header(dos, "/user/login.html");
                        Iterator users = DataBase.findAll().iterator();

                        StringBuilder sb = new StringBuilder();

                        for (Iterator it = users; it.hasNext(); ) {
                            User user = (User) it.next();
                            sb.append("<div>" + user.getUserId() + "</div>");
                        }

                        byte[] html = sb.toString().getBytes(StandardCharsets.UTF_8);
                        response200Header(dos, html.length);
                        responseBody(dos, html);
                        break;
                    default:
                        setResBody(request.getPath());
                        response200Header(dos, body.length);
                        responseBody(dos, body);
                }
            } else if (methods.equals("POST")) {
                switch (path) {
                    case "/user/create":
                        newUser = new User(
                                request.getParameter("userId"),
                                request.getParameter("password"),
                                request.getParameter("name"),
                                request.getParameter("email"));
                        DataBase.addUser(newUser);
                        log.info("회원가입 완료");
                        response302Header(dos, "/index.html");
                        break;
                    case "/user/login":
                        User findDBUser = DataBase.findUserById(request.getParameter("userId"));
                        if (findDBUser == null || !findDBUser.getPassword().equals(request.getParameter("password"))) {
                            reponseLoginHeader(dos, false);
                            return;
                        }
                        log.info("로그인 성공");
                        reponseLoginHeader(dos, true);
                        break;
                }
            }
        } catch (IOException | RuntimeException e) {
            log.error(e.getMessage());
        }
    }

    private String getContentType(String path) {
        if (path.endsWith(".css")) {
            return "text/css";
        }
        if (path.endsWith(".js")) {
            return "text/javascript";
        }
        return "text/html";
    }

    private Boolean isLoggedIn(String Cookie) {
        return Boolean.parseBoolean(util.HttpRequestUtils.parseCookies(Cookie).get("logined"));
    }

    private void setResBody(String path) throws IOException {
        body = Files.readAllBytes(new File(absolutePath + path).toPath());
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: " + contentType + ";charset=utf-8\r\n");
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
    }

    ;

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
