package com.colin.rule;

import com.colin.filter.GrayscaleThreadLocalEnvironment;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.RoundRobinRule;
import com.netflix.loadbalancer.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.alibaba.nacos.ribbon.NacosServer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author colin
 * @create 2022-10-02 17:49
 *
 * (重写)负载均衡算法
 */
@Component
@Slf4j
public class ColinRule extends RoundRobinRule {

    private AtomicInteger nextServerCyclicCounter;

    public ColinRule() {
        nextServerCyclicCounter = new AtomicInteger(0);
    }

    public Server choose(ILoadBalancer lb, Object key) {
        if (lb == null) {
            log.warn("no load balancer");
            return null;
        }
        Server server = null;
        int count = 0;
        while (server == null && count++ < 10) {
            List<Server> reachableServers = lb.getReachableServers();
            List<Server> allServers = lb.getAllServers();
            int upCount = reachableServers.size();
            int serverCount = allServers.size();

            if ((upCount == 0) || (serverCount == 0)) {
                log.warn("No up servers available from load balancer: " + lb);
                return null;
            }
            List<NacosServer> filterServers = new ArrayList<>();
            String currentEnvironmentVersion = GrayscaleThreadLocalEnvironment.getCurrentEnvironment();

            /**
             * 遍历服务的所有实例数据，根据规则进行筛选
             * 根据 nacos 元数据中版本标识进行筛选
             */
            for (Server serverInfo : allServers) {
                NacosServer nacosServer = (NacosServer) serverInfo;

                // 获取 nacos 元数据中的版本标识
                String version = nacosServer.getMetadata().get("version");
                if (version.equals(currentEnvironmentVersion)) {
                    /**
                     * 根据 nacos 元数据中版本标识进行筛选
                     */
                    filterServers.add(nacosServer);
                }
            }
            int filterServerCount = filterServers.size();
            // 使用负载均衡算法选出实例所在的下标
            int nextServerIndex = incrementAndGetModulo(filterServerCount);
            // 根据下标选出服务实例
            server = filterServers.get(nextServerIndex);

            if (server == null) {
                /* Transient. */
                Thread.yield();
                continue;
            }

            if (server.isAlive() && (server.isReadyToServe())) {
                return (server);
            }

            // Next.
            server = null;
        }

        if (count >= 10) {
            log.warn("No available alive servers after 10 tries from load balancer: "
                    + lb);
        }

        // 返回具体的服务实例
        return server;
    }

    private int incrementAndGetModulo(int modulo) {
        for (; ; ) {
            int current = nextServerCyclicCounter.get();
            int next = (current + 1) % modulo;
            if (nextServerCyclicCounter.compareAndSet(current, next))
                return next;
        }
    }
}
