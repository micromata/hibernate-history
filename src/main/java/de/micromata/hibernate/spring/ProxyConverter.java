/////////////////////////////////////////////////////////////////////////////
//
// $RCSfile: ProxyConverter.java,v $
//
// Project   Hibernate3History
//
// Author    Wolfgang Jung (w.jung@micromata.de)
// Created   Jan 16, 2006
// Copyright Micromata Jan 16, 2006
//
// $Id: ProxyConverter.java,v 1.1 2007/03/08 22:50:48 wolle Exp $
// $Revision: 1.1 $
// $Date: 2007/03/08 22:50:48 $
//
/////////////////////////////////////////////////////////////////////////////
package de.micromata.hibernate.spring;

import net.sf.cglib.proxy.Enhancer;

import org.hibernate.EntityMode;
import org.hibernate.Session;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.proxy.HibernateProxy;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import de.micromata.hibernate.dao.HibernateProxyHelper;

class ProxyConverter implements Converter
{
  /** The logger */
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ProxyConverter.class);
  
  private final XStream defaultXStream;

  private final Session session;

  private final boolean nullifyPk;

  public ProxyConverter(XStream defaultXStream, Session session, boolean nullifyPk)
  {
    this.defaultXStream = defaultXStream;
    this.session = session;
    this.nullifyPk = nullifyPk;
  }

  @SuppressWarnings("unchecked")
  public boolean canConvert(Class arg0)
  {
    log.debug("checking " + arg0 + " against HibernateProxy");
    return HibernateProxy.class.isAssignableFrom(arg0);
  }

  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context)
  {
    try {
      Object obj = HibernateProxyHelper.get(source);
      if (log.isDebugEnabled())
        log.debug("unpack proxy " + source.getClass() + " to " + obj.getClass());
      final Converter converter = defaultXStream.getConverterLookup().lookupConverterForType(obj.getClass());

      if (nullifyPk == true) {
        Class<?> targetClass = obj.getClass();
        while (Enhancer.isEnhanced(targetClass) == true) {
          targetClass = targetClass.getSuperclass();
        }
        ClassMetadata classMetadata = session.getSessionFactory().getClassMetadata(targetClass);
        if (classMetadata == null) {
          log.fatal("Can't init " + obj + " of type " + targetClass);
        } else {
          if (log.isDebugEnabled())
            log.debug("marshalling object " + obj + " to stream");
          // session.evict(obj);
          classMetadata.setIdentifier(source, null, EntityMode.POJO);
        }
      }
      converter.marshal(obj, writer, context);
    } catch (RuntimeException e) {
      // for debugging purposes ...
      throw e;
    }
  }

  public Object unmarshal(HierarchicalStreamReader arg0, UnmarshallingContext arg1)
  {
    // no unmarshalling
    return null;
  }
}