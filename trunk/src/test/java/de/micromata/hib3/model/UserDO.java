/////////////////////////////////////////////////////////////////////////////
//
// $RCSfile: UserDO.java,v $
//
// Project   hib3test
//
// Author    Wolfgang Jung (w.jung@micromata.de)
// Created   Dec 16, 2005
// Copyright Micromata Dec 16, 2005
//
// $Id: UserDO.java,v 1.1 2007/03/08 22:50:50 wolle Exp $
// $Revision: 1.1 $
// $Date: 2007/03/08 22:50:50 $
//
/////////////////////////////////////////////////////////////////////////////
package de.micromata.hib3.model;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import de.micromata.hibernate.history.Historizable;

/**
 * @author Wolfgang Jung (w.jung@micromata.de)
 *
 */
@Entity
@Table(name = "T_USER")
public class UserDO implements Historizable
{

  private Integer id;

  private String name;

  private Set<GroupDO> groups;

  private GroupDO mainGroup;

  private Timestamp timestamp;

  private Date date;

  private java.util.Date juDate;

  /**
   * @return Set<GroupDO>
   */
  @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
  @JoinTable(name = "T_USER2GROUP", joinColumns = { @JoinColumn(name = "USER_ID_FK")}, inverseJoinColumns = { @JoinColumn(name = "GROUP_ID_FK")})
  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
  public Set<GroupDO> getGroups()
  {
    return groups;
  }

  public void setGroups(Set<GroupDO> groups)
  {
    this.groups = groups;
  }

  @Id
  @GeneratedValue
  @Column(name = "pk")
  public Integer getId()
  {
    return id;
  }

  public void setId(Integer id)
  {
    this.id = id;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * @return Returns the mainGroup.
   */
  @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
  public GroupDO getMainGroup()
  {
    return mainGroup;
  }

  /**
   * @param mainGroup The mainGroup to set.
   */
  public void setMainGroup(GroupDO mainGroup)
  {
    this.mainGroup = mainGroup;
  }

  public Timestamp getTimestamp()
  {
    return timestamp;
  }

  public void setTimestamp(Timestamp timestamp)
  {
    this.timestamp = timestamp;
  }

  public Date getDate()
  {
    return date;
  }

  public void setDate(Date date)
  {
    this.date = date;
  }

  public java.util.Date getJuDate()
  {
    return juDate;
  }

  public void setJuDate(java.util.Date juDate)
  {
    this.juDate = juDate;
  }


}
