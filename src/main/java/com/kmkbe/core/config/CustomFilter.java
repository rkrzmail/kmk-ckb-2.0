package com.kmkbe.core.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Inisialisasi jika diperlukan
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        /*
        // Contoh validasi token atau header
        String authToken = httpRequest.getHeader("Authorization");
        if (authToken == null || !authToken.equals("valid-token")) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
            httpResponse.getWriter().write("Unauthorized: Invalid Token");
            return; // Stop processing filter chain
        }
        */


        // Jika valid, lanjutkan ke filter berikutnya atau handler
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Cleanup jika diperlukan
    }
}