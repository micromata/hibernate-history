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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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
import de.micromata.hibernate.history.HistoryUserRetriever;
import de.micromata.hibernate.history.delta.AssociationPropertyDelta;
import de.micromata.hibernate.history.delta.CollectionPropertyDelta;
import de.micromata.hibernate.history.delta.PropertyDelta;
import de.micromata.hibernate.history.delta.SimplePropertyDelta;

public class UserTest extends HibernateTestBase
{
  /** The logger */
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UserTest.class);

  public void testSessionFactory() throws Exception
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

    final Integer groupId = (Integer) tx.execute(new TransactionCallback() {
      public Object doInTransaction(TransactionStatus status)
      {
        return hibernate.execute(new HibernateCallback() {
          public Object doInHibernate(Session session) throws HibernateException
          {
            GroupDO group = alterUserName(userId, session);
            return group.getId();
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
            alterUserGroup(userId, groupId, session);
            return null;
          }
        });
      }
    });

    HistoryAdapter ad = new HistoryAdapter();
    ad.setSessionFactory(sessionFactory);
    HistoryEntry[] historyEntries = ad.getHistoryEntries(UserDO.class, userId);
    for (HistoryEntry he : historyEntries) {
      log.debug(he);
    }

    assertEquals(4, historyEntries.length);
    assertEquals(HistoryEntryType.UPDATE, historyEntries[0].getType());
    assertEquals(HistoryEntryType.UPDATE, historyEntries[1].getType());
    assertEquals(HistoryEntryType.UPDATE, historyEntries[2].getType());
    assertEquals(HistoryEntryType.INSERT, historyEntries[3].getType());

    // Check, ob Assoziatonen korrekt gespeichert sind
    List<PropertyDelta> delta = historyEntries[0].getDelta();
    assertNull(historyEntries[0].getComment());
    assertNull(historyEntries[1].getComment());
    assertEquals("Creating test user", historyEntries[2].getComment());
    assertEquals("Creating test user", historyEntries[3].getComment());
    assertEquals(1, delta.size());
    assertTrue(delta.get(0) instanceof AssociationPropertyDelta);
    final AssociationPropertyDelta apd = (AssociationPropertyDelta) delta.get(0);
    hibernate.execute(new HibernateCallback() {
      @SuppressWarnings("unchecked")
      public Object doInHibernate(Session session) throws HibernateException
      {
        assertNull(apd.getOldObjectValue(session));
        assertEquals(groupId, ((GroupDO) apd.getNewObjectValue(session)).getId());
        return null;
      }
    });

    // Check, ob Änderungen an Basistypen korrekt gespeichert sind
    delta = historyEntries[1].getDelta();
    assertEquals(1, delta.size());
    assertTrue(delta.get(0) instanceof SimplePropertyDelta);
    final SimplePropertyDelta spd = (SimplePropertyDelta) delta.get(0);
    hibernate.execute(new HibernateCallback() {
      @SuppressWarnings("unchecked")
      public Object doInHibernate(Session session) throws HibernateException
      {
        assertEquals("Foo", spd.getOldObjectValue(session));
        assertEquals("Bar", spd.getNewObjectValue(session));
        return null;
      }
    });

    // Check, ob Änderungen an Sets korrekt gespeichert sind
    delta = historyEntries[2].getDelta();
    assertEquals(1, delta.size());
    assertTrue(delta.get(0) instanceof CollectionPropertyDelta);
    final CollectionPropertyDelta cpd = (CollectionPropertyDelta) delta.get(0);
    hibernate.execute(new HibernateCallback() {
      @SuppressWarnings("unchecked")
      public Object doInHibernate(Session session) throws HibernateException
      {
        List<Object> oldObjectValue = (List<Object>) cpd.getOldObjectValue(session);
        List<Object> newObjectValue = (List<Object>) cpd.getNewObjectValue(session);
        assertNotNull(oldObjectValue);
        assertNotNull(newObjectValue);
        assertEquals(0, oldObjectValue.size());
        assertEquals(1, newObjectValue.size());
        assertTrue(newObjectValue.get(0) instanceof GroupDO);
        return null;
      }
    });
  }

  public void testAdapter() throws Exception
  {
    assertNotNull(sessionFactory);
    final HistoryAdapter ha = new HistoryAdapter();
    ha.setHibernateTemplate(hibernate);
    tx.execute(new TransactionCallback() {
      public Object doInTransaction(TransactionStatus status)
      {
        return hibernate.execute(new HibernateCallback() {
          public Object doInHibernate(Session session) throws HibernateException
          {
            runAdapterTest(ha, session);
            return null;
          }
        });
      }
    });

  }

  @SuppressWarnings( { "cast", "unchecked"})
  private HistoryEntry[] runAdapterTest(final HistoryAdapter ha, Session session)
  {
    HistoryInterceptor.setComment("Creating test user");
    UserDO user = new UserDO();
    session.save(user);

    GroupDO group = new GroupDO();
    session.save(group);
    session.flush();
    ha.createHistoryEntry(user, user.getId(), new HistoryUserRetriever() {
      public String getPrincipal()
      {
        return "testCaseUser";
      }
    }, "sampleData", String.class, "old", "new");

    ha.createHistoryEntry(user, user.getId(), new HistoryUserRetriever() {
      public String getPrincipal()
      {
        return "testCaseUser";
      }
    }, "otherData", Collection.class, null, Collections.singleton(group));

    ha.createHistoryEntry(user, user.getId(), new HistoryUserRetriever() {
      public String getPrincipal()
      {
        return "testCaseUser";
      }
    }, "groupData", GroupDO.class, group, null);

    HistoryEntry[] historyEntries = ha.getHistoryEntries(user);
    assertEquals(null, (GroupDO) historyEntries[0].getDelta().get(0).getNewObjectValue(session));
    assertEquals(group, (GroupDO) historyEntries[0].getDelta().get(0).getOldObjectValue(session));
    assertEquals(group, ((List<GroupDO>) historyEntries[1].getDelta().get(0).getNewObjectValue(session)).get(0));
    assertEquals("new", (String) historyEntries[2].getDelta().get(0).getNewObjectValue(session));
    assertEquals("old", (String) historyEntries[2].getDelta().get(0).getOldObjectValue(session));
    return historyEntries;
  }

  private UserDO createUser(Session session)
  {
    HistoryInterceptor.setComment("Creating test user");
    UserDO user = new UserDO();
    user.setName("Foo");
    session.save(user);
    session.flush();
    log.debug("Saved User foo");

    GroupDO group1 = new GroupDO();
    group1.setName("Foo");
    session.save(group1);
    session.flush();

    GroupDO group2 = new GroupDO();
    group2.setName("Bar");
    session.save(group2);
    session.flush();
    log.debug("Saved Group bar");

    log.debug("Loading user foo");
    user = (UserDO) session.load(UserDO.class, user.getId());
    if (user.getGroups() == null) {
      user.setGroups(new HashSet<GroupDO>());
    }
    user.getGroups().add(group1);
    log.debug("added group to user foo");
    session.flush();
    return user;
  }

  private GroupDO alterUserName(final Integer userId, Session session)
  {
    assertEquals(1, session.createCriteria(UserDO.class).list().size());
    UserDO user = (UserDO) session.load(UserDO.class, userId);
    user.setName("Bar");
    GroupDO group = user.getGroups().iterator().next();
    return group;
  }

  private void alterUserGroup(final Integer userId, final Integer groupId, Session session)
  {
    assertEquals(1, session.createCriteria(UserDO.class).list().size());
    UserDO user = (UserDO) session.load(UserDO.class, userId);
    GroupDO group = (GroupDO) session.load(GroupDO.class, groupId);
    user.setMainGroup(group);
  }
}
