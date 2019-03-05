package citic.c.rocketmq.starter.transaction.checker;

import citic.c.rocketmq.starter.message.ResponseMessage;
import com.aliyun.openservices.ons.api.transaction.TransactionStatus;

/**
 * 事务消息checker抽象类
 *
 * @auther wangpejian
 * @date 19-2-14 上午10:41
 */
public abstract class AbstractChecker {

    public abstract String getChannelName();

    public abstract TransactionStatus check(ResponseMessage msg);

}
