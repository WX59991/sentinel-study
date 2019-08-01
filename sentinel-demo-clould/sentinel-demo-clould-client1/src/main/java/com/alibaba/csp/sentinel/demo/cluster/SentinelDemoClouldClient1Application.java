package com.alibaba.csp.sentinel.demo.cluster;

import com.alibaba.csp.sentinel.demo.cluster.init.DemoClusterInitFunc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author wangxia
 * @date 2019/8/1 9:56
 * @Description:
 */
@Slf4j
@SpringBootApplication
public class SentinelDemoClouldClient1Application {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(SentinelDemoClouldClient1Application.class,args);
        new DemoClusterInitFunc().init();
        log.info("启动成功");
    }

}
