package no.cantara.stingray.httpclient.apache;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import java.io.IOException;
import java.util.Enumeration;

class EchoHandler extends AbstractHandler {

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Enumeration<String> headerNames = request.getHeaderNames();
        System.out.printf("REQUEST URL: %s%n", request.getRequestURL());
        if (!"/echo".equals(request.getServletPath() + request.getPathInfo())) {
            response.setStatus(400);
            response.getOutputStream()
                    .close();
            return;
        }
        System.out.printf("%s %s%s%n", request.getMethod(), request.getServletPath(), request.getPathInfo());
        System.out.printf("REQUEST HEADERS:%n");
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            System.out.printf("%s: %s%n", headerName, headerValue);
        }
        System.out.println();
        response.setStatus(200);
        response.setHeader("Content-Length", request.getHeader("Content-Length"));
        response.setHeader("Content-Type", request.getHeader("Content-Type"));
        try (ServletInputStream in = request.getInputStream()) {
            try (ServletOutputStream out = response.getOutputStream()) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) != -1) {
                    out.write(buf, 0, n);
                }
            }
        }
    }
}
