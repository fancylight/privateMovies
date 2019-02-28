package com.light.privateMovies.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class PicFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String url = ((HttpServletRequest) request).getRequestURL().toString();
        System.out.println(url);
        var out = ((HttpServletResponse) response).getOutputStream();
        //TODO:根据url处理获取正确的图片发送回去
        ((HttpServletResponse) response).setStatus(200);
    }

    @Override
    public void destroy() {

    }
}
