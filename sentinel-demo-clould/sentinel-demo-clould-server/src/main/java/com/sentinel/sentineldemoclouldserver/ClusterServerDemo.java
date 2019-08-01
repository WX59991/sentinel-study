/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sentinel.sentineldemoclouldserver;

import com.alibaba.csp.sentinel.cluster.server.ClusterTokenServer;
import com.alibaba.csp.sentinel.cluster.server.SentinelDefaultTokenServer;
import com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager;
import com.alibaba.csp.sentinel.cluster.server.config.ServerTransportConfig;
import com.unicom.enums.ServerParamEnum;

import java.util.Collections;

/**
 * <p>Cluster server demo (alone mode).</p>
 * <p>Here we init the cluster server dynamic data sources in
 * {@link com.sentinel.sentineldemoclouldserver.init.ClusterServerInit}.</p>
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
//运行时加上参数-Dproject.name=appA -Dcsp.sentinel.dashboard.server=127.0.0.1:8080
public class ClusterServerDemo {

    public static void start(String[] args) throws Exception {
        // 创建token服务
        ClusterTokenServer tokenServer = new SentinelDefaultTokenServer();

        // A sample for manually load config for cluster server.
        // It's recommended to use dynamic data source to cluster manage config and rules.
        // See the sample in DemoClusterServerInitFunc for detail.
        ClusterServerConfigManager.loadGlobalTransportConfig(new ServerTransportConfig()
            .setIdleSeconds(600)
            .setPort(11111));
        ClusterServerConfigManager.loadServerNamespaceSet(Collections.singleton(ServerParamEnum.APP_NAME.value()));

        // Start the server.
        tokenServer.start();
    }
}