/////////////////////////////////////////////////////////////////////////////
//
// $RCSfile: UserTest.java,v $
//
// Project   hib3test
//
// Author    Wolfgang Jung (w.jung@micromata.de)
// Created   Dec 16, 2005
// Copyright Micromata Dec 16, 2005
//
// $Id: UserTest.java,v 1.1 2007/03/08 22:50:50 wolle Exp $
// $Revision: 1.1 $
// $Date: 2007/03/08 22:50:50 $
//
/////////////////////////////////////////////////////////////////////////////
package de.micromata.hib3.model;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import de.micromata.hibernate.history.HistoryAdapter;
import de.micromata.hibernate.history.HistoryEntry;
import de.micromata.hibernate.history.HistoryEntryType;
import de.micromata.hibernate.history.HistoryInterceptor;
import de.micromata.hibernate.history.delta.PropertyDelta;
import de.micromata.hibernate.history.delta.SimplePropertyDelta;

public class TimeTest extends HibernateTestBase
{

  public void testDateDelta() throws Exception
  {
    assertNotNull(sessionFactory);
    Map< ? , ? > allClassMetadata = sessionFactory.getAllClassMetadata();
    assertEquals(7, allClassMetadata.size());
    final Integer userId = (Integer) tx.execute(new TransactionCallback() {
      public Object doInTransaction(TransactionStatus status)
      {
        return hibernate.execute(new HibernateCallback() {
          public Object doInHibernate(Session session) throws HibernateException
          {
            UserDO user = createUser(session);
            return user.getId();
          }
        });
      }
    });
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    final long oldTime = sdf.parse("17.03.2008 14:12:19").getTime();
    final long newTime = sdf.parse("18.02.2007 12:11:17").getTime();
    tx.execute(new TransactionCallback() {
      public Object doInTransaction(TransactionStatus status)
      {
        return hibernate.execute(new HibernateCallback() {
          public Object doInHibernate(Session session) throws HibernateException
          {
            assertEquals(1, session.createCriteria(UserDO.class).list().size());
            UserDO user = (UserDO) session.load(UserDO.class, userId);

            user.setDate(new java.sql.Date(oldTime));
            user.setJuDate(new java.util.Date(oldTime));
            user.setTimestamp(new java.sql.Timestamp(oldTime));
            return null;
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
            assertEquals(1, session.createCriteria(UserDO.class).list().size());
            UserDO user = (UserDO) session.load(UserDO.class, userId);

            user.setDate(new java.sql.Date(newTime));
            user.setJuDate(new java.util.Date(newTime));
            user.setTimestamp(new java.sql.Timestamp(newTime));
            return null;
          }
        });
      }
    });

    HistoryAdapter ad = new HistoryAdapter();
    ad.setSessionFactory(sessionFactory);
    HistoryEntry[] historyEntries = ad.getHistoryEntries(UserDO.class, userId);
    assertEquals(3, historyEntries.length);
    assertEquals(HistoryEntryType.INSERT, historyEntries[2].getType());
    assertEquals(HistoryEntryType.UPDATE, historyEntries[1].getType());
    assertEquals(HistoryEntryType.UPDATE, historyEntries[0].getType());

    List<PropertyDelta> delta = historyEntries[1].getDelta();
    for (PropertyDelta pd : delta) {
      assertEquals(SimplePropertyDelta.class, pd.getClass());
      SimplePropertyDelta spd = (SimplePropertyDelta) pd;
      assertNull(spd.getOldValue());
      assertNull(spd.getOldObjectValue(null));
      assertNotNull(spd.getNewValue());

      Object newValue = spd.getNewObjectValue(null);
      assertNotNull(newValue);

      if (spd.getPropertyName().equals("timestamp")) {
        assertEquals(Timestamp.class, newValue.getClass());
        assertEquals(newValue, new Timestamp(oldTime));
      }
      if (spd.getPropertyName().equals("date")) {
        assertEquals(java.sql.Date.class, newValue.getClass());
        assertEquals(new java.sql.Date(oldTime).toString(), newValue.toString());
      }
      if (spd.getPropertyName().equals("juDate")) {
        assertEquals(Date.class, newValue.getClass());
        assertEquals(newValue, new Date(oldTime));
      }

    }

    delta = historyEntries[0].getDelta();
    for (PropertyDelta pd : delta) {
      assertEquals(SimplePropertyDelta.class, pd.getClass());
      SimplePropertyDelta spd = (SimplePropertyDelta) pd;
      assertNotNull(spd.getOldValue());
      Object oldValue = spd.getOldObjectValue(null);
      Object newValue = spd.getNewObjectValue(null);
      assertNotNull(oldValue);
      assertNotNull(spd.getNewValue());
      assertNotNull(newValue);

      if (spd.getPropertyName().equals("timestamp")) {
        assertEquals(Timestamp.class, oldValue.getClass());
        assertEquals(Timestamp.class, newValue.getClass());
        assertEquals(oldValue, new Timestamp(oldTime));
        assertEquals(newValue, new Timestamp(newTime));
      }
      if (spd.getPropertyName().equals("date")) {
        assertEquals(java.sql.Date.class, oldValue.getClass());
        assertEquals(java.sql.Date.class, newValue.getClass());
        assertEquals(new java.sql.Date(oldTime).toString(), oldValue.toString());
        assertEquals(new java.sql.Date(newTime).toString(), newValue.toString());
      }
      if (spd.getPropertyName().equals("juDate")) {
        assertEquals(Date.class, oldValue.getClass());
        assertEquals(Date.class, newValue.getClass());
        assertEquals(oldValue, new Date(oldTime));
        assertEquals(newValue, new Date(newTime));
      }
    }

  }

  private UserDO createUser(Session session)
  {
    HistoryInterceptor.setComment("Creating test user");
    UserDO user = new UserDO();
    user.setName("Foo");
    session.save(user);
    session.flush();
    return user;
  }

}
