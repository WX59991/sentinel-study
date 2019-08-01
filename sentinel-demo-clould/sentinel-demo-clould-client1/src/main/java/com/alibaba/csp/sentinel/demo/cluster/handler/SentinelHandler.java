package com.alibaba.csp.sentinel.demo.cluster.handler;

/**
 * @author wangxia
 * @date 2019/8/1 9:21
 * @Description:
 */
public class SentinelHandler {

    public String handleException(){
        return "超过流量限制了";
    }

}
