package ru.aston.investmentloggerprofilingstarter.annotation.processor;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import ru.aston.investmentloggerprofilingstarter.annotation.Profiling;
import ru.aston.investmentloggerprofilingstarter.mbean.ProfilingController;

import javax.management.ObjectName;
import java.lang.annotation.Annotation;
import java.lang.management.ManagementFactory;
import java.lang.reflect.*;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

public class ProfilingAnnotationBeanPostProcessor implements BeanPostProcessor {
    private ConcurrentHashMap<String, Class> map = new ConcurrentHashMap<>();
    private ProfilingController profilingController = new ProfilingController();
    private Annotation[] annotation;

    public ProfilingAnnotationBeanPostProcessor() throws Exception {
        ManagementFactory.getPlatformMBeanServer().registerMBean(profilingController, new ObjectName("profiling", "name", "controller"));
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        if(beanClass.isAnnotationPresent(Profiling.class)) {
            map.put(beanName, beanClass);
            annotation = beanClass.getAnnotations();
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = map.get(beanName);
        if(beanClass != null) {
            for(Annotation annotation : beanClass.getAnnotations()) {
                System.out.println(annotation.getClass().getName());
            }
            Logger logger = LoggerFactory.getLogger(beanClass);
            if(beanClass.getInterfaces().length > 0) {
                return Proxy.newProxyInstance(beanClass.getClassLoader(), beanClass.getInterfaces(), new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        String result = getResultString(method, args);

                        if (profilingController.isEnabled()) {
                            long start = System.currentTimeMillis();
                            logger.info(method.getName() + " - start; " + result + "Profiling: " + start);
                            Object retVal = Optional.ofNullable(method.invoke(bean, args)).orElse("");
                            long end = System.currentTimeMillis();
                            logger.info(method.getName() + " - end; " + retVal + "Profiling: " + end);
                            return retVal;
                        }
                        else {
                            logger.info(method.getName() + " - start; " + result);
                            Object retVal = Optional.ofNullable(method.invoke(bean, args)).orElse("");
                            logger.info(method.getName() + " - end; " + retVal);
                            return retVal;
                        }
                    }
                });
            }
            else {
                Enhancer enhancer = new Enhancer();
                enhancer.setSuperclass(beanClass);
                enhancer.setCallback(new MethodInterceptor() {
                    @Override
                    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
                        String result = getResultString(method, args);
                        if (profilingController.isEnabled()) {
                            long start = System.currentTimeMillis();
                            logger.info(method.getName() + " - start; " + result + "Profiling: " + start);
                            Object retVal = Optional.ofNullable(method.invoke(bean, args)).orElse("");
                            long end = System.currentTimeMillis();
                            logger.info(method.getName() + " - end; " + retVal + "Profiling: " + end);
                            return retVal;
                        }
                        else {
                            logger.info(method.getName() + " - start; " + result);
                            Object retVal = Optional.ofNullable(method.invoke(bean, args)).orElse("");
                            logger.info(method.getName() + " - end; " + retVal);
                            return retVal;
                        }
                    }
                });
                return enhancer.create();
            }
        }
        return bean;
    }

    private String getResultString(Method method, Object[] args) throws IllegalAccessException, InvocationTargetException {
        Parameter[] parameters = method.getParameters();
        String result = IntStream.range(0, parameters.length)
                .mapToObj(i -> parameters[i].getName() + " = " + args[i])
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        return result;
    }
}
