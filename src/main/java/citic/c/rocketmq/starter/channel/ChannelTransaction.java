package citic.c.rocketmq.starter.channel;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.SendResult;
import com.aliyun.openservices.ons.api.transaction.LocalTransactionExecuter;
import com.aliyun.openservices.ons.api.transaction.TransactionProducer;

/**
 * 事务消息代理类
 */
public class ChannelTransaction extends ChannelBase {

    private TransactionProducer transactionProducer;

    public ChannelTransaction(TransactionProducer transactionProducer, String topic, String tag) {
        super(topic, tag);
        this.transactionProducer = transactionProducer;
    }

    public SendResult send(final Object message, final LocalTransactionExecuter executer, final Object arg) {
        Message msg = this.getMessage(message);
        return transactionProducer.send(msg, executer, arg);
    }
}
