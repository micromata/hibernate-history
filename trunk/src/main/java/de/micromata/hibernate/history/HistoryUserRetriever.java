/////////////////////////////////////////////////////////////////////////////
//
// $RCSfile: HistoryUserRetriever.java,v $
//
// Project   HibernateHistory
//
// Author    Wolfgang Jung (w.jung@micromata.de)
// Created   Mar 18, 2005
//
// $Id: HistoryUserRetriever.java,v 1.1 2007/03/08 22:50:47 wolle Exp $
// $Revision: 1.1 $
// $Date: 2007/03/08 22:50:47 $
//
/////////////////////////////////////////////////////////////////////////////
package de.micromata.hibernate.history;

/**
 * @author Wolfgang Jung (w.jung@micromata.de)
 *
 */
public interface HistoryUserRetriever {
  public String getPrincipal();
}
