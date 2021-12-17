package webserver;

import org.junit.Test;

import java.io.*;

public class HttpResponseTest {
    private String testDirectroy = "./src/test/resources/";

    @Test
    public void responseForward() throws Exception {
        HttpResponse reponse = new HttpResponse(createOutPutStream("Http_Forward.txt"));
        reponse.forward("/index.html");
    }

    @Test
    public void reponseRedirect() throws Exception {
        HttpResponse response = new HttpResponse(createOutPutStream("Http_Redirect.txt"));
        response.sendRedirect("/index.html");
    }

    @Test
    public void responseCookies() throws Exception {
        HttpResponse response = new HttpResponse(createOutPutStream("Http_Cookie.txt"));
        response.addHeader("Set-Cookie", "logined=true");
        response.sendRedirect("/index.html");
    }

    private OutputStream createOutPutStream(String filename) throws FileNotFoundException {
        return new FileOutputStream(new File(testDirectroy + filename));
    }
}