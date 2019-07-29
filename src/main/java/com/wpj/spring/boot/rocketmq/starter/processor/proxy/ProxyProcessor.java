package com.wpj.spring.boot.rocketmq.starter.processor.proxy;

import com.wpj.spring.boot.rocketmq.starter.annotation.ProducerChannel;
import com.wpj.spring.boot.rocketmq.starter.repertory.ChannelRepertory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.Set;

/**
 * @author wangpejian
 * @date 19-3-28 上午10:28
 */
@Slf4j
@Configuration
public class ProxyProcessor implements BeanDefinitionRegistryPostProcessor, ResourceLoaderAware, EnvironmentAware {

    private ResourceLoader resourceLoader;

    private Environment environment;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        scanRocketChannel(registry);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    /**
     * 扫描所有添加注解的接口
     *
     * @param registry
     */
    public void scanRocketChannel(BeanDefinitionRegistry registry) {

        ClassPathScanningCandidateComponentProvider scanner = getScanner();
        scanner.setResourceLoader(this.resourceLoader);
        AnnotationTypeFilter annotationTypeFilter = new AnnotationTypeFilter(ProducerChannel.class);
        scanner.addIncludeFilter(annotationTypeFilter);

        Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents("**/**");

        for (BeanDefinition candidateComponent : candidateComponents) {
            if (candidateComponent instanceof AnnotatedBeanDefinition) {
                // verify annotated class is an interface
                AnnotatedBeanDefinition beanDefinition = (AnnotatedBeanDefinition) candidateComponent;
                AnnotationMetadata annotationMetadata = beanDefinition.getMetadata();
                Assert.isTrue(annotationMetadata.isInterface(), "@ProducerChannel can only be specified on an interface");

                Map<String, Object> attributes = annotationMetadata.getAnnotationAttributes(ProducerChannel.class.getCanonicalName());
                String channelName = (String) attributes.get("value");

                BeanDefinitionHolder holder = getBeanDefinitionHolder(annotationMetadata, channelName);
                registerBean(holder, registry);
            }
        }
    }

    protected ClassPathScanningCandidateComponentProvider getScanner() {
        return new ClassPathScanningCandidateComponentProvider(false, this.environment) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                boolean isCandidate = false;
                if (beanDefinition.getMetadata().isIndependent()) {
                    if (!beanDefinition.getMetadata().isAnnotation()) {
                        isCandidate = true;
                    }
                }
                return isCandidate;
            }
        };
    }

    /**
     * 获取Bean定义处理器
     */
    protected BeanDefinitionHolder getBeanDefinitionHolder(AnnotationMetadata annotationMetadata, String channelName) {
        String className = annotationMetadata.getClassName();
        BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(RocketClientFactoryBean.class);
        definitionBuilder.addPropertyReference("channelRepertory", ChannelRepertory.CHANNEL_REPERTORY_BEAN_NAME);
        definitionBuilder.addPropertyValue("type", className);

        AbstractBeanDefinition beanDefinition = definitionBuilder.getBeanDefinition();
        return new BeanDefinitionHolder(beanDefinition, className);
    }

    /**
     * 注入接口的代理对象定义
     *
     * @param definitionHolder
     * @param registry
     */
    protected void registerBean(BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry) {
        registry.registerBeanDefinition(definitionHolder.getBeanName(), definitionHolder.getBeanDefinition());
    }


}