package com.wpj.spring.boot.rocketmq.starter.annotation.enums;

/**
 * 消息的发送方式
 *
 * @auther wangpejian
 * @date 19-3-29 上午10:26
 */
public enum MessageType {


    oneway("sendOneway"),

    async("sendAsync"),

    defaultType("send");

    private String methodName;

    MessageType(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }
}


