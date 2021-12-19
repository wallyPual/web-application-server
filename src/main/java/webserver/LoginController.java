package webserver;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginController extends AbstractController{
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    @Override
    public void doPost(HttpRequest request, HttpResponse response) {
        User findDBUser = DataBase.findUserById(request.getParameter("userId"));
        if (findDBUser == null || !findDBUser.getPassword().equals(request.getParameter("password"))) {
            response.addHeader("Set-Cookie", "logined=false");
            response.sendRedirect("/user/login_failed.html");
            return;
        }
        log.info("로그인 성공");
        response.addHeader("Set-Cookie", "loginedlogined=true");
        response.sendRedirect("/index.html");
    }
}
