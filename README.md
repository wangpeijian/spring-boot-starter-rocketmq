# spring-rocketmq-starter

spring-boot 使用注入方式调用阿里云RocketMQ SDK。
<br> 
email: 519955464@qq.com

---

### 配置示例

#### 1. yml配置文件
```
rocketMq:
    ONS-address: 
    access-key: 
    secret-key: 

    bindings:
        #生产者配置
        producers:
            #绑定的生产者别名
            p0: 
                #阿里topic
                topic: 
                tag: 'Tag0'
                #阿里生产者id
                producer: 
                #生产者类型-普通消息
                type: normal 

            p1:
                topic: 
                tag: 'TagA'
                producer: 
                type: normal

            p2:
                topic: 
                tag: 'TagB'
                producer: 
                #顺序消息
                type: order 

            p3:
                topic: 
                tag: 'TagC'
                producer: 
                #事务消息
                type: transaction 

        #消费者配置
        consumers:
            #绑定的消费者别名
            c1:
                #阿里topic
                topic: 
                #消费的tag, 可以消费多个tag
                subExpression: 'Tag0||TagA' 
                #阿里消费者id
                consumer: 
                #普通消息
                type: normal
                #是否使用广播方式
                useBroadcast: false 
                #顺序消息消费失败进行重试前的等待时间，单位(毫秒)
                suspendTimeMillis: 10000 
                #消息消费失败时的最大重试次数
                MaxReconsumeTimes: 2 

            c2:
                topic: 
                subExpression: 'TagB'
                consumer: 
                #顺序消息
                type: order

            c3:
                topic: 
                subExpression: 'TagC'
                consumer: 
                #事务消息
                type: transaction

```

#### 2. 使用发送者
##### 2.1 注入生产者
```
    @Autowired
    @Qualifier("p0")
    private ChannelNormal p0;
    
    @Autowired
    @Qualifier("p1")
    private ChannelNormal p1;
    
    @Autowired
    @Qualifier("p2")
    private ChannelOrder p2;
    
    @Autowired
    @Qualifier("p3")
    private ChannelTransaction p3;
    
```

##### 2.2 使用注入的对象可以直接发送消息
发送者对象是 RocketMQ 中三个发送者的代理对象，分别代理了三个发送者的所有对象，发送的消息体自动使用json序列化发送。五种类型（普通、定时、延时、顺序、事务）的消息都可以发送。

```
    SendResult sendResult0 = p0.send("发送了一条普通消息");
    //发送延迟消息、定时消息，传入一个时间戳值
    SendResult sendResult0 = p0.send("发送了一条普通消息", 1551836991150L);
    
    SendResult sendResult2 = p2.send("发送了一条顺序消息");
    
    SendResult sendResult2 = p3.send("发送了一条事务消息", (msg, arg) -> TransactionStatus.CommitTransaction, null);

```

##### 2.3 注入生产者仓库
```
    @Autowired
    private ChannelRepertory channelRepertory;
    
    //获取普通生产者
    ChannelNormal p0 = channelRepertory.getChannel("p0");
    
    //获取顺序消息生产者
    ChannelOrder p2 = channelRepertory.getChannelOrder("p2");
    
    //获取事务消息生产者
    ChannelTransaction p3 = channelRepertory.getChannelTransaction("p3");
    
```

#### 3. 事务消息注册checker
事务消息的发送者，依赖一个checker，checker类需要实现 AbstractChecker 抽象类
```
    @Component
    public class TestChecker extends AbstractChecker {
    
        //Checker 类中不能直接注入生产者对象，需要从生产者仓库中获取
        @Autowired
        private ChannelRepertory channelRepertory;
    
        //此方法返回，绑定了事务消息的生产者别名
        public String getChannelName() {
            return "p3";
        }
    
        //事务消息状态为 TransactionStatus.Unknow 时此处会接受到检测消息，需要返回一个事务状态
        public TransactionStatus check(final ResponseMessage msg) {
            logger.info("消息内容：", msg.getBody());
    
            //Checker 类中如果需要使用生产者，需要从生产者仓库中获取一个生产者
            ChannelNormal p1 = channelRepertory.getChannel("p1");
    
            return TransactionStatus.CommitTransaction;
        }
    }
    
```

#### 4. 注册消费者回调
消费者回调注册，类使用添加注解 `@MonitorEnable` 回调方法添加 `@MonitorRocketMsg` 注解，参数为消费者别名。
方法回调参数为阿里云SDK中 `Message` 对象的代理对象 `ResponseMessage<T>` 根据泛型自动将 `body` 转为对应对象。
方法中不抛出异常,表示正常消费成功，抛出异常则表示消费失败。
消费者回调注册类中可以注入其他消费者对象。

```
    @Slf4j
    @MonitorEnable
    public class MqConsumer {
    
        @Autowired
        @Qualifier("p1")
        private ChannelNormal channelNormal;
    
        @MonitorRocketMsg("c1")
        public void ConsumerTest(ResponseMessage<InstanceServiceReq> message) {
            logger.info("========普通消息接受: {}", message.getBody());
        }
    
        @MonitorRocketMsg("c2")
        public void ConsumerTest2(ResponseMessage<InstanceServiceReq> message) {
            logger.info("========顺序消息接受: {}", message.getBody());
    
            channelNormal.send("c2消费者发送消息了");
        }
    
        @MonitorRocketMsg("c3")
        public void ConsumerTest3(ResponseMessage<InstanceServiceReq> message) {
            logger.info("========事务消息接受: {}", message.getBody());
        }
    }
    
```
