/////////////////////////////////////////////////////////////////////////////
//
// $RCSfile: CGLibClassMapper.java,v $
//
// Project   Hibernate3History
//
// Author    Wolfgang Jung (w.jung@micromata.de)
// Created   Jan 8, 2006
// Copyright Micromata Jan 8, 2006
//
// $Id: CGLibClassMapper.java,v 1.2 2007-07-19 13:43:24 wolle Exp $
// $Revision: 1.2 $
// $Date: 2007-07-19 13:43:24 $
//
/////////////////////////////////////////////////////////////////////////////
package de.micromata.hibernate.spring;

import net.sf.cglib.proxy.Enhancer;

import com.thoughtworks.xstream.alias.ClassMapper;
import com.thoughtworks.xstream.mapper.MapperWrapper;

/**
 * Mapper that removes the annoying CGLib signature which generates an unsuable XML (the classes mentioned in there do not exist).
 * 
 * <br>
 * <strong>NOTE</strong> This mapper takes care only of the writing to the XML (deflating) not the other way around (inflating) because
 * there is no need.
 * 
 * @author Costin Leau
 * 
 */
public class CGLibClassMapper extends MapperWrapper
{
  public static final String marker = new String("EnhancerByCGLIB");

  /** The logger */
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CGLibClassMapper.class);

  public CGLibClassMapper(ClassMapper wrapped)
  {
    super(wrapped);
  }

  private Class<?> getBaseClass(Class<?> clazz)
  {
    while (Enhancer.isEnhanced(clazz)) {
      clazz = clazz.getSuperclass();
    }
    return clazz;
  }

  /**
   * @see com.thoughtworks.xstream.mapper.Mapper#serializedClass(java.lang.Class)
   */
  @SuppressWarnings("unchecked")
  @Override
  public String serializedClass(Class type)
  {
    return super.serializedClass(getBaseClass(type));
  }

  @Override
  public String mapNameToXML(String javaName)
  {
    return removeSignature(javaName);
  }

  @SuppressWarnings("unchecked")
  @Override
  public String realMember(Class type, String serialized)
  {
    return super.realMember(getBaseClass(type), removeSignature(serialized));
  }

  /**
   * @see com.thoughtworks.xstream.mapper.Mapper#serializedMember(java.lang.Class, java.lang.String)
   */
  @SuppressWarnings("unchecked")
  @Override
  public String serializedMember(Class type, String memberName)
  {
    return super.serializedMember(getBaseClass(type), removeSignature(memberName));
  }

  private String removeSignature(String name)
  {
    int count = name.indexOf(marker);
    if (count >= 0) {
      count -= 2;
      String substring = name.substring(0, count);
      log.debug("remove " + name + " to " + substring);
      return substring;
    }
    return name;
  }
}