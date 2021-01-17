package cn.laifuzhi.joymq.broker.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContainer implements ApplicationContextAware {
    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    public static <T> T getBean(Class<T> clazzName) {
        return context.getBean(clazzName);
    }

    public static <T> T getBean(String name, Class<T> clazzName) {
        return context.getBean(name, clazzName);
    }
}
