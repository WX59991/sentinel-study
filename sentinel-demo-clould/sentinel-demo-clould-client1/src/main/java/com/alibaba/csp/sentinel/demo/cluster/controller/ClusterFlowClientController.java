package com.alibaba.csp.sentinel.demo.cluster.controller;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wangxia
 * @date 2019/8/1 10:23
 * @Description:
 */
@RestController
public class ClusterFlowClientController {

    private static String RESOURCE_NAME = "cluster-resource";

    /**
     * 模拟流量请求该方法
     */
    @GetMapping("/clusterFlow")
    public String clusterFlow() {
        Entry entry = null;
        String retVal;
        try{
            entry = SphU.entry(RESOURCE_NAME, EntryType.IN,1);
            retVal = "passed";
        }catch(BlockException e){
            retVal = "blocked";
        }finally {
            if(entry!=null){
                entry.exit();
            }
        }
        return retVal;
    }

}
