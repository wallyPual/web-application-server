package webserver;

import util.HttpRequestUtils;
import util.IOUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HttpRequest {
    private String methods;
    private String path;
    private Map<String, String> parsedQuery;
    private Map<String, String> headers = new HashMap<>();

    HttpRequest(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));

        String line = br.readLine();
        if (line == null) return;
        setMethodAndPath(line);
        while ((line = br.readLine()) != null && !"".equals(line)) {
            String[] tokens = line.split(": ");
            if (tokens.length == 2) this.headers.put(tokens[0], tokens[1]);
        }
        setParameter(br);
    }

    private void setMethodAndPath(String line) {
        String[] methodLine = line.split(" ");
        this.methods = methodLine[0];
        this.path = methodLine[1];
    }

    private void setParameter(BufferedReader br) throws IOException {
        if ("GET".equals(this.methods) && this.path.contains("?")) {
            String[] pathToken = this.path.split("\\?");
            this.path = pathToken[0];
            this.parsedQuery = HttpRequestUtils.parseQueryString(pathToken[1]);
        } else if ("POST".equals(this.methods)) {
            this.parsedQuery = HttpRequestUtils.parseQueryString(IOUtils.readData(br, Integer.parseInt(this.headers.get("Content-Length"))));
        }
    }

    public String getMethods() {
        return this.methods;
    }

    public String getPath() {
        return this.path;
    }

    public Optional<String> getHeader(String key) {
        return Optional.ofNullable(headers.get(key));
    }

    public Optional<String> getParameter(String key) {
        return Optional.ofNullable(parsedQuery.get(key));
    }
}
