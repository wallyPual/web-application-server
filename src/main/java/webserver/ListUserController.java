package webserver;

import db.DataBase;
import model.User;

import java.util.Iterator;

public class ListUserController extends AbstractController {
    @Override
    public void doGet(HttpRequest request, HttpResponse response) {
        // 로그인 하지 않은 경우 로그인 페이지로 리다이렉트
        if (!isLoggedIn(request.getHeader("Cookie"))) response.sendRedirect("/user/login.html");

        Iterator users = DataBase.findAll().iterator();

        StringBuilder sb = new StringBuilder();

        for (Iterator it = users; it.hasNext(); ) {
            User user = (User) it.next();
            sb.append("<div>" + user.getUserId() + "</div>");
        }
        response.forwardBody(sb.toString());
    }

    private Boolean isLoggedIn(String Cookie) {
        return Boolean.parseBoolean(util.HttpRequestUtils.parseCookies(Cookie).get("logined"));
    }
}
