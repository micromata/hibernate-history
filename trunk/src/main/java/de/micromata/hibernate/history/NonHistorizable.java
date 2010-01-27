/////////////////////////////////////////////////////////////////////////////
//
//$RCSfile: Historizable.java,v $
//
//Project   BaseApp
//
//Author    Wolfgang Jung (w.jung@micromata.de)
//Created   Mar 9, 2005
//
//$Id: Historizable.java,v 1.1 2007/03/08 22:50:47 wolle Exp $
//$Revision: 1.1 $
//$Date: 2007/03/08 22:50:47 $
//
/////////////////////////////////////////////////////////////////////////////
package de.micromata.hibernate.history;

/**
 * Marker interface of entities to non reord the changes from.
 *
 * @author Wolfgang Jung (w.jung@micromata.de)
 * @see de.micromata.hibernate.history.HistoryInterceptor
 */

public interface NonHistorizable {
  // only marker
}
