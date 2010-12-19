package org.jboss.seam.solder.log;

import java.util.Collection;
import java.util.HashSet;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessProducerMethod;

import org.jboss.logging.MessageBundle;
import org.jboss.logging.MessageLogger;
import org.jboss.seam.solder.bean.NarrowingBeanBuilder;

/**
 * Adds LoggerProducers to the deployment, and detects and installs beans for any
 * typed loggers defined.
 * 
 * @author Pete Muir
 * 
 */
public class LoggerExtension implements Extension
{

   static
   {
      // Until we get tooling, set this globally
      System.setProperty("jboss.i18n.generate-proxies", "true");
   }

   private final Collection<AnnotatedType<?>> messageLoggerTypes;
   private final Collection<AnnotatedType<?>> messageBundleTypes;
   private Bean<Object> loggerProducerBean;
   private Bean<Object> bundleProducerBean;

   LoggerExtension()
   {
      this.messageLoggerTypes = new HashSet<AnnotatedType<?>>();
      this.messageBundleTypes = new HashSet<AnnotatedType<?>>();
   }

   void addProducer(@Observes BeforeBeanDiscovery event, BeanManager beanManager)
   {
      event.addAnnotatedType(beanManager.createAnnotatedType(LoggerProducers.class));
   }

   void detectInterfaces(@Observes ProcessAnnotatedType<?> event, BeanManager beanManager)
   {
      AnnotatedType<?> type = event.getAnnotatedType();
      if (type.isAnnotationPresent(MessageLogger.class))
      {
         messageLoggerTypes.add(type);
      }
      if (type.isAnnotationPresent(MessageBundle.class))
      {
         messageBundleTypes.add(type);
      }
   }

   // FIXME cannot use optimal ProcessProducerMethod<LoggerProducers, Object> because of a bug in Weld 1.1.0.CR1
   void detectProducers(@Observes ProcessProducerMethod<Object, Object> event)
   {
      if (event.getBean().getBeanClass().equals(LoggerProducers.class))
      {
         if (event.getAnnotatedProducerMethod().isAnnotationPresent(TypedLogger.class))
         {
            this.loggerProducerBean = event.getBean();
         }
         if (event.getAnnotatedProducerMethod().isAnnotationPresent(TypedMessageBundle.class))
         {
            this.bundleProducerBean = event.getBean();
         }
      }
   }

   void installBeans(@Observes AfterBeanDiscovery event, BeanManager beanManager)
   {
      for (AnnotatedType<?> type : messageLoggerTypes)
      {
         event.addBean(createMessageLoggerBean(loggerProducerBean, type, beanManager));
      }
      for (AnnotatedType<?> type : messageBundleTypes)
      {
         event.addBean(createMessageBundleBean(bundleProducerBean, type, beanManager));
      }
   }
   
   private static <T> Bean<T> createMessageLoggerBean(Bean<Object> delegate, AnnotatedType<T> type, BeanManager beanManager)
   {
      return new NarrowingBeanBuilder<T>(delegate, beanManager).readFromType(type).types(type.getBaseType(), Object.class).create();
   }
   
   private static <T> Bean<T> createMessageBundleBean(Bean<Object> delegate, AnnotatedType<T> type, BeanManager beanManager)
   {
      return new NarrowingBeanBuilder<T>(delegate, beanManager).readFromType(type).types(type.getBaseType(), Object.class).addQualifier(MessageBundleLiteral.INSTANCE).create();
   }
   
   void cleanup(@Observes AfterDeploymentValidation event)
   {
      // defensively clear the set to help with gc
      this.messageLoggerTypes.clear();
      this.messageBundleTypes.clear();
   }

}
