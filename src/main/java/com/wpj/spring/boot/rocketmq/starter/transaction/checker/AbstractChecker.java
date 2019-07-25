package com.wpj.spring.boot.rocketmq.starter.transaction.checker;

import com.aliyun.openservices.ons.api.transaction.TransactionStatus;
import com.wpj.spring.boot.rocketmq.starter.message.ResponseMessage;

/**
 * 事务消息checker抽象类
 *
 * @author wangpejian
 * @date 19-2-14 上午10:41
 */
public abstract class AbstractChecker {

    public abstract String getChannelName();

    public abstract TransactionStatus check(ResponseMessage msg);

}
