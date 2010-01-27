/////////////////////////////////////////////////////////////////////////////
//
// $RCSfile: CollectionConverter.java,v $
//
// Project   Hibernate3History
//
// Author    Wolfgang Jung (w.jung@micromata.de)
// Created   Jan 16, 2006
// Copyright Micromata Jan 16, 2006
//
// $Id: CollectionConverter.java,v 1.1 2007/03/08 22:50:48 wolle Exp $
// $Revision: 1.1 $
// $Date: 2007/03/08 22:50:48 $
//
/////////////////////////////////////////////////////////////////////////////
package de.micromata.hibernate.spring;

import java.util.Map;

import org.hibernate.collection.PersistentCollection;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

class CollectionConverter implements Converter
{
  /** The logger */
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CollectionConverter.class);
  
  private final Map<Class<?>, Class<?>> hibernateClassMappings;

  private final XStream defaultXStream;

  public CollectionConverter(XStream defaultXStream, Map<Class<?>, Class<?>> hibernateClassMappings)
  {
    this.defaultXStream = defaultXStream;
    this.hibernateClassMappings = hibernateClassMappings;
  }

  @SuppressWarnings("unchecked")
  public boolean canConvert(Class type)
  {
    log.debug("checking " + type + " against PersistentCollection");
    return PersistentCollection.class.isAssignableFrom(type);
  }

  public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context)
  {
    try {
      Class<?> clazz = hibernateClassMappings.get(source.getClass());
      if (log.isDebugEnabled())
        log.debug("Using " + clazz + " for " + source.getClass());
      final Converter converter = defaultXStream.getConverterLookup().lookupConverterForType(clazz);
      if (source instanceof PersistentCollection) {
        PersistentCollection set = (PersistentCollection) source;
        set.forceInitialization();
      }
      converter.marshal(source, writer, context);
    } catch (RuntimeException e) {
      // for debugging purposes ...
      throw e;
    }
  }

  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
  {
    // no unmarshalling
    return null;
  }
}