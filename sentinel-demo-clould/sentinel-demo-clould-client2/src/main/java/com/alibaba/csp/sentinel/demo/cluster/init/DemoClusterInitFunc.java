package com.alibaba.csp.sentinel.demo.cluster.init;

import com.alibaba.csp.sentinel.cluster.ClusterStateManager;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientAssignConfig;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfig;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfigManager;
import com.alibaba.csp.sentinel.cluster.server.config.ServerTransportConfig;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.demo.cluster.entity.ClusterGroupEntity;
import com.alibaba.csp.sentinel.init.InitFunc;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import com.alibaba.csp.sentinel.util.HostNameUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.unicom.enums.ServerParamEnum;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author wangxia
 * @date 2019/8/1 9:14
 * @Description:
 */
public class DemoClusterInitFunc implements InitFunc {

    private static final String APP_NAME = "appA";
    private static final  Integer CLUSTER_SERVER_PORT=11111;
    private static final Integer REQUEST_TIME_OUT=200;
    private final String flowDataId = APP_NAME + ServerParamEnum.FLOW_POSTFIX.value();
    private final String paramDataId = APP_NAME + ServerParamEnum.PARAM_FLOW_POSTFIX.value();
    private final String clusterMapDataId = APP_NAME + ServerParamEnum.CLUSTER_MAP_POSTFIX.value();

    @Override
    public void init() throws Exception {
        // Register client dynamic rule data source.
        initDynamicRuleProperty();

        // Register token client related data source.  注册令牌客户端相关数据源
        // Token client common config:
        initClientConfigProperty();

        initStateProperty();
    }

    /**
     * 通过配置文件初始化限流规则
     */
    private void initDynamicRuleProperty() {
        //从nacos读取限流规则
        ReadableDataSource<String, List<FlowRule>> ruleSource = new NacosDataSource<>(ServerParamEnum.remoteAddress.value(), ServerParamEnum.groupId.value(),
                flowDataId, source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {}));
        //注册限流规则
        FlowRuleManager.register2Property(ruleSource.getProperty());
        //从nacos读取参数限制规则
        ReadableDataSource<String, List<ParamFlowRule>> paramRuleSource = new NacosDataSource<>(ServerParamEnum.remoteAddress.value(), ServerParamEnum.groupId.value(),
                paramDataId, source -> JSON.parseObject(source, new TypeReference<List<ParamFlowRule>>() {}));
        //注册参数限流规则
        ParamFlowRuleManager.register2Property(paramRuleSource.getProperty());
    }

    /**
     * 加载集群客户端配置
     * 主要是集群服务端的相关连接信息
     */
    private void initClientConfigProperty() {
        ClusterClientAssignConfig assignConfig = new ClusterClientAssignConfig();
        assignConfig.setServerHost(ServerParamEnum.remoteAddress.value());
        assignConfig.setServerPort(CLUSTER_SERVER_PORT);
        ClusterClientConfigManager.applyNewAssignConfig(assignConfig);

        ClusterClientConfig clientConfig = new ClusterClientConfig();
        clientConfig.setRequestTimeout(REQUEST_TIME_OUT);
        ClusterClientConfigManager.applyNewConfig(clientConfig);
    }


    /**
     * 动态加载服务端配置信息
     */
    private void initClientAssignProperty() {
        String clientConfigDataId = "cluster-client-config";
        // 初始化一个配置ClusterClientConfig的 Nacos 数据源
        ReadableDataSource<String, ClusterClientConfig> clientConfigDS = new NacosDataSource<>(ServerParamEnum.remoteAddress.value(), ServerParamEnum.groupId.value(), clientConfigDataId,
                source -> JSON.parseObject(source, new TypeReference<ClusterClientConfig>() {}));
        ClusterClientConfigManager.registerClientConfigProperty(clientConfigDS.getProperty());

        String clientAssignConfigDataId = "cluster-client-assign-config";
        // 初始化一个配置ClusterClientAssignConfig的 Nacos 数据源
        ReadableDataSource<String, ClusterClientAssignConfig> clientAssignConfigDS = new NacosDataSource<>(ServerParamEnum.remoteAddress.value(), ServerParamEnum.groupId.value(), clientAssignConfigDataId,
                source -> JSON.parseObject(source, new TypeReference<ClusterClientAssignConfig>() {}));
        ClusterClientConfigManager.registerServerAssignProperty(clientAssignConfigDS.getProperty());

    }


    private void initStateProperty() {
        // Cluster map format:
        // [{"clientSet":["112.12.88.66@8729","112.12.88.67@8727"],"ip":"112.12.88.68","machineId":"112.12.88.68@8728","port":11111}]
        // machineId: <ip@commandPort>, commandPort for port exposed to Sentinel dashboard (transport module)
        ReadableDataSource<String, Integer> clusterModeDs = new NacosDataSource<>(ServerParamEnum.remoteAddress.value(), ServerParamEnum.groupId.value(),
                clusterMapDataId, source -> {
            List<ClusterGroupEntity> groupList = JSON.parseObject(source, new TypeReference<List<ClusterGroupEntity>>() {});
            return Optional.ofNullable(groupList)
                    .map(this::extractMode)
                    .orElse(ClusterStateManager.CLUSTER_NOT_STARTED);
        });
        ClusterStateManager.registerProperty(clusterModeDs.getProperty());
    }

    private int extractMode(List<ClusterGroupEntity> groupList) {
        // If any server group machineId matches current, then it's token server.
        if (groupList.stream().anyMatch(this::machineEqual)) {
            return ClusterStateManager.CLUSTER_SERVER;
        }
        // If current machine belongs to any of the token server group, then it's token client.
        // Otherwise it's unassigned, should be set to NOT_STARTED.
        boolean canBeClient = groupList.stream()
                .flatMap(e -> e.getClientSet().stream())
                .filter(Objects::nonNull)
                .anyMatch(e -> e.equals(getCurrentMachineId()));
        return canBeClient ? ClusterStateManager.CLUSTER_CLIENT : ClusterStateManager.CLUSTER_NOT_STARTED;
    }

    private Optional<ServerTransportConfig> extractServerTransportConfig(List<ClusterGroupEntity> groupList) {
        return groupList.stream()
                .filter(this::machineEqual)
                .findAny()
                .map(e -> new ServerTransportConfig().setPort(e.getPort()).setIdleSeconds(600));
    }

    private Optional<ClusterClientAssignConfig> extractClientAssignment(List<ClusterGroupEntity> groupList) {
        if (groupList.stream().anyMatch(this::machineEqual)) {
            return Optional.empty();
        }
        // Build client assign config from the client set of target server group.
        for (ClusterGroupEntity group : groupList) {
            if (group.getClientSet().contains(getCurrentMachineId())) {
                String ip = group.getIp();
                Integer port = group.getPort();
                return Optional.of(new ClusterClientAssignConfig(ip, port));
            }
        }
        return Optional.empty();
    }

    private boolean machineEqual(/*@Valid*/ ClusterGroupEntity group) {
        return getCurrentMachineId().equals(group.getMachineId());
    }

    private String getCurrentMachineId() {
        // Note: this may not work well for container-based env.
        return HostNameUtil.getIp() + SEPARATOR + TransportConfig.getRuntimePort();
    }

    private static final String SEPARATOR = "@";
}
