/////////////////////////////////////////////////////////////////////////////
//
// $RCSfile: GroupDO.java,v $
//
// Project   hib3test
//
// Author    Wolfgang Jung (w.jung@micromata.de)
// Created   Dec 16, 2005
// Copyright Micromata Dec 16, 2005
//
// $Id: GroupDO.java,v 1.1 2007/03/08 22:50:50 wolle Exp $
// $Revision: 1.1 $
// $Date: 2007/03/08 22:50:50 $
//
/////////////////////////////////////////////////////////////////////////////
package de.micromata.hib3.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import de.micromata.hibernate.history.Historizable;

@Entity
@Table(name = "T_GROUP")
public class GroupDO implements Historizable
{

  private Integer id;

  private String name;

  @Id
  @GeneratedValue
  @Column(name = "group_id_pk")
  public Integer getId()
  {
    return id;
  }

  public void setId(Integer id)
  {
    this.id = id;
  }

  @Basic
  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

}
