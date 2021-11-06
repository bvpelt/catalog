package com.bsoft.catalogus.util;

import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

//@Slf4j
@Component
@WebFilter("/*")
public class RequestTimeFilter implements Filter {
    /*
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // empty
    }

     */

    private ServletContext context;

    public void init(FilterConfig filterConfig) throws ServletException {
        this.context = filterConfig.getServletContext();
        this.context.log("RequestLoggingFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        Instant start = Instant.now();
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            Instant finish = Instant.now();
            long time = Duration.between(start, finish).toMillis();
            this.context.log("RequestTimeFilter Timing data " + ((HttpServletRequest) servletRequest).getRequestURI() + ": " + time + " ms ");
        }
    }

    @Override
    public void destroy() {
        // empty
    }
}
