package com.alibaba.csp.sentinel.demo.cluster;

import com.alibaba.csp.sentinel.demo.cluster.init.DemoClusterInitFunc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 必须添加vm参数:-Dproject.name=appA -Dcsp.sentinel.dashboard.server=127.0.0.1:8080 -Dcsp.sentinel.log.use.pid=true
 * -Dcsp.sentinel.log.use.pid=true 如果是一台机器上必须添加这个，否则会出问题
 * -Dproject.name=appA  必须与token令牌发放者保持一致
 * @author wangxia
 * @date 2019/8/1 9:56
 * @Description:
 */
@Slf4j
@SpringBootApplication
public class SentinelDemoClouldClient2Application {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(SentinelDemoClouldClient2Application.class,args);
        new DemoClusterInitFunc().init();
        log.info("启动成功");
    }

}
