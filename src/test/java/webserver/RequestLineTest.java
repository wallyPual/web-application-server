package webserver;

import org.junit.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class RequestLineTest {
    @Test
    public void 메소드_생성() {
        RequestLine line = new RequestLine("GET /index.html HTTP/1.1");

        assertEquals("GET", line.getMethods());
        assertEquals("/index.html", line.getPath());

        line = new RequestLine("POST /index.html HTTP/1.1");

        assertEquals("POST", line.getMethods());
        assertEquals("/index.html", line.getPath());
    }

    @Test
    public void 파라미터_생성() {
        RequestLine line = new RequestLine("GET /index.html?userId=paul&password=password&email=yjk@marpple.com");

        assertEquals("GET", line.getMethods());
        assertEquals("/index.html", line.getPath());
        Map<String, String> params = line.getParameter();
        assertEquals(3, params.size());
    }
}