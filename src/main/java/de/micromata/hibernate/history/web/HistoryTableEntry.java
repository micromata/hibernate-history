/////////////////////////////////////////////////////////////////////////////
//
// $RCSfile: HistoryTableEntry.java,v $
//
// Project   HibernateHistory
//
// Author    Wolfgang Jung (w.jung@micromata.de)
// Created   Sep 21, 2005
// Copyright Micromata Sep 21, 2005
//
// $Id: HistoryTableEntry.java,v 1.1 2007/03/08 22:50:48 wolle Exp $
// $Revision: 1.1 $
// $Date: 2007/03/08 22:50:48 $
//
/////////////////////////////////////////////////////////////////////////////
package de.micromata.hibernate.history.web;

import java.util.Date;

public class HistoryTableEntry
{
  private String user;

  private String timestamp;

  private String property;

  private String oldValue;

  private String newValue;

  private String action;

  private Date date;

  /**
   * @return Returns the date.
   */
  public Date getDate()
  {
    return new Date(date.getTime());
  }

  /**
   * @param date The date to set.
   */
  void setDate(Date date)
  {
    this.date = new Date(date.getTime());
  }

  /**
   * @return Returns the action.
   */
  public String getAction()
  {
    return action == null ? "" : action;
  }

  /**
   * @param action The action to set.
   */
  void setAction(String action)
  {
    this.action = action;
  }

  /**
   * @return Returns the newValue.
   */
  public String getNewValue()
  {
    return newValue == null ? "" : newValue;
  }

  /**
   * @return Returns the oldValue.
   */
  public String getOldValue()
  {
    return oldValue == null ? "" : oldValue;
  }

  /**
   * @return Returns the property.
   */
  public String getProperty()
  {
    return property == null ? "" : property;
  }

  /**
   * @return Returns the timestamp.
   */
  public String getTimestamp()
  {
    return timestamp == null ? "" : timestamp;
  }

  /**
   * @return Returns the user.
   */
  public String getUser()
  {
    return user == null ? "" : user;
  }

  /**
   * @param newValue The newValue to set.
   */
  void setNewValue(String newValue)
  {
    this.newValue = newValue;
  }

  /**
   * @param oldValue The oldValue to set.
   */
  void setOldValue(String oldValue)
  {
    this.oldValue = oldValue;
  }

  /**
   * @param property The property to set.
   */
  void setProperty(String property)
  {
    this.property = property;
  }

  /**
   * @param timestamp The timestamp to set.
   */
  void setTimestamp(String timestamp)
  {
    this.timestamp = timestamp;
  }

  /**
   * @param user The user to set.
   */
  void setUser(String user)
  {
    this.user = user;
  }
}
