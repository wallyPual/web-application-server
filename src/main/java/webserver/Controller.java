package webserver;

public interface Controller {
    void service(HttpRequest reqest, HttpResponse response);
}
