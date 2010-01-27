package de.micromata.hibernate.spring;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class XStreamSavingConverter implements Converter
{
  /** The logger */
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(XStreamSavingConverter.class);

  // Objekte dürfen nur einmal geschrieben werden, daher merken, was bereits gespeichert wurde
  private final Set<Object> wroteItems;

  private final ConverterLookup defaultConv;

  private final Map<String, Object> persistentClasses;

  private final Session session;

  @SuppressWarnings("unchecked")
  public XStreamSavingConverter(Session session) throws HibernateException
  {
    defaultConv = new XStream().getConverterLookup();
    wroteItems = new HashSet<Object>();
    this.session = session;
    persistentClasses = session.getSessionFactory().getAllClassMetadata();
  }

  @SuppressWarnings("unchecked")
  public boolean canConvert(Class arg0)
  {
    return true;
  }

  public void marshal(Object arg0, HierarchicalStreamWriter arg1, MarshallingContext arg2)
  {
    defaultConv.lookupConverterForType(arg0.getClass()).marshal(arg0, arg1, arg2);
  }

  public Object unmarshal(HierarchicalStreamReader arg0, UnmarshallingContext arg1)
  {
    Object result;
    Class<?> targetType = null;
    try {
      targetType = arg1.getRequiredType();
      result = defaultConv.lookupConverterForType(targetType).unmarshal(arg0, arg1);
    } catch (Exception ex) {
      log.warn("Ignore unknown class or property " + targetType + " " + ex.getMessage());
      return null;
    }
    try {
      // Persistente Klasse?
      if (result != null && persistentClasses.get(result.getClass().getName()) != null) {
        // Bereits geschrieben?
        if (wroteItems.contains(result) == true) {
          return result;
        }
        if (log.isDebugEnabled()) {
          log.debug("Try to write object " + result);
        }
        Serializable id = session.save(result);
        wroteItems.add(result);
        log.debug("wrote object " + result + " under id " + id);
      }
    } catch (HibernateException ex) {
      log.fatal("Failed to write " + result + " ex=" + ex, ex);
    } catch (NullPointerException ex) {
      log.fatal("Failed to write " + result + " ex=" + ex, ex);
    }
    return result;
  }
}