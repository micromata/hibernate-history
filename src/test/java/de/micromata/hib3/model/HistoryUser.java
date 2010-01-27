/////////////////////////////////////////////////////////////////////////////
//
// $RCSfile: HistoryUser.java,v $
//
// Project   hib3test
//
// Author    Wolfgang Jung (w.jung@micromata.de)
// Created   Dec 16, 2005
// Copyright Micromata Dec 16, 2005
//
// $Id: HistoryUser.java,v 1.1 2007/03/08 22:50:50 wolle Exp $
// $Revision: 1.1 $
// $Date: 2007/03/08 22:50:50 $
//
/////////////////////////////////////////////////////////////////////////////
package de.micromata.hib3.model;

import de.micromata.hibernate.history.HistoryUserRetriever;

public class HistoryUser implements HistoryUserRetriever
{
  public String getPrincipal()
  {
    return "local-User";
  }
}
