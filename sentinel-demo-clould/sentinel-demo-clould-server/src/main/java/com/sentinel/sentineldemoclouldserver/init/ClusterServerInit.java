package com.sentinel.sentineldemoclouldserver.init;

import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterParamFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager;
import com.alibaba.csp.sentinel.cluster.server.config.ServerTransportConfig;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.init.InitFunc;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.unicom.enums.ServerParamEnum;

import java.util.List;
import java.util.Set;

/**
 * @author wangxia
 * @date 2019/7/31 11:15
 * @Description:
 */
public class ClusterServerInit implements InitFunc {

    @Override
    public void init() throws Exception {
        //ClusterFlowRuleManager 针对集群限流规则，ClusterParamFlowRuleManager 针对集群热点规则，配置方式类似。
        // Register cluster flow rule property supplier which creates data source by namespace.
        //读取配置
        ClusterFlowRuleManager.setPropertySupplier(namespace -> {
            ReadableDataSource<String, List<FlowRule>> ds = new NacosDataSource<>(ServerParamEnum.remoteAddress.value(), ServerParamEnum.groupId.value(),
                    namespace + ServerParamEnum.FLOW_POSTFIX.value(),
                    source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {}));
            return ds.getProperty();
        });

        // Register cluster parameter flow rule property supplier.
        //读取参数配置
        ClusterParamFlowRuleManager.setPropertySupplier(namespace -> {
            ReadableDataSource<String, List<ParamFlowRule>> ds = new NacosDataSource<>(ServerParamEnum.remoteAddress.value(), ServerParamEnum.groupId.value(),
                    namespace + ServerParamEnum.PARAM_FLOW_POSTFIX.value(),
                    source -> JSON.parseObject(source, new TypeReference<List<ParamFlowRule>>() {}));
            return ds.getProperty();
        });

        // Server namespace set (scope) data source.
        //为 namespace 注册一个 SentinelProperty
        ReadableDataSource<String, Set<String>> namespaceDs = new NacosDataSource<>(ServerParamEnum.remoteAddress.value(), ServerParamEnum.groupId.value(),
                ServerParamEnum.namespaceSetDataId.value(), source -> JSON.parseObject(source, new TypeReference<Set<String>>() {}));
        ClusterServerConfigManager.registerNamespaceSetProperty(namespaceDs.getProperty());
        // Server transport configuration data source.
        //为 ServerTransportConfig 注册一个 SentinelProperty
        ReadableDataSource<String, ServerTransportConfig> transportConfigDs = new NacosDataSource<>(ServerParamEnum.remoteAddress.value(),
                ServerParamEnum.groupId.value(), ServerParamEnum.serverTransportDataId.value(),
                source -> JSON.parseObject(source, new TypeReference<ServerTransportConfig>() {}));
        //注册
        ClusterServerConfigManager.registerServerTransportProperty(transportConfigDs.getProperty());
    }
}
