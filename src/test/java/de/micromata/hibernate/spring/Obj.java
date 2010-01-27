/////////////////////////////////////////////////////////////////////////////
//
// $RCSfile: Obj.java,v $
//
// Project   Hibernate3History
//
// Author    Wolfgang Jung (w.jung@micromata.de)
// Created   Jan 8, 2006
// Copyright Micromata Jan 8, 2006
//
// $Id: Obj.java,v 1.1 2007/03/08 22:50:50 wolle Exp $
// $Revision: 1.1 $
// $Date: 2007/03/08 22:50:50 $
//
/////////////////////////////////////////////////////////////////////////////
package de.micromata.hibernate.spring;

/**
 * Dummy object used for CGLib generation.
 * @author Costin Leau
 * 
 */
public class Obj
{

  String name;

  public Obj()
  {
  }

  /**
   * @param name
   */
  public Obj(String name)
  {
    this.name = name;
  }

  /**
   * @return Returns the name.
   */
  public String getName()
  {
    return name;
  }

  /**
   * @param name The name to set.
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj)
  {
    if (!(obj instanceof Obj))
      return false;

    return name.equals(obj);
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode()
  {
    return name.hashCode();
  }
}
