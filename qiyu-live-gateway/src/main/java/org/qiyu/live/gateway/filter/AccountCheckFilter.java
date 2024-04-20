package org.qiyu.live.gateway.filter;

import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.qiyu.live.account.interfaces.rpc.IAccountTokenRPC;
import org.qiyu.live.common.interfaces.enums.GatewayHeaderEnum;
import org.qiyu.live.gateway.properties.GatewayApplicationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import static io.netty.handler.codec.http.cookie.CookieHeaderNames.MAX_AGE;
import static org.springframework.web.cors.CorsConfiguration.ALL;


@Component
public class AccountCheckFilter implements GlobalFilter, Ordered {

    @DubboReference
    private IAccountTokenRPC accountTokenRPC;
    @Resource
    private GatewayApplicationProperties gatewayApplicationProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        //获取请求地址
        URI uri = request.getURI();
        String urlPath = uri.getPath();
        ServerHttpResponse response = exchange.getResponse();
        HttpHeaders headers = response.getHeaders();
        headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://127.0.0.1:5500");
        headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "POST, GET");
        headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*");
        headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, ALL);
        headers.add(HttpHeaders.ACCESS_CONTROL_MAX_AGE, MAX_AGE);
        //为空判断
        if (StringUtils.isEmpty(urlPath)) {
            return Mono.empty();
        }
        List<String> filterUrlList = gatewayApplicationProperties.getNotCheckUrlList();
        //逐个遍历看是否在白名单内
        for (String urlItem : filterUrlList) {
            if (urlPath.startsWith(urlItem)) {
                System.out.println("满足匹配规则");
                return chain.filter(exchange);
            }
        }
        //不在白名单内
        if (!CollectionUtils.isEmpty(filterUrlList) && filterUrlList.contains(urlPath)) {
            System.out.println("可以不用进行token校验");
            return chain.filter(exchange);
        }
        System.out.println("网关请求处理");
        //取出token
        List<HttpCookie> httpCookieList = request.getCookies().get("qytk");
        if (CollectionUtils.isEmpty(httpCookieList)) {
            //禁止访问
            System.err.println("httpCookieList is null");
            return Mono.empty();
        }
        HttpCookie httpCookie = httpCookieList.get(0);
        if (httpCookie == null) {
            System.err.println("error visit");
            return Mono.empty();
        }
        Long userId = accountTokenRPC.getUserIdByToken(httpCookie.getValue());
        if (userId == null) {
            System.out.println("执行不通过，无法请求");
            return Mono.empty();
        }
        ServerHttpRequest.Builder builder = request.mutate();
        //把userId传递给到下游去
        builder.header(GatewayHeaderEnum.USER_LOGIN_ID.getName(), String.valueOf(userId));
        return chain.filter(exchange.mutate().request(builder.build()).build());
    }

    @Override
    public int getOrder() {
        return 0;
    }
}