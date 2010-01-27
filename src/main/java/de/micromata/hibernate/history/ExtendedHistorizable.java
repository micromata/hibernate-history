/////////////////////////////////////////////////////////////////////////////
//
// Project   HibernateHistory
//
// Author    K.Reinhard@micromata.com
//
/////////////////////////////////////////////////////////////////////////////



package de.micromata.hibernate.history;

import java.util.Set;

import de.micromata.hibernate.history.Historizable;

/**
 * Interface to provide the names of valid and invalid properties to record the updates from.
 * 
 * @author K.Reinhard@micromata.com
 * @see de.micromata.hibernate.history.HistoryInterceptor
 * @see de.micromata.hibernate.history.Historizable
 */
public interface ExtendedHistorizable extends Historizable {
  
  /**
   * Get the valid properties.
   * @return the valid properties or null, if at default all properties should be historizable.
   */
  public Set<String> getHistorizableAttributes();

  /**
   * Gets the invalid properties.
   * @return the valid properties or null, if no property should be excluded in negative list.
   */
  public Set<String> getNonHistorizableAttributes();

}
