/////////////////////////////////////////////////////////////////////////////
//
// $RCSfile: XmlStreamTest.java,v $
//
// Project   Hibernate3History
//
// Author    Wolfgang Jung (w.jung@micromata.de)
// Created   Jan 4, 2006
// Copyright Micromata Jan 4, 2006
//
// $Id: XmlStreamTest.java,v 1.1 2007/03/08 22:50:50 wolle Exp $
// $Revision: 1.1 $
// $Date: 2007/03/08 22:50:50 $
//
/////////////////////////////////////////////////////////////////////////////
package de.micromata.hib3.model;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.io.StringWriter;

import org.springframework.core.io.FileSystemResource;

import de.micromata.hibernate.spring.AutoSessionFactoryBean;
import de.micromata.hibernate.spring.HibernateXmlConverter;

public class XmlStreamTest extends HibernateTestBase
{
  @Override
  protected void changeAutoSessionFactoryBean(AutoSessionFactoryBean asfb)
  {
    asfb.setInitialXmlData(new FileSystemResource("src/test/resources/sample-data.xml"));
    asfb.setEntityInterceptor(null);
  }
  
  public void testSampleImport() throws IOException
  {
    assertEquals(2, hibernate.loadAll(UserDO.class).size());
    
    HibernateXmlConverter cnv = new HibernateXmlConverter();
    cnv.setHibernate(hibernate);
    StringWriter sw = new StringWriter();
    cnv.dumpDatabaseToXml(sw, false);
    LineNumberReader f1 = new LineNumberReader(new FileReader("src/test/resources/sample-data.xml"));
    LineNumberReader f2 = new LineNumberReader(new StringReader(sw.toString()));
    String line;
    while (( line = f1.readLine()) != null) {
      String other = f2.readLine();
      assertEquals(line, other);
    }
    f1.close();
    f2.close();
  }
}
