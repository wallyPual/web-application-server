package webserver;

import util.HttpRequestUtils;
import util.IOUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HttpRequest {
    private Map<String, String> params;
    private Map<String, String> headers = new HashMap<>();
    private RequestLine requestLine;

    HttpRequest(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        String line = br.readLine();
        if (line == null) return;

        requestLine = new RequestLine(line);

        while ((line = br.readLine()) != null && !"".equals(line)) {
            String[] tokens = line.split(": ");
            if (tokens.length == 2) this.headers.put(tokens[0], tokens[1]);
        }

        if ("POST".equals(getMethods())) {
            params = HttpRequestUtils.parseQueryString(IOUtils.readData(br, Integer.parseInt(headers.get("Content-Length"))));
        } else {
            params = requestLine.getParameter();
        }
    }

    public String getMethods() {
        return requestLine.getMethods();
    }

    public String getPath() {
        return requestLine.getPath();
    }

    public String getHeader(String key) {return headers.get(key);}

    public String getParameter(String key) {
        return params.get(key);
    }
}
