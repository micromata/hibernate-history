/////////////////////////////////////////////////////////////////////////////
//
// $RCSfile: HistoryEntry.java,v $
//
// Project   BaseApp
//
// Author    Wolfgang Jung (w.jung@micromata.de)
// Created   Mar 8, 2005
//
// $Id: HistoryEntry.java,v 1.2 2007-07-19 13:44:15 wolle Exp $
// $Revision: 1.2 $
// $Date: 2007-07-19 13:44:15 $
//
/////////////////////////////////////////////////////////////////////////////
package de.micromata.hibernate.history;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;

import de.micromata.hibernate.history.delta.DeltaSet;
import de.micromata.hibernate.history.delta.PropertyDelta;

/**
 * @author Wolfgang Jung (w.jung@micromata.de)
 */
@Entity
@Table(name = "T_HISTORY_ENTRY")
@org.hibernate.annotations.Table(appliesTo = "T_HISTORY_ENTRY", indexes = { @Index(name = "IDX_HIST_ENTITY", columnNames = { "MODIFIED_ID",
    "CLASSNAME"})})
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class HistoryEntry
{
  private Integer id;

  private String userName;

  private Timestamp timestamp;

  private Integer entityId;

  private String className;

  private List<PropertyDelta> deltaSet;

  private HistoryEntryType type;

  private String comment;

  @Override
  public boolean equals(Object obj)
  {
    if (obj instanceof HistoryEntry) {
      HistoryEntry other = (HistoryEntry) obj;
      EqualsBuilder eq = new EqualsBuilder();
      eq.append(getTimestamp(), other.getTimestamp());
      eq.append(getEntityId(), other.getEntityId());
      eq.append(getClassName(), other.getClassName());
      eq.append(getUserName(), other.getUserName());
      eq.append(getType(), other.getType());
      return eq.isEquals();
    }
    return false;
  }

  @Override
  public int hashCode()
  {
    HashCodeBuilder hc = new HashCodeBuilder();
    hc.append(getTimestamp());
    hc.append(getEntityId());
    hc.append(getClassName());
    hc.append(getUserName());
    hc.append(getClassName());
    hc.append(getType());
    return hc.toHashCode();
  }

  @Id
  @GeneratedValue
  @Column(name = "ID")
  public Integer getId()
  {
    return id;
  }

  @Column(name = "CLASSNAME", length = 64)
  public String getClassName()
  {
    return className;
  }

  @Column(name = "MODIFIED_ID")
  public Integer getEntityId()
  {
    return entityId;
  }

  @Column(name = "TIMESTAMP")
  public Timestamp getTimestamp()
  {
    return timestamp;
  }

  @Column(name = "USERNAME")
  public String getUserName()
  {
    return userName;
  }

  @Column(name = "TYPE")
  public HistoryEntryType getType()
  {
    return type;
  }

  @Column(name = "USER_COMMENT", length = 2000)
  public String getComment()
  {
    return comment;
  }

  public void setId(Integer id)
  {
    this.id = id;
  }

  public void setClassName(String className)
  {
    this.className = className;
  }

  void setDelta(List<PropertyDelta> delta)
  {
    this.deltaSet = delta;
  }

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinColumn(name = "HISTORY_ID_FK")
  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
  public List<PropertyDelta> getDelta()
  {
    return deltaSet;
  }

  void setDeltaSet(DeltaSet deltaSet)
  {
    if (this.deltaSet != null) {
      this.deltaSet.addAll(deltaSet.getDeltas());
    } else {
      this.deltaSet = deltaSet.getDeltas();
    }
  }

  void setEntityId(Integer entityId)
  {
    this.entityId = entityId;
  }

  void setTimestamp(Timestamp timestamp)
  {
    this.timestamp = timestamp;
  }

  void setUserName(String userName)
  {
    this.userName = userName;
  }

  void setType(HistoryEntryType type)
  {
    this.type = type;
  }

  @Override
  public String toString()
  {
    return ToStringBuilder.reflectionToString(this);
  }

  public void setComment(String string)
  {
    this.comment = string;
  }
}
