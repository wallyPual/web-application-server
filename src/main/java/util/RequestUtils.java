package util;

import java.util.Map;

public class RequestUtils {
    public static String getReqURL(String req) {
        return req.split(" ")[1];
    }

    public static Map<String, String> queryToMap(String url) {
        String[] splitedURL = url.split("\\?");

        return HttpRequestUtils.parseQueryString(splitedURL[1]);
    }
}