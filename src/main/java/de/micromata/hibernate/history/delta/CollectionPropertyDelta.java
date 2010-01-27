/////////////////////////////////////////////////////////////////////////////
//
// $RCSfile: CollectionPropertyDelta.java,v $
//
// Project   BaseApp
//
// Author    Wolfgang Jung (w.jung@micromata.de)
// Created   Mar 7, 2005
//
// $Id: CollectionPropertyDelta.java,v 1.1 2007/03/08 22:50:48 wolle Exp $
// $Revision: 1.1 $
// $Date: 2007/03/08 22:50:48 $
//
/////////////////////////////////////////////////////////////////////////////
package de.micromata.hibernate.history.delta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;

/**
 * @author Wolfgang Jung (w.jung@micromata.de)
 * 
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue(value="S")
public class CollectionPropertyDelta extends PropertyDelta
{

  private transient Set<Object> additions = new HashSet<Object>();

  private transient Set<Object> removals = new HashSet<Object>();

  protected CollectionPropertyDelta()
  {
    // do nothing
  }

  public CollectionPropertyDelta(String propertyName, Class<?> propertyType, Collection<Object> oldValue, Collection<Object> newValue)
  {
    this.propertyName = propertyName;
    this.propertyType = ClassUtils.getShortClassName(propertyType);
    calculateAdditionsAndRemovals(oldValue, newValue);
    this.oldValue = StringUtils.join(removals.iterator(), ",");
    this.newValue = StringUtils.join(additions.iterator(), ",");
  }

  private void calculateAdditionsAndRemovals(Collection<Object> oldValue, Collection<Object> newValue)
  {
    // //////////////////////////////////////////////////////////////////////
    // First, determine additions
    if (newValue != null)
      additions.addAll(newValue);
    if (oldValue != null)
      additions.removeAll(oldValue);

    // //////////////////////////////////////////////////////////////////////
    // Then, determine removals
    if (oldValue != null)
      removals.addAll(oldValue);
    if (newValue != null)
      removals.removeAll(newValue);
  }

  @Override
  public String toString()
  {
    return "changes of " + propertyName + " new=" + newValue + " old=" + oldValue;
  }

  @Override
  public Object getNewObjectValue(final Session session)
  {
    return splitElements(getNewValue(), session);
  }

  private List<Object> splitElements(final String keyList, final Session session)
  {
    List<Object> entityList = new ArrayList<Object>();
    if (StringUtils.isEmpty(keyList) == false) {
      for (String key : keyList.split(",")) {
        if (StringUtils.isEmpty(key)) {
          continue;
        }
        entityList.add(loadItem(propertyType, key, session));
      }
    }
    return entityList;
  }

  @Override
  public Object getOldObjectValue(final Session session)
  {
    return splitElements(getOldValue(), session);
  }
}