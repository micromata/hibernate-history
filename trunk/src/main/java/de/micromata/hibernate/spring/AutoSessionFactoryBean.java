/////////////////////////////////////////////////////////////////////////////
//
// $RCSfile: AutoSessionFactoryBean.java,v $
//
// Project   Hibernate3History
//
// Author    Wolfgang Jung (w.jung@micromata.de)
// Created   Dec 23, 2005
// Copyright Micromata Dec 23, 2005
//
// $Id: AutoSessionFactoryBean.java,v 1.2 2007/03/30 18:17:34 hx Exp $
// $Revision: 1.2 $
// $Date: 2007/03/30 18:17:34 $
//
/////////////////////////////////////////////////////////////////////////////
package de.micromata.hibernate.spring;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.springframework.core.io.Resource;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;

/**
 * @author Wolfgang Jung (w.jung@micromata.de)
 * 
 */
public class AutoSessionFactoryBean extends AnnotationSessionFactoryBean
{
  /** The logger */
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AutoSessionFactoryBean.class);

  public static final String PROPERTY_FILE_NAME = "hibernate-mappings.properties";

  public static final String PROPERTY_NAME = "hibernate.classes";

  private Resource initialXmlData;

  /**
   * Resource mit dem initialen Datenbestand
   * 
   * @param initialXmlData eine lesbare Resource
   */
  public void setInitialXmlData(Resource initialXmlData)
  {
    this.initialXmlData = initialXmlData;
  }

  private Set<Class<?>> classesToAdd = new HashSet<Class<?>>();

  private boolean schemaUpdate;

  @Override
  protected void postProcessAnnotationConfiguration(AnnotationConfiguration config) throws HibernateException
  {
    Enumeration<URL> resources = null;
    try {
      resources = getClass().getClassLoader().getResources(PROPERTY_FILE_NAME);
    } catch (IOException ex) {
      log.fatal("Exception encountered " + ex, ex);
      return;
    }
    while (resources.hasMoreElements()) {
      URL url = resources.nextElement();
      log.debug("appending " + url + " with this of annotated classes");
      Properties props = new Properties();
      try {
        InputStream openStream = url.openStream();
        props.load(openStream);
        openStream.close();
      } catch (IOException ex) {
        log.warn("Exception encountered " + ex + " while adding url " + url, ex);
        continue;
      }
      String strClasses = props.getProperty(PROPERTY_NAME, null);
      if (strClasses == null) {
        log.info("mapping file " + url + " does not contain a value under the key " + PROPERTY_NAME);
        continue;
      }
      String[] classNames = strClasses.split("[, ]+");
      addClassNames(config, classNames);
    }
    for (Class<?> newClass : classesToAdd) {
      log.debug("Adding class " + newClass.getName());
      config.addAnnotatedClass(newClass);
    }
    super.postProcessAnnotationConfiguration(config);
  }

  @Override
  public void setSchemaUpdate(boolean schemaUpdate)
  {
    super.setSchemaUpdate(schemaUpdate);
    this.schemaUpdate = schemaUpdate;
  }

  /**
   * Nach dem Update des Schema die Datenbank mit den in der XML-Datei angegebenen Objekten befüllt.
   * @see org.springframework.orm.hibernate3.LocalSessionFactoryBean#updateDatabaseSchema()
   */
  @Override
  public void afterPropertiesSet()
  {
    super.setSchemaUpdate(false);
    try {
      super.afterPropertiesSet();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
    if (schemaUpdate == true) {
      super.setSchemaUpdate(schemaUpdate);
      updateDatabaseSchema();
    }
    if (initialXmlData == null) {
      log.info("no initial data specified");
      return;
    }
    log.debug("loading initial data from " + initialXmlData);

    HibernateTemplate template = new HibernateTemplate((SessionFactory) getObject());

    HibernateXmlConverter converter = new HibernateXmlConverter();
    converter.setHibernate(template);
    try {
      log.info("Loading initial data from " + initialXmlData + " into database");
      converter.fillDatabaseFromXml(new InputStreamReader(initialXmlData.getInputStream(), "utf-8"));
    } catch (Exception ex) {
      log.fatal("Filling initial data failed " + ex, ex);
    } finally {
      template.clear();
    }
  }

  private void addClassNames(AnnotationConfiguration config, String[] classNames)
  {
    log.debug("adding classes " + Arrays.asList(classNames));
    for (String className : classNames) {
      try {
        if ( StringUtils.trimToNull(className) == null) {
          continue;
        }
        classesToAdd.add(Class.forName(className));
      } catch (Exception ex) {
        log.fatal("Exception encountered " + ex + " while adding class " + className, ex);
      }
    }
  }
}
