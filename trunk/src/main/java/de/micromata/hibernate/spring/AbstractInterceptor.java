/////////////////////////////////////////////////////////////////////////////
//
// $RCSfile: AbstractInterceptor.java,v $
//
// Project   BaseApp
//
// Author    Wolfgang Jung (w.jung@micromata.de)
// Created   Mar 7, 2005
//
// $Id: AbstractInterceptor.java,v 1.1 2007/03/08 22:50:48 wolle Exp $
// $Revision: 1.1 $
// $Date: 2007/03/08 22:50:48 $
//
/////////////////////////////////////////////////////////////////////////////
package de.micromata.hibernate.spring;

import java.io.Serializable;
import java.util.Iterator;

import org.hibernate.CallbackException;
import org.hibernate.EntityMode;
import org.hibernate.Interceptor;
import org.hibernate.Transaction;
import org.hibernate.type.Type;

/**
 * 
 * @author Oliver Hutchison
 * @see Interceptor
 */
public class AbstractInterceptor implements Interceptor
{
  protected Interceptor delegate = null;

  /**
   * Constructor
   */
  public AbstractInterceptor()
  {
    super();
  }

  public void setDelegateInterceptor(Interceptor delegateInterceptor)
  {
    if (delegateInterceptor == this) {
      return;
    }
    this.delegate = delegateInterceptor;
  }

  /**
   * @see org.hibernate.Interceptor#afterTransactionBegin(org.hibernate.Transaction)
   */
  public void afterTransactionBegin(Transaction tx)
  {
    if (delegate != null)
      delegate.afterTransactionBegin(tx);
  }

  /**
   * @see org.hibernate.Interceptor#afterTransactionCompletion(org.hibernate.Transaction)
   */
  public void afterTransactionCompletion(Transaction tx)
  {
    if (delegate != null)
      delegate.afterTransactionCompletion(tx);
  }

  /**
   * @see org.hibernate.Interceptor#beforeTransactionCompletion(org.hibernate.Transaction)
   */
  public void beforeTransactionCompletion(Transaction tx)
  {
    if (delegate != null)
      delegate.beforeTransactionCompletion(tx);
  }

  /**
   * @see org.hibernate.Interceptor#findDirty(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.Object[],
   *      java.lang.String[], org.hibernate.type.Type[])
   */
  public int[] findDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types)
  {
    if (delegate != null)
      return delegate.findDirty(entity, id, currentState, previousState, propertyNames, types);
    return null;
  }

  /**
   * @see org.hibernate.Interceptor#getEntity(java.lang.String, java.io.Serializable)
   */
  public Object getEntity(String entityName, Serializable id) throws CallbackException
  {
    if (delegate != null)
      return delegate.getEntity(entityName, id);
    return null;
  }

  /**
   * @see org.hibernate.Interceptor#getEntityName(java.lang.Object)
   */
  public String getEntityName(Object object) throws CallbackException
  {
    if (delegate != null)
      return delegate.getEntityName(object);
    return null;
  }

  /**
   * @see org.hibernate.Interceptor#instantiate(java.lang.String, org.hibernate.EntityMode, java.io.Serializable)
   */
  public Object instantiate(String entityName, EntityMode entityMode, Serializable id) throws CallbackException
  {
    if (delegate != null)
      return delegate.instantiate(entityName, entityMode, id);
    return null;
  }

  /**
   * @see org.hibernate.Interceptor#isTransient(java.lang.Object)
   */
  public Boolean isTransient(Object entity)
  {
    if (delegate != null)
      return delegate.isTransient(entity);
    return null;
  }

  /**
   * @see org.hibernate.Interceptor#onCollectionRecreate(java.lang.Object, java.io.Serializable)
   */
  public void onCollectionRecreate(Object collection, Serializable key) throws CallbackException
  {
    if (delegate != null)
      delegate.onCollectionRecreate(collection, key);
  }

  /**
   * @see org.hibernate.Interceptor#onCollectionRemove(java.lang.Object, java.io.Serializable)
   */
  public void onCollectionRemove(Object collection, Serializable key) throws CallbackException
  {
    if (delegate != null)
      delegate.onCollectionRemove(collection, key);
  }

  /**
   * @see org.hibernate.Interceptor#onCollectionUpdate(java.lang.Object, java.io.Serializable)
   */
  public void onCollectionUpdate(Object collection, Serializable key) throws CallbackException
  {
    if (delegate != null)
      delegate.onCollectionUpdate(collection, key);
  }

  /**
   * @see org.hibernate.Interceptor#onDelete(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.String[],
   *      org.hibernate.type.Type[])
   */
  public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) throws CallbackException
  {
    if (delegate != null)
      delegate.onDelete(entity, id, state, propertyNames, types);
  }

  /**
   * @see org.hibernate.Interceptor#onFlushDirty(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.Object[],
   *      java.lang.String[], org.hibernate.type.Type[])
   */
  public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames,
      Type[] types) throws CallbackException
  {
    if (delegate != null)
      return delegate.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
    return false;
  }

  /**
   * @see org.hibernate.Interceptor#onLoad(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.String[],
   *      org.hibernate.type.Type[])
   */
  public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) throws CallbackException
  {
    if (delegate != null)
      return delegate.onLoad(entity, id, state, propertyNames, types);
    return false;
  }

  /**
   * @see org.hibernate.Interceptor#onPrepareStatement(java.lang.String)
   */
  public String onPrepareStatement(String sql)
  {
    if (delegate != null)
      return delegate.onPrepareStatement(sql);
    return sql;
  }

  /**
   * @see org.hibernate.Interceptor#onSave(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.String[],
   *      org.hibernate.type.Type[])
   */
  public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) throws CallbackException
  {
    if (delegate != null)
      return delegate.onSave(entity, id, state, propertyNames, types);
    return false;
  }

  /**
   * @see org.hibernate.Interceptor#postFlush(java.util.Iterator)
   */
  @SuppressWarnings("unchecked")
  public void postFlush(Iterator entities) throws CallbackException
  {
    if (delegate != null)
      delegate.postFlush(entities);
  }

  /**
   * @see org.hibernate.Interceptor#preFlush(java.util.Iterator)
   */
  @SuppressWarnings("unchecked")
  public void preFlush(Iterator entities) throws CallbackException
  {
    if (delegate != null)
      delegate.preFlush(entities);
  }

}