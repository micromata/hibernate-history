/////////////////////////////////////////////////////////////////////////////
//
// $RCSfile: NullWriter.java,v $
//
// Project   Hibernate3History
//
// Author    Wolfgang Jung (w.jung@micromata.de)
// Created   Jan 16, 2006
// Copyright Micromata Jan 16, 2006
//
// $Id: NullWriter.java,v 1.1 2007/03/08 22:50:48 wolle Exp $
// $Revision: 1.1 $
// $Date: 2007/03/08 22:50:48 $
//
/////////////////////////////////////////////////////////////////////////////
package de.micromata.hibernate.spring;

import java.io.Writer;

public class NullWriter extends Writer
{

  /**
   * @see java.io.Writer#close()
   */
  @Override
  public void close() 
  {
  }

  /**
   * @see java.io.Writer#flush()
   */
  @Override
  public void flush() 
  {
  }

  /**
   * @see java.io.Writer#write(char[], int, int)
   */
  @Override
  public void write(char[] cbuf, int off, int len) 
  {
  }
 
}
