/////////////////////////////////////////////////////////////////////////////
//
// $RCSfile: HibernateTestBase.java,v $
//
// Project   hib3test
//
// Author    Wolfgang Jung (w.jung@micromata.de)
// Created   Dec 16, 2005
// Copyright Micromata Dec 16, 2005
//
// $Id: HibernateTestBase.java,v 1.1 2007/03/08 22:50:50 wolle Exp $
// $Revision: 1.1 $
// $Date: 2007/03/08 22:50:50 $
//
/////////////////////////////////////////////////////////////////////////////
package de.micromata.hib3.model;

import java.util.Properties;

import junit.framework.TestCase;

import org.apache.commons.dbcp.BasicDataSource;
import org.hibernate.SessionFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import de.micromata.hibernate.history.HistoryInterceptor;
import de.micromata.hibernate.spring.AutoSessionFactoryBean;

public abstract class HibernateTestBase extends TestCase
{
  /** The logger */
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(HibernateTestBase.class);

  protected SessionFactory sessionFactory;

  protected HibernateTemplate hibernate;

  protected TransactionTemplate tx;

  protected BasicDataSource ds = new BasicDataSource();

  @Override
  protected void setUp() throws Exception
  {
    log.debug("Starting");
    ds.setDriverClassName("org.hsqldb.jdbcDriver");
    ds.setUrl("jdbc:hsqldb:mem:hib3DB");
    ds.setUsername("sa");
    ds.setPassword("");
    ds.setMaxActive(2);
    ds.setDefaultAutoCommit(false);

    AutoSessionFactoryBean asfb = new AutoSessionFactoryBean();
    asfb.setAnnotatedClasses(new Class[] { de.micromata.hib3.model.UserDO.class, de.micromata.hib3.model.GroupDO.class});
    asfb.setDataSource(ds);
    asfb.setSchemaUpdate(true);
    Properties props = new Properties();
    props.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
    props.setProperty("hibernate.cache.use_second_level_cache", "false");
    props.setProperty("hibernate.cache.use_query_cache", "false");
    asfb.setHibernateProperties(props);
    HistoryInterceptor historyInterceptor = new HistoryInterceptor(new HistoryUser());
    asfb.setEntityInterceptor(historyInterceptor);
    changeAutoSessionFactoryBean(asfb);
    asfb.afterPropertiesSet();
    sessionFactory = (SessionFactory) asfb.getObject();
    historyInterceptor.setSessionFactory(sessionFactory);
    hibernate = new HibernateTemplate(sessionFactory);
    tx = new TransactionTemplate(new HibernateTransactionManager(sessionFactory));
    log.debug("Initialized");
  }

  protected void changeAutoSessionFactoryBean(AutoSessionFactoryBean asfb)
  {
  }

  @Override
  protected void tearDown() throws Exception
  {
    sessionFactory.close();
    JdbcTemplate jdbc = new JdbcTemplate(ds);
    jdbc.execute("SHUTDOWN");
  }
}
