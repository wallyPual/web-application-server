package util;

import org.junit.Test;
import static org.junit.Assert.*;

public class RequestUtilsTest {
    @Test
    public void HTTP_요청_정보의_첫번쨰_라인에서_요청_URL을_추출한다() {
        assertEquals(RequestUtils.getReqURL("GET /index.html HTTP/1.1"), "/index.html");
    }
}