/////////////////////////////////////////////////////////////////////////////
//
// $RCSfile: SpringInterceptor.java,v $
//
// Project   BaseApp
//
// Author    Wolfgang Jung (w.jung@micromata.de)
// Created   Mar 7, 2005
//
// $Id: SpringInterceptor.java,v 1.1 2007/03/08 22:50:48 wolle Exp $
// $Revision: 1.1 $
// $Date: 2007/03/08 22:50:48 $
//
/////////////////////////////////////////////////////////////////////////////
package de.micromata.hibernate.spring;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.hibernate.CallbackException;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.SessionFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

/**
 * 
 * @author Oliver Hutchison
 * @see Interceptor
 */
public class SpringInterceptor extends AbstractInterceptor implements InitializingBean, BeanFactoryAware
{
  private Logger log = Logger.getLogger(this.getClass());

  private BeanFactory beanFactory;

  private SessionFactory sessionFactory;

  private String sessionFactoryBeanName;

  private Map<String, String> persistantClassBeanNames = new HashMap<String, String>();

  /**
   * Constructor
   */
  public SpringInterceptor()
  {
    super();
  }

  public void setBeanFactory(BeanFactory beanFactory)
  {
    log.debug("Setting beanfactory  to " + beanFactory);
    this.beanFactory = beanFactory;
  }

  public void setSessionFactory(SessionFactory sessionFactory)
  {
    this.sessionFactory = sessionFactory;
  }

  public void setSessionFactoryBeanName(String sessionFactoryBeanName)
  {
    this.sessionFactoryBeanName = sessionFactoryBeanName;
  }

  public void setPersistantClassBeanNames(Properties persistantClassBeanNames)
  {
    log.debug("Setting persitent to " + persistantClassBeanNames);
    for (Iterator<Object> i = persistantClassBeanNames.keySet().iterator(); i.hasNext();) {
      String className = (String) i.next();
      String beanName = persistantClassBeanNames.getProperty(className);
      log.debug("storing " + beanName + " under " + className);
      persistantClassBeanNames.put(className, beanName);
    }
  }

  public void afterPropertiesSet()
  {
    log.debug("Setting properties");
    if (beanFactory == null) {
      throw new IllegalArgumentException("beanFactory is required");
    }

    if ((sessionFactory == null) && !StringUtils.hasText(sessionFactoryBeanName)) {
      throw new IllegalArgumentException("sessionFactory or sessionFactoryBeanName is required");
    }
  }

  @Override
  public Object instantiate(String name, EntityMode mode, Serializable id) throws CallbackException
  {
    if (name == null || mode != EntityMode.POJO || persistantClassBeanNames.containsKey(name) == false) {
      return super.instantiate(name, mode, id);
    }
    if (beanFactory.isSingleton(name)) {
      throw new UnsupportedOperationException("Bean name [" + name + "] must be a prototype. i.e. singleton=\"false\"");
    }
    Object newEntity = beanFactory.getBean(name);
    try {
      if (log.isDebugEnabled()) {
        log.debug("loading new instance of " + name + " named " + name + " from beanFactory");
      }
      BeanWrapper wrapper = new BeanWrapperImpl(newEntity);
      wrapper.setPropertyValue(getSessionFactory().getClassMetadata(name).getIdentifierPropertyName(), id);
    } catch (HibernateException e) {
      throw new CallbackException("Error getting identifier property for class " + name, e);
    }
    return newEntity;
  }

  private SessionFactory getSessionFactory()
  {
    if (sessionFactory == null) {
      sessionFactory = (SessionFactory) beanFactory.getBean(sessionFactoryBeanName);
    }
    return sessionFactory;
  }
}