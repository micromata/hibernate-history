/////////////////////////////////////////////////////////////////////////////
//
// $RCSfile: ExpliciteHistorizable.java,v $
//
// Project   eon-db
//
// Author    H.Spiewok@micromata.com
// Created   Apr 13, 2005 2:58:16 PM
//
// $Id: ExpliciteHistorizable.java,v 1.2 2007/04/04 08:45:24 wolle Exp $
// $Revision: 1.2 $
// $Date: 2007/04/04 08:45:24 $
//
/////////////////////////////////////////////////////////////////////////////



package de.micromata.hibernate.history;

import java.util.Set;

import de.micromata.hibernate.history.Historizable;

/**
 * Interface to provide the names of properties to record the updates from.
 * 
 * @author H.Spiewok@micromata.com
 * @see de.micromata.hibernate.history.HistoryInterceptor
 * @see de.micromata.hibernate.history.Historizable
 */
public interface ExpliciteHistorizable extends Historizable {
  
  /**
   * Get the valid properties.
   * @return the valid properties.
   */
  public Set<String> getHistorizableAttributes();

}
