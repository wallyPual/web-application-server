package util;

public class RequestUtils {
    public static String getReqURL(String req) {
        return req.split(" ")[1];
    }
}
