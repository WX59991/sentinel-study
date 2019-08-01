package com.alibaba.csp.sentinel.demo.cluster.controller;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wangxia
 * @date 2019/8/1 9:16
 * @Description:
 */
@RestController
public class HelloController {
    private static String RESOURCE_NAME = "sayHelloBlockHandler";
    @GetMapping("/hello")
    public String sayHello(String name){
        Entry entry = null;
        String retVal;
        try{
            entry = SphU.entry(RESOURCE_NAME, EntryType.IN,1);
            retVal =name+"你好，欢迎登录";
        }catch(BlockException e){
            retVal = "超过流量限制了";
        }finally {
            if(entry!=null){
                entry.exit();
            }
        }
        return retVal;
    }

}
