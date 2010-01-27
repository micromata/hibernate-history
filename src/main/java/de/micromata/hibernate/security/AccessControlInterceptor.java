/////////////////////////////////////////////////////////////////////////////
//
// $RCSfile: AccessControlInterceptor.java,v $
//
// Project   HibernateHistory
//
// Author    Wolfgang Jung (w.jung@micromata.de)
// Created   Jan 20, 2006
// Copyright Micromata Jan 20, 2006
//
// $Id: AccessControlInterceptor.java,v 1.1 2007/03/08 22:50:48 wolle Exp $
// $Revision: 1.1 $
// $Date: 2007/03/08 22:50:48 $
//
/////////////////////////////////////////////////////////////////////////////
package de.micromata.hibernate.security;

import java.io.Serializable;
import java.security.AccessControlException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.Validate;
import org.hibernate.CallbackException;
import org.hibernate.type.Type;
import org.springframework.beans.factory.InitializingBean;

import de.micromata.hibernate.spring.AbstractInterceptor;

/**
 * Interceptor for assuring that only certain roles may change the database. The interceptor can be chained before another Interceptor and
 * throws an AccessException if an unauthorized modification occurs. <br/> The Interceptor is controlled by a property file containig
 * 
 * @author Wolfgang Jung (w.jung@micromata.de)
 * 
 */
public class AccessControlInterceptor extends AbstractInterceptor implements InitializingBean
{
  /** The logger */
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AccessControlInterceptor.class);

  private Map<String, String[]> allowed_map = new HashMap<String, String[]>();

  /**
   * a simple cache for the roles. The keys in the maps are of the form FQCN.[S|U|D]
   * 
   */
  private Map<String, String[]> knownRights = new HashMap<String, String[]>();

  private AccessControlChecker checker;

  private static final String DELETE = "D";

  private static final String UPDATE = "U";

  private static final String SAVE = "S";

  public void afterPropertiesSet() throws Exception
  {
    Validate.notNull(checker, "An AccessControlChecker must be provided");
    Validate.notEmpty(allowed_map, "at least one right has to specified in setAllowed()");
  }

  /**
   * Set the packages/classes where an update/delete/save has to be checked against a role-implementation
   * @param allowed key/value properties of the form <table>
   *          <tr>
   *          <td>update.de.micromata.user.*=ROLE_1,ROLE_2,ROLE_3</td>
   *          owners of ROLE_1, ROLE_2 or ROLE_3 are allowed to update all objects in the de.micromata.user package
   *          <td></tr>
   *          <tr>
   *          <td>save.de.micromata.user.FooDO=ROLE_3</td>
   *          only owners of ROLE_3 are allowed to create new objects of the type the de.micromata.user.FooDO
   *          <td></tr>
   *          <tr>
   *          <td>delete.de.micromata.user.*=ROLE_2</td>
   *          only owners of ROLE_2 are allowed to delete existent entries in the package de.micromata.user
   *          <td></tr>
   *          </table>
   */
  public void setAllowed(Properties allowed)
  {
    for (Map.Entry<Object, Object> entry : allowed.entrySet()) {
      allowed_map.put((String) entry.getKey(), ((String) entry.getValue()).split(","));
    }
  }

  /**
   * Set the implementation of the AccessControl.
   * @param checker must not be null
   */
  public void setAccessControlChecker(AccessControlChecker checker)
  {
    Validate.notNull(checker);
    this.checker = checker;
  }

  private void checkRight(Class<?> clazz, String operation)
  {
    String lookupKey = clazz.getName() + "." + operation;
    String opName = null;
    if (operation.equals(UPDATE)) {
      opName = "update";
    } else if (operation.equals(SAVE)) {
      opName = "save";
    } else if (operation.equals(DELETE)) {
      opName = "delete";
    }
    if (log.isDebugEnabled())
      log.debug("Lookup configuration for " + clazz + " operation " + opName);
    String[] roles = knownRights.get(lookupKey);
    if (roles == null) {
      knownRights.put(lookupKey, lookup(opName, clazz));
      roles = knownRights.get(lookupKey);
    } else {
      if (log.isDebugEnabled()) {
        log.debug("Using cached configuration for " + clazz + " operation " + opName);
      }
    }
    try {
      checker.checkAccess(roles);
    } catch (AccessControlException ex) {
      throw new SecurityException(opName + " of " + clazz.getName() + " not permitted", ex);
    }
  }

  private String[] lookup(String prefix, Class<?> clazz)
  {
    String className = clazz.getName();
    boolean glob = false;
    while (className.length() > 0) {
      String searchKey = prefix + "." + className + (glob ? ".*" : "");
      String[] foundRoles = allowed_map.get(searchKey);
      if (log.isDebugEnabled())
        log.debug("roles found for " + searchKey + "=" + ArrayUtils.toString(foundRoles));
      if (foundRoles != null) { return foundRoles; }
      glob = true;
      className = ClassUtils.getPackageName(className);
    }
    String[] foundRoles = allowed_map.get(prefix + ".*");
    if (log.isDebugEnabled())
      log.debug("roles found for " + prefix + ".*=" + ArrayUtils.toString(foundRoles));
    return foundRoles != null ? foundRoles : new String[0];
  }

  @Override
  public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) throws CallbackException
  {
    checkRight(entity.getClass(), DELETE);
    super.onDelete(entity, id, state, propertyNames, types);
  }

  @Override
  public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames,
      Type[] types) throws CallbackException
  {
    checkRight(entity.getClass(), UPDATE);
    return super.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
  }

  @Override
  public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) throws CallbackException
  {
    checkRight(entity.getClass(), SAVE);
    return super.onSave(entity, id, state, propertyNames, types);
  }
}
