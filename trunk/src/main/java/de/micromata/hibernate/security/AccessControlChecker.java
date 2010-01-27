/////////////////////////////////////////////////////////////////////////////
//
// $RCSfile: AccessControlChecker.java,v $
//
// Project   HibernateHistory
//
// Author    Wolfgang Jung (w.jung@micromata.de)
// Created   Jan 20, 2006
// Copyright Micromata Jan 20, 2006
//
// $Id: AccessControlChecker.java,v 1.1 2007/03/08 22:50:48 wolle Exp $
// $Revision: 1.1 $
// $Date: 2007/03/08 22:50:48 $
//
/////////////////////////////////////////////////////////////////////////////
package de.micromata.hibernate.security;

import java.security.AccessControlException;

public interface AccessControlChecker
{
  public void checkAccess(String[] permittedRoles) throws AccessControlException;
}
