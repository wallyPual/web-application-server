package webserver;

abstract class AbstractController implements Controller {
    @Override
    public void service(HttpRequest request, HttpResponse response) {
        String methods = request.getMethods();

        if ("POST".equals(methods)) {
            doPost(request, response);
            return;
        }
        doGet(request, response);
    }

    void doPost(HttpRequest request, HttpResponse response) {
    }

    void doGet(HttpRequest request, HttpResponse response) {
    }
}
