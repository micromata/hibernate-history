/////////////////////////////////////////////////////////////////////////////
//
//$RCSfile: HistoryInterceptor.java,v $
//
//Project   BaseApp
//
//Author    Wolfgang Jung (w.jung@micromata.de)
//Created   Mar 7, 2005
//
//$Id: HistoryInterceptor.java,v 1.4 2007-07-19 13:44:15 wolle Exp $
//$Revision: 1.4 $
//$Date: 2007-07-19 13:44:15 $
//
/////////////////////////////////////////////////////////////////////////////
package de.micromata.hibernate.history;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.hibernate.CallbackException;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.type.Type;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import de.micromata.hibernate.history.delta.DeltaSet;
import de.micromata.hibernate.history.delta.DeltaSetCalculator;
import de.micromata.hibernate.spring.AbstractInterceptor;

/**
 * The <code>HistoryInterceptor</code> record chages of specific entities in to a table.
 * <p>
 * It creates entries of the table T_HISTORY_ENTRY which records changes of entities which has the <code>Historizable</code> interface
 * implemented.
 * </p>
 * <p>
 * If the changed entity is of type <code>ExpliciteHistorizable</code> just a subset of valid properties will be recorded.
 * </p>
 * <p>
 * This class use <i>Spring</i> functionality, like the <code>HibernateTemplate</code> to create the history objects (<code>HistoryEntry</code>)
 * in a new <code>Session</code> but over the same Connection.
 * </p>
 * @author Wolfgang Jung (w.jung@micromata.de)
 * @see de.micromata.hibernate.history.Historizable
 * @see de.micromata.hibernate.history.ExpliciteHistorizable
 */
public class HistoryInterceptor extends AbstractInterceptor implements Interceptor, BeanFactoryAware
{
  private ThreadLocal<Map<Integer, EntityChange>> insertMap = new ThreadLocal<Map<Integer, EntityChange>>() {
    @Override
    protected Map<Integer, EntityChange> initialValue()
    {
      return new HashMap<Integer, EntityChange>();
    }
  };

  private static ThreadLocal<String> commentHolder = new ThreadLocal<String>();

  private ThreadLocal<Set<HistoryEntry>> recordedChanges = new ThreadLocal<Set<HistoryEntry>>() {
    @SuppressWarnings("unused")
    public java.util.Set<HistoryEntry> initalValue()
    {
      return new HashSet<HistoryEntry>();
    }
  };

  private Logger log = Logger.getLogger(this.getClass());

  private SessionFactory sessionFactory;

  /**
   * The bean name of the <code>SessionFactory</code> will be set on creation time.
   */
  private String sessionFactoryBeanName;

  /**
   * The <code>BeanFactory</code> to create the <code>SessionFactory</code>.
   */
  private BeanFactory beanFactory;

  /**
   * The context to create and save the record objects.
   */
  private HibernateTemplate templ;

  /**
   * The name of the user which make the changes.
   */
  private HistoryUserRetriever userRetriever;

  public HistoryInterceptor(HistoryUserRetriever retr)
  {
    userRetriever = retr;
  }

  /**
   * Specify a comment for all historyentries in the current transaction. Warning: only the last set comment before a
   * {@link Session#flush()} is used for the history. To use different comments in one transaction, you have to use explicit calls to
   * {@link Session#flush()}.
   *
   * @param comment a comment describing the cause for this change. Must be less than 2000 chars.
   */
  public static void setComment(String comment)
  {
    commentHolder.set(comment);
  }

  /**
   * First set just the <b>name</b> of the factory bean (Spring),
   * @param sessionFactoryBeanName
   * @see #getSessionFactory()
   */
  public void setSessionFactoryBeanName(String sessionFactoryBeanName)
  {
    this.sessionFactoryBeanName = sessionFactoryBeanName;
  }

  public void setSessionFactory(SessionFactory sessionFactory)
  {
    this.sessionFactory = sessionFactory;
  }

  /**
   * Get the <i>Hibernate</i> <code>SessionFactory</code> over it's bean name inside the <i>Spring</i> context (<code>BeanFactory</code>).
   * @return
   */
  protected SessionFactory getSessionFactory()
  {
    if (sessionFactory != null) {
      return sessionFactory;
    }
    sessionFactory = (SessionFactory) beanFactory.getBean(sessionFactoryBeanName);
    return sessionFactory;
  }

  /**
   * Create a <i>Spring</i> <code>HibernateTemplate</code> to work in.
   * @return a new <code>HibernateTemplate</code> over a <u>new</u> session
   * @see org.springframework.orm.hibernate.HibernateTemplate.HibernateTemplate#alwaysUseNewSession
   */
  private HibernateTemplate getTemplate()
  {
    if (templ != null) {
      return templ;
    }
    templ = new HibernateTemplate();
    templ.setSessionFactory(getSessionFactory());
    templ.setAlwaysUseNewSession(true);
    templ.setEntityInterceptor(null);
    // TODO use FLUSH_NEVER for readOnly
    templ.setFlushModeName("FLUSH_AUTO");
    return templ;
  }

  /**
   * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
   */
  public void setBeanFactory(BeanFactory beanFactory) throws BeansException
  {
    this.beanFactory = beanFactory;
  }

  @Override
  public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) throws CallbackException
  {
    if (log.isDebugEnabled())
      log.debug("saving object " + entity);
    if (entity instanceof Historizable) {
      insertMap.get().put(System.identityHashCode(entity), new EntityChange(id, entity));
    }
    return super.onSave(entity, id, state, propertyNames, types);
  }

  @Override
  public void afterTransactionBegin(Transaction tx)
  {
    super.afterTransactionBegin(tx);
    commentHolder.set(null);
    recordedChanges.set(new HashSet<HistoryEntry>());
  }

  @Override
  public int[] findDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types)
  {
    if (entity instanceof NonHistorizable) {
      return super.findDirty(entity, id, currentState, previousState, propertyNames, types);
    }
    boolean mayChanged = true;
    if (currentState != null && previousState != null) {
      mayChanged = false;
      for (int i = 0; i < currentState.length; i++) {
        if (currentState[i] != previousState[i] && ObjectUtils.equals(currentState[i], previousState[i]) == false) {
          mayChanged = true;
          break;
        }
      }
    }
    if (mayChanged == true && (entity instanceof Historizable)) {
      Set<String> validProperties = null;
      Set<String> invalidProperties = null;
      if (entity instanceof ExpliciteHistorizable) {
        validProperties = ((ExpliciteHistorizable) entity).getHistorizableAttributes();
      } else if (entity instanceof ExtendedHistorizable) {
        ExtendedHistorizable extHist = (ExtendedHistorizable) entity;
        if (extHist.getHistorizableAttributes() != null) {
          validProperties = extHist.getHistorizableAttributes();
        }
        if (extHist.getNonHistorizableAttributes() != null) {
          invalidProperties = extHist.getNonHistorizableAttributes();
        }
      }
      DeltaSet changes = DeltaSetCalculator.calculateDeltaSet(getSessionFactory(), validProperties, invalidProperties, id, entity,
          propertyNames, previousState, currentState);
      if (log.isDebugEnabled())
        log.debug("find dirty elements " + entity);
      recordUpdate(changes);
    }
    return super.findDirty(entity, id, currentState, previousState, propertyNames, types);
  }

  @Override
  public void beforeTransactionCompletion(Transaction tx)
  {
    if (sessionFactoryBeanName == null && sessionFactory == null) {
      return;
    }
    getTemplate().execute(new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException
      {
        for (HistoryEntry hist : recordedChanges.get()) {
          hist.setTimestamp(new Timestamp(new Date().getTime()));
          session.save(hist);
        }
        recordedChanges.get().clear();
        session.flush();
        commentHolder.set(null);
        return null;
      }
    });
  }

  @Override
  public synchronized void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
      throws CallbackException
  {
    if (log.isDebugEnabled())
      log.debug("delete elements" + entity);
    if (entity instanceof Historizable) {
      EntityChange delete = new EntityChange(id, entity);
      recordChange(delete, HistoryEntryType.DELETE);
    }
    super.onDelete(entity, id, state, propertyNames, types);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void postFlush(Iterator entities) throws CallbackException
  {
    Map<Integer, EntityChange> inserts = insertMap.get();
    if (inserts.size() == 0) {
      super.postFlush(entities);
      return;
    }
    log.debug("postFlush " + inserts.size() + " elements");
    List<Object> l = new ArrayList<Object>();
    List<Object> objectsToSave = new ArrayList<Object>();

    try {
      while (entities.hasNext()) {
        Object item = entities.next();
        l.add(item);
        EntityChange insert = inserts.get(System.identityHashCode(item));
        if (insert == null) {
          continue;
        }
        if (log.isDebugEnabled())
          log.debug("post flushing " + item);
        Serializable id = getSessionFactory().getClassMetadata(insert.getEntity().getClass()).getIdentifier(insert.getEntity(),
            EntityMode.POJO);
        insert.setId(id);
        if (log.isDebugEnabled()) {
          log.debug("storing entry " + insert.getEntity().getClass() + " with id " + item);
        }
        inserts.remove(insert);
        objectsToSave.add(insert);
      }
    } catch (HibernateException ex) {
      log.warn("Can't record history " + ex, ex);
    } finally {
      insertMap.get().clear();
    }

    for (Iterator it = objectsToSave.iterator(); it.hasNext();) {
      EntityChange insert = (EntityChange) it.next();
      recordChange(insert, HistoryEntryType.INSERT);
    }
    super.postFlush(l.iterator());
  }

  /**
   * Create a new <code>HistoryEntry</code> of the <code>type</code> and save it.
   *
   * @param type
   * @param delete
   * @throws DataAccessException
   * @see #saveHistoryEntry(HistoryEntry, Serializable)
   */
  private void recordChange(EntityChange delete, HistoryEntryType type) throws CallbackException
  {
    HistoryEntry hist = new HistoryEntry();
    hist.setType(type);
    hist.setClassName(ClassUtils.getShortClassName(delete.getEntity().getClass()));
    Serializable id = delete.getId();
    if (id instanceof Number) {
      hist.setEntityId(new Integer(((Number) id).intValue()));
    }
    saveHistoryEntry(hist);
  }

  /**
   * Create a new <code>HistoryEntry</code>, inserts the <code>DeltaSet</code> to record an update and save it.
   * @param changes object which contains the changes as a XML-document
   * @throws DataAccessException
   * @see #saveHistoryEntry(HistoryEntry, Serializable)
   */
  private void recordUpdate(final DeltaSet changes) throws CallbackException
  {
    if (changes.getDeltas().size() == 0) {
      return;
    }
    HistoryEntry hist = new HistoryEntry();
    hist.setType(HistoryEntryType.UPDATE);
    hist.setDeltaSet(changes);
    hist.setClassName(ClassUtils.getShortClassName(changes.getEntity()));
    if (changes.getId() instanceof Number) {
      hist.setEntityId(new Integer(((Number) changes.getId()).intValue()));
    }
    hist.setUserName(userRetriever.getPrincipal());
    hist.setComment(commentHolder.get());
    recordedChanges.get().add(hist);

  }

  /**
   * Save the <code>HistoryEntry</code> over a new <code>HibernateTemplate</code>.
   *
   * @param hist the <code>HistoryEntry</code> to save to
   * @param id
   * @throws DataAccessException
   * @see #getTemplate()
   */
  private void saveHistoryEntry(HistoryEntry hist) throws CallbackException
  {
    if (sessionFactoryBeanName == null && sessionFactory == null) {
      return;
    }

    hist.setTimestamp(new Timestamp(new Date().getTime()));
    hist.setUserName(userRetriever.getPrincipal());
    hist.setComment(commentHolder.get());
    try {
      HibernateTemplate templ = getTemplate();
      templ.save(hist);
    } catch (DataAccessException ex) {
      throw new CallbackException(ex);
    }
  }

}
