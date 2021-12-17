package webserver;

import util.HttpRequestUtils;
import java.util.Map;

public class RequestLine {
    private String methods;
    private String path;
    private Map<String, String> params;

    public RequestLine(String line) {
        String[] methodLine = line.split(" ");
        methods = methodLine[0];
        path = methodLine[1];

        if ("GET".equals(methods) && path.contains("?")) {
            String[] pathToken = path.split("\\?");
            path = pathToken[0];
            params = HttpRequestUtils.parseQueryString(pathToken[1]);
        }
    }
    public String getMethods() {
        return methods;
    }
    public String getPath() {
        return path;
    }
    public Map<String, String> getParameter() {
        return params;
    }
}
