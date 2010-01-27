/////////////////////////////////////////////////////////////////////////////
//
// $RCSfile: HistoryTable.java,v $
//
// Project   HibernateHistory
//
// Author    Wolfgang Jung (w.jung@micromata.de)
// Created   Sep 21, 2005
// Copyright Micromata Sep 21, 2005
//
// $Id: HistoryTable.java,v 1.4 2007-06-18 15:55:25 tung Exp $
// $Revision: 1.4 $
// $Date: 2007-06-18 15:55:25 $
//
/////////////////////////////////////////////////////////////////////////////
package de.micromata.hibernate.history.web;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.commons.lang.ClassUtils;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import de.micromata.hibernate.history.HistoryAdapter;
import de.micromata.hibernate.history.HistoryEntry;
import de.micromata.hibernate.history.delta.PropertyDelta;

public class HistoryTable
{
  private static Map<String, HistoryFormatter> formatters = new HashMap<String, HistoryFormatter>();

  /** The logger */
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(HistoryTable.class);

  private static HistoryFormatter defaultFormat;

  private SessionFactory sessionFactory;

  private static Map<String, Class<?>> shortNameToClass = new HashMap<String, Class<?>>();

  public static void setDefaultFormat(HistoryFormatter defaultFormat)
  {
    HistoryTable.defaultFormat = defaultFormat;
  }

  public static void registerFormatter(Class<?> entity, String property, HistoryFormatter formatter)
  {
    formatters.put(entity.getName() + "@" + property, formatter);
  }

  public static void registerFormatter(Class<?> type, HistoryFormatter formatter)
  {
    formatters.put(type.getName(), formatter);
  }

  private static HistoryFormatter findFormatter(Class<?> clazz, String property)
  {
    HistoryFormatter formatter = null;
    if (clazz != null) {
      if (property != null) {
        formatter = formatters.get(clazz.getName() + "@" + property);
        if (formatter != null) {
          return formatter;
        }
      }
      formatter = formatters.get(clazz.getName());
      if (formatter != null) {
        return formatter;
      }

      formatter = findFormatter(clazz.getSuperclass(), property);
      if (formatter != null) {
        return formatter;
      }
    }
    if (property != null) {
      HistoryFormatter typeSpecific = formatters.get(property);
      if (typeSpecific != null) {
        return typeSpecific;
      }
    }
    return defaultFormat;
  }

  private static HistoryFormatter findFormatter(HistoryEntry historyEntry, PropertyDelta delta)
  {
    Class<?> clazz = shortNameToClass.get(historyEntry.getClassName());
    if (delta == null) {
      return findFormatter(clazz, null);
    }
    return findFormatter(clazz, delta.getPropertyName());

  }

  @SuppressWarnings("unchecked")
  public HistoryTable(SessionFactory sessionFactory) throws HibernateException
  {
    this.sessionFactory = sessionFactory;
    for (Iterator<Map.Entry<?,?>> it = sessionFactory.getAllClassMetadata().entrySet().iterator(); it.hasNext();) {
      Map.Entry<?, ?> entry = it.next();
      shortNameToClass.put(ClassUtils.getShortClassName((String) entry.getKey()), ((EntityPersister) entry.getValue()).getClassMetadata()
          .getMappedClass(EntityMode.POJO));
    }
  }

  public Collection<HistoryTableEntry> getHistoryList(Object historyObject, final Locale locale)
  {
    return getHistoryList(new Object[] { historyObject}, locale);
  }

  @SuppressWarnings("unchecked")
  public Collection<HistoryTableEntry> getHistoryList(final Object[] historizedObjects, final Locale locale)
  {
    final Set<HistoryTableEntry> historyList = new TreeSet<HistoryTableEntry>(ComparatorUtils.chainedComparator(new ReverseComparator(
        new BeanComparator("date")), new BeanComparator("property")));

    final HistoryAdapter historyAdapter = new HistoryAdapter();
    historyAdapter.setSessionFactory(sessionFactory);
    final HibernateTemplate hibernate = new HibernateTemplate(sessionFactory);
    hibernate.setAlwaysUseNewSession(false);
    hibernate.execute(new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException
      {
        for (int j = 0; j < historizedObjects.length; j++) {
          HistoryEntry entries[] = historyAdapter.getHistoryEntries(historizedObjects[j], session);
          addToHistoryList(session, locale, historyList, entries);
        }
        session.close();
        return null;
      }
    });
    return historyList;
  }

  /**
   * @param historyList
   * @param hibernate
   * @param entries
   */
  private void addToHistoryList(Session session, final Locale locale, Collection<HistoryTableEntry> historyList, HistoryEntry[] entries)
  {
    for (int i = 0; i < entries.length; i++) {
      HistoryEntry he = entries[i];
      List<PropertyDelta> ds = he.getDelta();
      if (ds == null || ds.size() == 0) {
        Object changed = retrieveChangedObject(session, he);
        HistoryTableEntry hte = new HistoryTableEntry();
        HistoryFormatter formatter = findFormatter(he, null);

        if (formatter.isVisible(session, locale, changed, he, null) == true) {
          hte.setUser(formatter.formatUser(session, locale, changed, he, null));
          hte.setDate(he.getTimestamp());
          hte.setTimestamp(formatter.formatTimestamp(session, locale, changed, he, null));
          hte.setProperty(formatter.formatProperty(session, locale, changed, he, null));
          hte.setOldValue(formatter.formatOldValue(session, locale, changed, he, null));
          hte.setNewValue(formatter.formatNewValue(session, locale, changed, he, null));
          hte.setAction(formatter.formatAction(session, locale, changed, he, null));
          historyList.add(hte);
        }

      } else {
        for (final Iterator<PropertyDelta> it = ds.iterator(); it.hasNext();) {
          PropertyDelta pd = it.next();

          HistoryTableEntry hte = new HistoryTableEntry();
          HistoryFormatter formatter = findFormatter(he, pd);
          Object changed = retrieveChangedObject(session, he);
          if (formatter.isVisible(session, locale, changed, he, pd) == true) {
            hte.setDate(he.getTimestamp());
            hte.setUser(formatter.formatUser(session, locale, changed, he, pd));
            hte.setTimestamp(formatter.formatTimestamp(session, locale, changed, he, pd));
            hte.setProperty(formatter.formatProperty(session, locale, changed, he, pd));
            hte.setOldValue(formatter.formatOldValue(session, locale, changed, he, pd));
            hte.setNewValue(formatter.formatNewValue(session, locale, changed, he, pd));
            hte.setAction(formatter.formatAction(session, locale, changed, he, pd));
            historyList.add(hte);
          }
        }
      }
    }
  }

  private Object retrieveChangedObject(Session session, HistoryEntry he)
  {
    try {
      Class<?> clazz = shortNameToClass.get(he.getClassName());
      if (clazz == null) {
        return null;
      }
      if (log.isDebugEnabled()) {
        log.debug("loading instance " + he.getEntityId() + " of class " + he.getClassName());
      }
      return session.load(clazz, he.getEntityId());
    } catch (Exception ex) {
      log.info("error retrieving history for " + he);
    }
    return null;
  }

}
