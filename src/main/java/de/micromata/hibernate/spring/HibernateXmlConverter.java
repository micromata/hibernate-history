/////////////////////////////////////////////////////////////////////////////
//
// $RCSfile: HibernateXmlConverter.java,v $
//
// Project   MehrwertOffensive
//
// Author    Wolfgang Jung (w.jung@micromata.de)
// Created   Sep 9, 2005
// Copyright Micromata Sep 9, 2005
//
// $Id: HibernateXmlConverter.java,v 1.2 2007/03/20 00:43:55 kai Exp $
// $Revision: 1.2 $
// $Date: 2007/03/20 00:43:55 $
//
/////////////////////////////////////////////////////////////////////////////
package de.micromata.hibernate.spring;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import net.sf.cglib.proxy.Enhancer;

import org.hibernate.EmptyInterceptor;
import org.hibernate.EntityMode;
import org.hibernate.FlushMode;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.collection.PersistentList;
import org.hibernate.collection.PersistentMap;
import org.hibernate.collection.PersistentSet;
import org.hibernate.collection.PersistentSortedMap;
import org.hibernate.collection.PersistentSortedSet;
import org.hibernate.metadata.ClassMetadata;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.thoughtworks.xstream.MarshallingStrategy;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.io.xml.CompactWriter;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.mapper.MapperWrapper;

import de.micromata.hibernate.history.HistoryEntry;
import de.micromata.hibernate.history.delta.PropertyDelta;

/**
 * Hilfsklasse zum Laden und Speichern einer gesamten Hibernate-Datenbank im XML-Format. Zur Darstellung der Daten in XML wird XStream zur
 * Serialisierung eingesetzt. Alle Lazy-Objekte aus Hibernate werden vollständig initialisiert.
 * @author Wolfgang Jung (w.jung@micromata.de)
 * 
 */
public class HibernateXmlConverter
{
  /** The logger */
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(HibernateXmlConverter.class);

  /** the wrapper to hibernate */
  private HibernateTemplate hibernate;

  /**
   * Initialisierung der Hibernate-verbindung.
   * 
   * @param hibernate ein bereits initialisiertes HibernateTemplate
   */
  public void setHibernate(HibernateTemplate hibernate)
  {
    this.hibernate = new HibernateTemplate(hibernate.getSessionFactory());
    this.hibernate.setAlwaysUseNewSession(false);
    this.hibernate.setExposeNativeSession(true);
  }

  /**
   * Schreibt alle Objekte der Datenbank in den angegebenen Writer.<br/> <b>Warnung!</b> Bei der Serialisierung von Collections wird
   * derzeit nur {@link java.util.Set} sauber unterstützt.
   * @param writer Ziel für die XML-Datei.
   * @param includeHistory bei false werden die History Einträge nicht geschrieben
   */
  public void dumpDatabaseToXml(final Writer writer, final boolean includeHistory)
  {
    dumpDatabaseToXml(writer, includeHistory, false);
  }

  /**
   * Schreibt alle Objekte der Datenbank in den angegebenen Writer.<br/> <b>Warnung!</b> Bei der Serialisierung von Collections wird
   * derzeit nur {@link java.util.Set} sauber unterstützt.
   * @param writer Ziel für die XML-Datei.
   * @param includeHistory bei false werden die History Einträge nicht geschrieben
   * @param preserveIds If true, the object ids will be preserved, otherwise new ids will be assigned through xstream.
   */
  public void dumpDatabaseToXml(final Writer writer, final boolean includeHistory, final boolean preserveIds)
  {
    TransactionTemplate tx = new TransactionTemplate(new HibernateTransactionManager(hibernate.getSessionFactory()));
    tx.execute(new TransactionCallback() {
      public Object doInTransaction(final TransactionStatus status)
      {
        hibernate.execute(new HibernateCallback() {
          public Object doInHibernate(Session session) throws HibernateException
          {
            writeObjects(writer, includeHistory, session, preserveIds);
            status.setRollbackOnly();
            return null;
          }
        });
        return null;
      }
    });
  }

  /**
   * Füllt die Datenbank mit den in der XML-Datei angegebenen Objekte. Alle Objekte werden dabei mittels
   * {@link net.sf.hibernate.Session#save(java.lang.Object)} gespeichert, so dass die Datenbank leer sein sollte.
   * @param reader Reader auf eine XML-Datei
   */
  public void fillDatabaseFromXml(final Reader reader)
  {
    TransactionTemplate tx = new TransactionTemplate(new HibernateTransactionManager(hibernate.getSessionFactory()));
    tx.execute(new TransactionCallback() {
      public Object doInTransaction(final TransactionStatus status)
      {
        SessionFactory sessionFactory = hibernate.getSessionFactory();
        try {
          Session session = sessionFactory.openSession(EmptyInterceptor.INSTANCE);
          session.setFlushMode(FlushMode.AUTO);
          insertObjectsFromStream(reader, session);
          session.flush();
          session.connection().commit();
        } catch (HibernateException ex) {
          log.warn("Failed to load db " + ex, ex);
        } catch (SQLException ex) {
          log.warn("Failed to load db " + ex, ex);
        }
        return null;
      }
    });
  }

  /**
   * @param reader
   * @param session
   * @throws HibernateException
   */
  private void insertObjectsFromStream(final Reader reader, final Session session) throws HibernateException
  {
    log.debug("Loading DB from stream");
    final XStream stream = new XStream(new DomDriver());
    stream.setMode(XStream.ID_REFERENCES);
    Converter save = new XStreamSavingConverter(session);
    stream.registerConverter(save, 10);

    // alle Objekte Laden und speichern
    stream.fromXML(reader);
  }

  /**
   * @param writer
   * @param includeHistory
   * @param session
   * @throws DataAccessException
   * @throws HibernateException
   */
  private void writeObjects(final Writer writer, final boolean includeHistory, Session session, boolean preserveIds)
      throws DataAccessException, HibernateException
  {
    // Container für die Objekte
    List<Object> all = new ArrayList<Object>();
    final XStream stream = initXStream(session, true);
    final XStream defaultXStream = initXStream(session, false);

    session.flush();
    // Alles laden
    List<?> list = session.createQuery("select o from java.lang.Object o").setReadOnly(true).list();
    for (Iterator<?> it = list.iterator(); it.hasNext();) {
      Object obj = it.next();
      if (log.isDebugEnabled()) {
        log.debug("loaded object " + obj);
      }
      if ((obj instanceof HistoryEntry || obj instanceof PropertyDelta) && includeHistory == false) {
        continue;
      }
      Hibernate.initialize(obj);
      Class<?> targetClass = obj.getClass();
      while (Enhancer.isEnhanced(targetClass) == true) {
        targetClass = targetClass.getSuperclass();
      }
      ClassMetadata classMetadata = session.getSessionFactory().getClassMetadata(targetClass);
      if (classMetadata == null) {
        log.fatal("Can't init " + obj + " of type " + targetClass);
        continue;
      }
      // initalisierung des Objekts...
      defaultXStream.marshal(obj, new CompactWriter(new NullWriter()));

      if (preserveIds == false) {
        // Nun kann die ID gelöscht werden
        classMetadata.setIdentifier(obj, null, EntityMode.POJO);
      }
      if (log.isDebugEnabled()) {
        log.debug("loading evicted object " + obj);
      }
      all.add(obj);
    }
    // und schreiben
    try {
      writer.write("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n");
    } catch (IOException ex) {
      // ignore, will fail on stream.marshal()
    }
    log.info("Wrote " + all.size() + " objects");
    MarshallingStrategy marshallingStrategy = new ProxyIdRefMarshallingStrategy();
    stream.setMarshallingStrategy(marshallingStrategy);
    stream.marshal(all, new PrettyPrintWriter(writer));
  }

  /**
   * @return
   */
  private XStream initXStream(final Session session, boolean nullifyPk)
  {

    final XStream defaultXStream = new XStream() {
      @Override
      protected MapperWrapper wrapMapper(MapperWrapper next)
      {
        return new CGLibClassMapper(next);
      }
    };

    final XStream stream = new XStream() {
      @Override
      protected MapperWrapper wrapMapper(MapperWrapper next)
      {
        return new CGLibClassMapper(next);
      }
    };

    // Converter für die Hibernate-Collections

    Map<Class<?>, Class<?>> hibernateClassMappings = new HashMap<Class<?>, Class<?>>();
    hibernateClassMappings.put(PersistentSet.class, Set.class);
    hibernateClassMappings.put(PersistentMap.class, Map.class);
    hibernateClassMappings.put(PersistentList.class, List.class);
    // hibernateClassMappings.put(PersistentCollection.class, Collection.class);
    hibernateClassMappings.put(PersistentSortedMap.class, SortedMap.class);
    hibernateClassMappings.put(PersistentSortedSet.class, SortedSet.class);
    for (Map.Entry<Class<?>, Class<?>> entry : hibernateClassMappings.entrySet()) {
      stream.addDefaultImplementation(entry.getKey(), entry.getValue());
    }

    stream.registerConverter(new CollectionConverter(defaultXStream, hibernateClassMappings), 10);
    stream.registerConverter(new ProxyConverter(defaultXStream, session, nullifyPk), 10);

    return stream;
  }
}
