[
    {
        "resource" : "cluster-resource",     // 限流的资源名称
        "grade" : 1,                         // 限流模式为：qps
        "count" : 10,                        // 阈值为：10
        "clusterMode" :  true,               // 集群模式为：true
        "clusterConfig" : {
            "flowId" : 111,                  // 全局唯一id
            "thresholdType" : 1,             // 阈值模式伪：全局阈值
            "fallbackToLocalWhenFail" : true // 在 client 连接失败或通信失败时，是否退化到本地的限流模式
        }
    }
]