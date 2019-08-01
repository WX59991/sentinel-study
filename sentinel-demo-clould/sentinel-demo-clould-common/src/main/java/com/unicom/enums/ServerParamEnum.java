package com.unicom.enums;

/**
 * @author wangxia
 * @date 2019/7/31 11:33
 * @Description: sentinel所需配置参数
 */
public enum ServerParamEnum {

    APP_NAME("appA"),
    FLOW_POSTFIX("-flow-rules"),   //流量控制文件后缀
    PARAM_FLOW_POSTFIX("-param-rules"),  //参数控制文件后缀
    remoteAddress("localhost"),
    groupId("DEFAULT_GROUP"),
    namespaceSetDataId("cluster-server-namespace-set"),
    serverTransportDataId("cluster-server-transport-config"),
    CLUSTER_MAP_POSTFIX("-cluster-map");   //集群后缀

    private String value;

    ServerParamEnum(String value){
        this.value=value;
    }

    public String value(){
        return value;
    }
}
