package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.RequestUtils;

public class RequestHandler extends Thread {
    String absolutePath = new File("").getAbsolutePath();
    ArrayList<String> reqURL = new ArrayList<>();

    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            DataOutputStream dos = new DataOutputStream(out);
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));

            String line = br.readLine();

            if (line == null) return;

            while (!line.equals("")) {
                if (line.indexOf("index.html") != -1) {
                    reqURL.add(RequestUtils.getReqURL(line));
                }
                line = br.readLine();
            }

            Iterator<String> reqIter = reqURL.iterator();

            while(reqIter.hasNext()) {
                if (reqIter.next().equals("/index.html")) {
                    byte[] body = Files.readAllBytes(new File(absolutePath + "/webapp/index.html").toPath());
                    response200Header(dos, body.length);
                    responseBody(dos, body);
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
