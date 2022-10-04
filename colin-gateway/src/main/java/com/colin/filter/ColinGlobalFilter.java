package com.colin.filter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author colin
 * @create 2022-10-02 17:44
 */
@Component
@RefreshScope
public class ColinGlobalFilter implements GlobalFilter, Ordered {

    @Value("${colin.gateway.grayscaleUserConfig}")
    private String[] grayscaleUserConfig;
    @Value("${colin.gateway.grayscale.version}")
    private String grayscaleVersion;
    @Value("${colin.gateway.formal.version}")
    private String formalVersion;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1.获取到 ServerHttpRequest
        ServerHttpRequest request = exchange.getRequest();
        /*ServerHttpResponse response = exchange.getResponse();*/
        // 2.判断是否是灰度用户 根据参数判断
        List<String> colinParameGrayscales = request.getHeaders().get("colinParameGrayscale");
        if (colinParameGrayscales != null && colinParameGrayscales.size() > 0) {
            // 3.如果是灰度用户
            grayscale(colinParameGrayscales);
        } else {
            // 设置当前环境为正式环境
            GrayscaleThreadLocalEnvironment.setCurrentEnvironment(formalVersion);
        }
        return chain.filter(exchange.mutate().request(request).build());
    }

    /**
     * 灰度流程
     */
    private void grayscale(List<String> colinParameGrayscales) {
        String colinParameGrayscale = colinParameGrayscales.get(0);
        if (StringUtils.isEmpty(colinParameGrayscale)) {
            return;
        }
        for (String userConfig : grayscaleUserConfig) {
            if (userConfig.equals(colinParameGrayscale)) {
                // 设置当前用户灰度的环境
                GrayscaleThreadLocalEnvironment.setCurrentEnvironment(grayscaleVersion);
                return;
            }
        }
        // 设置当前环境为正式环境
        GrayscaleThreadLocalEnvironment.setCurrentEnvironment(formalVersion);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
