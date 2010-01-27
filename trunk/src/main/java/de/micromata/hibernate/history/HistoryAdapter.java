/////////////////////////////////////////////////////////////////////////////
//
// $RCSfile: HistoryAdapter.java,v $
//
// Project   HibernateHistory
//
// Author    Wolfgang Jung (w.jung@micromata.de)
// Created   Mar 18, 2005
//
// $Id: HistoryAdapter.java,v 1.2 2007-06-13 09:00:25 wolle Exp $
// $Revision: 1.2 $
// $Date: 2007-06-13 09:00:25 $
//
/////////////////////////////////////////////////////////////////////////////
package de.micromata.hibernate.history;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import net.sf.cglib.proxy.Enhancer;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.PredicateUtils;
import org.apache.commons.lang.ClassUtils;
import org.hibernate.Criteria;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import de.micromata.hibernate.history.delta.DeltaSet;
import de.micromata.hibernate.history.delta.DeltaSetCalculator;
import de.micromata.hibernate.history.delta.PropertyDelta;

/**
 * @author Wolfgang Jung (w.jung@micromata.de)
 * 
 */
public class HistoryAdapter
{
  private HibernateTemplate template;

  private TransactionTemplate tx;

  public void setSessionFactory(SessionFactory sessionFactory)
  {
    setHibernateTemplate(new HibernateTemplate(sessionFactory));
    template.setAlwaysUseNewSession(false);
  }

  public void setHibernateTemplate(HibernateTemplate hibernateTemplate)
  {
    template = hibernateTemplate;
    tx = new TransactionTemplate(new HibernateTransactionManager(template.getSessionFactory()));
  }

  @SuppressWarnings("unchecked")
  private HistoryEntry[] getHistoryEntries(final Class entity, final Number id, Session session) throws HibernateException
  {
    if (id == null)
      return new HistoryEntry[0];
    Criteria criteria = session.createCriteria(HistoryEntry.class);
    criteria = criteria.add(Expression.eq("className", ClassUtils.getShortClassName(entity)));
    criteria = criteria.add(Expression.eq("entityId", id));
    criteria = criteria.setCacheable(true);
    criteria = criteria.setCacheRegion("historyItemCache");
    criteria = criteria.addOrder(Order.desc("timestamp"));
    List result = criteria.list();
    return (HistoryEntry[]) CollectionUtils.select(result, PredicateUtils.uniquePredicate()).toArray(new HistoryEntry[0]);
  }
  
  public HistoryEntry[] getHistoryEntries(final Class<?> entity, final Number id)
  {
    if (id == null)
      return new HistoryEntry[0];
    return (HistoryEntry[]) tx.execute(new TransactionCallback() {
      public Object doInTransaction(TransactionStatus status)
      {
        return template.execute(new HibernateCallback() {
          public Object doInHibernate(Session session) throws HibernateException
          {
            try {
              return getHistoryEntries(entity, id, session);
            } finally {
              session.close();
            }
          }
        });
      }
    });
  }

  public HistoryEntry[] getHistoryEntries(final Object persistent, final Session session) throws HibernateException
  {
    if (persistent == null)
      return new HistoryEntry[0];
    Class<?> clazz = persistent.getClass();
    while (Enhancer.isEnhanced(clazz)) {
      clazz = clazz.getSuperclass();
    }
    Number pk = (Number) session.getSessionFactory().getClassMetadata(clazz).getIdentifier(persistent, EntityMode.POJO);
    return getHistoryEntries(persistent.getClass(), pk, session);
  }
  
  public HistoryEntry[] getHistoryEntries(final Object persistent)
  {
    if (persistent == null)
      return new HistoryEntry[0];

    return (HistoryEntry[]) tx.execute(new TransactionCallback() {
      public Object doInTransaction(TransactionStatus status)
      {
        return template.execute(new HibernateCallback() {
          public Object doInHibernate(Session session) throws HibernateException
          {
            try {
              return getHistoryEntries(persistent, session);
            } finally {
              session.close();
            }
          }
        });
      }
    });
  }

  /**
   * Erzeugt einen neuen Historyenintrag zu einem Objekt. Dabei kann zu dem Objekt <code>entity</code>, das bereits persistent 
   * gespeichert sein muss, eine zusätzliche Property-Änderung hinterlegt werden.
   * @param <T> der Typ 
   * @param entity Das persistente Objekt, zu dem die Änderung protokolliert werden soll
   * @param id der Primärschlüssel des persistenten Objekts.
   * @param user Der Benutzer, der die Änderung verursacht hat 
   * @param property Ein Name für die Eigenschaft, die geändert wurde. Der Name sollte dem Aufbau einem Identifier entsprechen. 
   * @param valueClass Der Typ der Änderungen
   * @param oldValue der alte Wert 
   * @param newValue der neue Wert
   */
  public <T> void createHistoryEntry(Object entity, Number id, HistoryUserRetriever user, String property, Class<T> valueClass, Object oldValue, Object newValue)
  {
    HistoryEntry he = new HistoryEntry();
    he.setClassName(ClassUtils.getShortClassName(entity.getClass()));
    he.setEntityId(new Integer(id.intValue()));
    he.setTimestamp(new Timestamp(new Date().getTime()));
    he.setType(HistoryEntryType.UPDATE);
    he.setUserName(user.getPrincipal());
    PropertyDelta delta = DeltaSetCalculator.getDeltaOrNull(entity, template.getSessionFactory(), property, valueClass, oldValue, newValue);
    DeltaSet deltaSet = new DeltaSet();
    deltaSet.setEntity(entity.getClass());
    deltaSet.setId(he.getEntityId());
    deltaSet.addDelta(delta);
    he.setDeltaSet(deltaSet);
    template.save(he);
  }
}
