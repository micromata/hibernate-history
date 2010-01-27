/////////////////////////////////////////////////////////////////////////////
//
// $RCSfile: PropertyDelta.java,v $
//
// Project   BaseApp
//
// Author    Wolfgang Jung (w.jung@micromata.de)
// Created   Mar 7, 2005
//
// $Id: PropertyDelta.java,v 1.1 2007/03/08 22:50:48 wolle Exp $
// $Revision: 1.1 $
// $Date: 2007/03/08 22:50:48 $
//
/////////////////////////////////////////////////////////////////////////////
package de.micromata.hibernate.history.delta;

import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;
import org.hibernate.metadata.ClassMetadata;

/**
 * @author Wolfgang Jung (w.jung@micromata.de)
 * 
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "clazz", discriminatorType = DiscriminatorType.STRING)
@Table(name = "T_HISTORY_ENTRY")
@org.hibernate.annotations.Table(appliesTo = "T_HISTORY_ENTRY", indexes = { @Index(name = "IDX_REF_HIST_ENTITY", columnNames = { "HISTORY_ID_FK"})})
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public abstract class PropertyDelta
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PropertyDelta.class);
  
  private Integer id;

  protected String propertyName;

  protected String propertyType;

  protected String oldValue;

  protected String newValue;

  @Id
  @GeneratedValue
  @Column(name = "HISTORY_ENTRY_ID")
  public final Integer getId()
  {
    return id;
  }

  public final void setId(Integer id)
  {
    this.id = id;
  }

  /**
   * @return Returns the newValue.
   */
  @Column(name = "NEW_VALUE", length = 4000)
  public String getNewValue()
  {
    return newValue;
  }

  /**
   * @return Returns the oldValue.
   */
  @Column(name = "OLD_VALUE", length = 4000)
  public String getOldValue()
  {
    return oldValue;
  }

  /**
   * @return Returns the propertyName.
   */
  @Column(name = "PROPERTY_NAME", length = 64)
  public String getPropertyName()
  {
    return propertyName;
  }

  /**
   * @return Returns the propertyType.
   */
  @Column(name = "PROPERTY_TYPE", length = 128)
  public String getPropertyType()
  {
    return propertyType;
  }

  /**
   * @param newValue The newValue to set.
   */
  public void setNewValue(String newValue)
  {
    this.newValue = newValue;
  }

  /**
   * @param oldValue The oldValue to set.
   */
  public void setOldValue(String oldValue)
  {
    this.oldValue = oldValue;
  }

  /**
   * @param propertyName The propertyName to set.
   */
  public void setPropertyName(String propertyName)
  {
    this.propertyName = propertyName;
  }

  /**
   * @param propertyType The propertyType to set.
   */
  public void setPropertyType(String propertyType)
  {
    this.propertyType = propertyType;
  }

  @SuppressWarnings("unchecked")
  protected Object loadItem(String type, String id, Session session)
  {
    if (StringUtils.isBlank(id)) {
      return null;
    }
    try {
      Query query = session.createQuery("select o from " + type + " o where o.id = :pk");
      SessionFactory factory = session.getSessionFactory();
      Map<String, ClassMetadata> map = factory.getAllClassMetadata();
      ClassMetadata meta = null;
      for (String entry : map.keySet()) {
        if (entry.endsWith(type) == true) {
          meta = factory.getClassMetadata(entry);
          break;
        }
      }
      if (meta == null) {
        log.warn("Oups, no metadata found for entity: " + type);
        return null;
      }
      Class< ? > pkType = meta.getIdentifierType().getReturnedClass();
      if (ClassUtils.isAssignable(pkType, Number.class) == true) {
        if (pkType == Integer.class) {
          Integer pk = Integer.parseInt(id);
          query.setInteger("pk", pk);
        } else {
          Long pk = Long.parseLong(id);
          query.setLong("pk", pk);
        }
      } else {
        query.setString("pk", id);
      }
      query.setCacheable(true);
      query.setMaxResults(1);
      List< ? > list = query.list();
      if (list.size() > 0) {
        return list.get(0);
      }
      return null;
    } catch (HibernateException ex) {
      return null;
    }
  }

  @Transient
  public abstract Object getOldObjectValue(Session session) throws HibernateException;

  @Transient
  public abstract Object getNewObjectValue(Session session) throws HibernateException;

  public boolean anyChangeDetected()
  {
    return !StringUtils.equals(getOldValue(), getNewValue());
  }

}