/////////////////////////////////////////////////////////////////////////////
//
// $RCSfile: EntityChange.java,v $
//
// Project   BaseApp
//
// Author    Wolfgang Jung (w.jung@micromata.de)
// Created   Mar 8, 2005
//
// $Id: EntityChange.java,v 1.1 2007/03/08 22:50:47 wolle Exp $
// $Revision: 1.1 $
// $Date: 2007/03/08 22:50:47 $
//
/////////////////////////////////////////////////////////////////////////////
package de.micromata.hibernate.history;

import java.io.Serializable;

/**
 * @author Wolfgang Jung (w.jung@micromata.de)
 * 
 */
public class EntityChange implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 6078645519659358595L;

  private Serializable id;

  private Object entity;

  EntityChange(Serializable id, Object entity) {
    this.id = id;
    this.entity = entity;
  }
  
  public Object getEntity() {
    return entity;
  }

  public Serializable getId() {
    return id;
  }

  void setId(Serializable id) {
    this.id = id;
  }
}
