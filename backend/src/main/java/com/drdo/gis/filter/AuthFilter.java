package com.drdo.gis.filter;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(urlPatterns = "/api/*")
public class AuthFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(AuthFilter.class);

    @Override
    public void init(FilterConfig config) {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String path = req.getRequestURI();

        // Allow login endpoint without authentication
        if (path.endsWith("/api/auth/login") || path.endsWith("/api/auth/session")) {
            chain.doFilter(request, response);
            return;
        }

        // Allow CORS preflight
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            chain.doFilter(request, response);
        } else {
            resp.setStatus(401);
            resp.setContentType("application/json;charset=UTF-8");
            JsonObject err = new JsonObject();
            err.addProperty("status", "error");
            err.addProperty("code", 401);
            err.addProperty("message", "Unauthorized — please log in");
            resp.getWriter().write(err.toString());
            log.warn("Unauthorized access attempt: {}", path);
        }
    }

    @Override
    public void destroy() {}
}
