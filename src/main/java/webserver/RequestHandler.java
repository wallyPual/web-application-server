package webserver;

import java.io.*;
import java.net.Socket;
import java.util.*;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler extends Thread {
    private Socket connection;
    private User newUser;
    private Boolean logined = false;
    private String path;
    private String methods;

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

            this.logined = isLoggedIn(request.getHeader("Cookie"));
            this.path = request.getPath();
            this.methods = request.getMethods();

            if (methods.equals("GET")) {
                switch (path) {
                    case "/user/login.html":
                        // 로그인 한 경우 홈으로 보냄
                        if (logined) {
                            log.warn("이미 로그인 된 상태입니다.");
                            response.sendRedirect("/index.html");
                            return;
                        }
                        response.forward(path);
                        break;
                    case "/user/list":
                        // 로그인 하지 않은 경우 로그인 페이지로 리다이렉트
                        if (!logined) response.sendRedirect("/user/login.html");

                        Iterator users = DataBase.findAll().iterator();

                        StringBuilder sb = new StringBuilder();

                        for (Iterator it = users; it.hasNext(); ) {
                            User user = (User) it.next();
                            sb.append("<div>" + user.getUserId() + "</div>");
                        }
                        response.forwardBody(sb.toString());
                        break;
                    default:
                        response.forward(path);
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
                        response.sendRedirect("/index.html");
                        break;
                    case "/user/login":
                        User findDBUser = DataBase.findUserById(request.getParameter("userId"));
                        if (findDBUser == null || !findDBUser.getPassword().equals(request.getParameter("password"))) {
                            response.addHeader("Set-Cookie", "logined=false");
                            response.sendRedirect("/user/login_failed.html");
                            return;
                        }
                        log.info("로그인 성공");
                        response.addHeader("Set-Cookie", "logined=true");
                        response.sendRedirect("/index.html");
                        break;
                }
            }
        } catch (IOException | RuntimeException e) {
            log.error(e.getMessage());
        }
    }
    private Boolean isLoggedIn(String Cookie) {
        return Boolean.parseBoolean(util.HttpRequestUtils.parseCookies(Cookie).get("logined"));
    }
}
