package citic.c.rocketmq.starter.message;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import lombok.Getter;

/**
 * 消费者接收消息的代理对象
 *
 * @param <T> 消息体解析后的类型
 */
@Getter
public class ResponseMessage<T> {

    //原始消息
    final private Message msg;

    //消息扩展信息,目前为null
    final private ConsumeContext context;

    //解析后的消息体内容
    final private T body;

    public ResponseMessage(Message msg, ConsumeContext context) {
        this.msg = msg;
        this.context = context;

        if (msg.getBody() != null) {
            this.body = JSONObject.parseObject(msg.getBody(), Object.class);
        } else {
            this.body = null;
        }
    }
}
