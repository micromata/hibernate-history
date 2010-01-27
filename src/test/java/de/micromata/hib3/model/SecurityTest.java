/////////////////////////////////////////////////////////////////////////////
//
// $RCSfile: SecurityTest.java,v $
//
// Project   hib3test
//
// Author    Wolfgang Jung (w.jung@micromata.de)
// Created   Dec 16, 2005
// Copyright Micromata Dec 16, 2005
//
// $Id: SecurityTest.java,v 1.1 2007/03/08 22:50:50 wolle Exp $
// $Revision: 1.1 $
// $Date: 2007/03/08 22:50:50 $
//
/////////////////////////////////////////////////////////////////////////////
package de.micromata.hib3.model;

import java.security.AccessControlException;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.ArrayUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import de.micromata.hibernate.security.AccessControlChecker;
import de.micromata.hibernate.security.AccessControlInterceptor;
import de.micromata.hibernate.spring.AutoSessionFactoryBean;

public class SecurityTest extends HibernateTestBase
{
  /** The logger */
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SecurityTest.class);

  private AccessControlInterceptor accessControl = new AccessControlInterceptor();

  @Override
  protected void changeAutoSessionFactoryBean(AutoSessionFactoryBean asfb)
  {
    super.changeAutoSessionFactoryBean(asfb);
    accessControl.setAccessControlChecker(new AccessControlChecker() {
      public void checkAccess(String[] permittedRoles) throws AccessControlException
      {
        log.debug("Checking roles " + ArrayUtils.toString(permittedRoles));
        if (permittedRoles.length > 0 && permittedRoles[0].equals("PERMIT")) {
          return;
        }
        throw new AccessControlException("access denied");
      }
    });
    Properties props = new Properties();
    props.put("update.de.micromata.hib3.model.UserDO", "PERMIT,ALL");
    props.put("save.de.micromata.*", "PERMIT");
    props.put("delete.de.micromata.hib3.model.GroupDO", "DENY");
    // props.put("update.de.micromata.hib3.model.GroupDO", "PERMIT");
    accessControl.setAllowed(props);
    asfb.setEntityInterceptor(accessControl);
  }

  @Override
  protected void tearDown() throws Exception
  {

    super.tearDown();
  }

  @Override
  public void setUp() throws Exception
  {
    super.setUp();
    assertNotNull(sessionFactory);
    tx.execute(new TransactionCallback() {
      public Object doInTransaction(TransactionStatus status)
      {
        return hibernate.execute(new HibernateCallback() {
          public Object doInHibernate(Session session) throws HibernateException
          {
            UserDO user = new UserDO();
            user.setName("Foo");
            session.save(user);
            session.flush();
            log.debug("Saved User foo");
            return user.getId();
          }
        });
      }
    });

    tx.execute(new TransactionCallback() {
      public Object doInTransaction(TransactionStatus status)
      {
        return hibernate.execute(new HibernateCallback() {
          public Object doInHibernate(Session session) throws HibernateException
          {
            GroupDO group1 = new GroupDO();
            group1.setName("Foo");
            session.save(group1);
            return group1.getId();
          }
        });
      }
    });
  }

  public void testUpdateUser()
  {
    tx.execute(new TransactionCallback() {
      public Object doInTransaction(TransactionStatus status)
      {
        return hibernate.execute(new HibernateCallback() {
          public Object doInHibernate(Session session) throws HibernateException
          {
            List<?> list = session.createCriteria(UserDO.class).list();
            assertEquals(1, list.size());
            UserDO user = (UserDO) list.get(0);
            user.setName("Bar");
            return null;
          }
        });
      }
    });
  }

  public void testUpdateGroup()
  {
    try {
      tx.execute(new TransactionCallback() {
        public Object doInTransaction(TransactionStatus status)
        {
          return hibernate.execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException
            {
              List<?> list = session.createCriteria(GroupDO.class).list();
              assertEquals(1, list.size());
              GroupDO group = (GroupDO) list.get(0);
              group.setName("another name");
              return null;
            }
          });
        }
      });
      fail();
    } catch (SecurityException ex) {
      // ok
    }

  }

  public void testDeleteGroup()
  {
    try {
      tx.execute(new TransactionCallback() {
        public Object doInTransaction(TransactionStatus status)
        {
          return hibernate.execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException
            {
              List<?> list = session.createCriteria(GroupDO.class).list();
              assertEquals(1, list.size());
              GroupDO group = (GroupDO) list.get(0);
              session.delete(group);
              return null;
            }
          });
        }
      });
      fail();
    } catch (SecurityException ex) {
      // ok
    }
  }

  public void testDeleteUser()
  {
    try {
      tx.execute(new TransactionCallback() {
        public Object doInTransaction(TransactionStatus status)
        {
          return hibernate.execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException
            {
              List<?> list = session.createCriteria(UserDO.class).list();
              assertEquals(1, list.size());
              UserDO user = (UserDO) list.get(0);
              session.delete(user);
              return null;
            }
          });
        }
      });
      fail();
    } catch (SecurityException ex) {
      // ok
    }
  }
}
