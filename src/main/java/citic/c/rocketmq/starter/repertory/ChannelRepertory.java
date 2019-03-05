package citic.c.rocketmq.starter.repertory;

import citic.c.rocketmq.starter.channel.ChannelNormal;
import citic.c.rocketmq.starter.channel.ChannelOrder;
import citic.c.rocketmq.starter.channel.ChannelTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Slf4j
@Component
public class ChannelRepertory {

    //普通消息
    private final HashMap<String, ChannelNormal> channelNormal = new HashMap<>();

    //顺序消息
    private final HashMap<String, ChannelOrder> channelOrder = new HashMap<>();

    //事务消息
    private final HashMap<String, ChannelTransaction> channelTransaction = new HashMap<>();

    public ChannelNormal addChannelNormal(String name, ChannelNormal channelNormal) {
        this.channelNormal.put(name, channelNormal);
        return channelNormal;
    }

    public ChannelOrder addChannelOrder(String name, ChannelOrder channelOrder) {
        this.channelOrder.put(name, channelOrder);
        return channelOrder;
    }

    public ChannelTransaction addChannelTransaction(String name, ChannelTransaction channelTransaction) {
        this.channelTransaction.put(name, channelTransaction);
        return channelTransaction;
    }

    public ChannelNormal getChannel(String name) {
        return channelNormal.get(name);
    }

    public ChannelOrder getChannelOrder(String name) {
        return channelOrder.get(name);
    }

    public ChannelTransaction getChannelTransaction(String name) {
        return channelTransaction.get(name);
    }
}
