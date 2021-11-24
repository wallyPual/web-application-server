package util;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class RequestUtilsTest {
    @Test
    public void HTTP_요청_정보의_첫번쨰_라인에서_요청_URL을_추출한다() {
        assertEquals(RequestUtils.getReqURL("GET /index.html HTTP/1.1"), "/index.html");
    }

    @Test
    public void HTTP_요청_URL에서_쿼리스트링_파싱하기() {
        HashMap hm = new HashMap();
        hm.put("userId", "paul");
        hm.put("password", "luap");
        hm.put("name", "김영주");
        hm.put("email", "yjk@marpple.com");
        assertEquals(RequestUtils.queryToMap("POST /user/create?userId=paul&password=luap&name=김영주&email=yjk@marpple.com"), hm);
    }
}