/////////////////////////////////////////////////////////////////////////////
//
// $RCSfile: HistoryTag.java,v $
//
// Project   HibernateHistory
//
// Author    Wolfgang Jung (w.jung@micromata.de)
// Created   Sep 21, 2005
// Copyright Micromata Sep 21, 2005
//
// $Id: HistoryTag.java,v 1.2 2007-05-07 13:57:12 tung Exp $
// $Revision: 1.2 $
// $Date: 2007-05-07 13:57:12 $
//
/////////////////////////////////////////////////////////////////////////////
package de.micromata.hibernate.history.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.el.ELException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;

import org.springframework.beans.BeansException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class HistoryTag extends BodyTagSupport
{
  /**
   * 
   */
  private static final long serialVersionUID = 580887198648354448L;
  
  private static String defaultBundle;

  private String[] objectNames;

  private String sessionFactoryName = "sessionFactory";

  private int maxItems = 20;

  private String bundle = defaultBundle;

  private Locale locale;
  
  public static void setDefaultBundle(String bundle)
  {
    defaultBundle = bundle;
  }

  public void setObjectNames(final String objectNames)
  {
    this.objectNames = objectNames.split("[\\ , ]+");
  }

  public void setSessionFactoryName(final String sessionFactoryName)
  {
    this.sessionFactoryName = sessionFactoryName;
  }

  public void setMaxItems(final int maxItems)
  {
    this.maxItems = maxItems;
  }

  public void setBundle(String bundle)
  {
    this.bundle = bundle;
  }

  public void setLocale(Object locale)
  {
    if (locale instanceof Locale) {
      this.locale = (Locale) locale;
    } else if (locale instanceof String) {
      this.locale = new Locale((String) locale);
    }
  }

  @Override
  public int doStartTag() throws JspException
  {
    if (locale == null) {
      locale = pageContext.getRequest().getLocale();
    }

    Iterator<HistoryTableEntry> listIterator = getHistoryEntries();

    if (listIterator.hasNext() == false) {
      return SKIP_BODY;
    }

    ResourceBundle resourceBundle = ResourceBundle.getBundle(bundle, locale);
    JspWriter out = pageContext.getOut();
    try {
      out.write("<table class=\"historyTable\">\n");
      renderHead(out, resourceBundle);
      int evenOdd = 0;
      while (listIterator.hasNext() == true) {
        HistoryTableEntry hte = listIterator.next();
        renderEntry(out, evenOdd, hte);
        evenOdd++;
        if (maxItems > 0 && evenOdd >= maxItems) {
          break;
        }
      }
      out.write("</table>\n");
    } catch (IOException ex) {
      throw new JspException(ex);
    }
    return EVAL_PAGE;
  }

  /**
   * @param resourceBundle
   * @throws IOException
   */
  private void renderHead(JspWriter out, ResourceBundle resourceBundle) throws IOException
  {
    out.write("  <tr>\n");
    out.write("    <th class=\"user\">" + resourceBundle.getString("history.table.head.user") + "</th>\n");
    out.write("    <th class=\"timestamp\">" + resourceBundle.getString("history.table.head.timestamp") + "</th>\n");
    out.write("    <th class=\"aktion\">" + resourceBundle.getString("history.table.head.aktion") + "</th>\n");
    out.write("    <th class=\"property\">" + resourceBundle.getString("history.table.head.property") + "</th>\n");
    out.write("    <th class=\"oldValue\">" + resourceBundle.getString("history.table.head.oldValue") + "</th>\n");
    out.write("    <th class=\"newValue\">" + resourceBundle.getString("history.table.head.newValue") + "</th>\n");
    out.write("  </tr>\n");
  }

  /**
   * @param evenOdd
   * @param hte
   * @throws IOException
   */
  private void renderEntry(JspWriter out, int evenOdd, HistoryTableEntry hte) throws IOException
  {
    out.write("  <tr class=\"");
    out.write(evenOdd % 2 == 0 ? "even" : "odd");
    out.write("\" >\n    <td class=\"user\">");
    out.write(hte.getUser());
    out.write("</td>\n    <td class=\"timestamp\">");
    out.write(hte.getTimestamp());
    out.write("</td>\n    <td class=\"aktion\">");
    out.write(hte.getAction());
    out.write("</td>\n    <td class=\"property\">");
    out.write(hte.getProperty());
    out.write("</td>\n    <td class=\"oldValue\">");
    out.write(hte.getOldValue());
    out.write("</td>\n    <td class=\"newValue\">");
    out.write(hte.getNewValue());
    out.write("</td>\n  </tr>\n");
  }

  /**
   * @return
   * @throws BeansException
   * @throws JspException
   */
  private Iterator<HistoryTableEntry> getHistoryEntries() throws BeansException, JspException
  {
    WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(pageContext.getServletContext());
    SessionFactory sessionFactory = (SessionFactory) context.getBean(sessionFactoryName, SessionFactory.class);
    List<Object> items = new ArrayList<Object>();
    try {
      HistoryTable table = new HistoryTable(sessionFactory);
      for (int i = 0; i < objectNames.length; i++) {
        Object o = pageContext.getExpressionEvaluator().evaluate("${" + objectNames[i] + "}", Object.class,
            pageContext.getVariableResolver(), null);
        if (o instanceof Collection) {
          Collection<?> collection = (Collection<?>) o;
          items.addAll(collection);
        } else {
          items.add(o);
        }
      }
      Iterator<HistoryTableEntry> listIterator;

      listIterator = table.getHistoryList(items.toArray(), locale).iterator();
      return listIterator;
    } catch (ELException ex) {
      throw new JspException(ex);
    } catch (HibernateException ex) {
      throw new JspException(ex);
    }
  }

}
