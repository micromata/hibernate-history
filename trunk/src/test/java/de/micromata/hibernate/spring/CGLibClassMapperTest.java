/////////////////////////////////////////////////////////////////////////////
//
// $RCSfile: CGLibClassMapperTest.java,v $
//
// Project   Hibernate3History
//
// Author    Wolfgang Jung (w.jung@micromata.de)
// Created   Jan 8, 2006
// Copyright Micromata Jan 8, 2006
//
// $Id: CGLibClassMapperTest.java,v 1.1 2007/03/08 22:50:50 wolle Exp $
// $Revision: 1.1 $
// $Date: 2007/03/08 22:50:50 $
//
/////////////////////////////////////////////////////////////////////////////
package de.micromata.hibernate.spring;

import junit.framework.TestCase;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

import com.thoughtworks.xstream.mapper.DefaultMapper;
import com.thoughtworks.xstream.mapper.MapperWrapper;

/**
 * 
 * @author Costin Leau
 *
 */
public class CGLibClassMapperTest extends TestCase {


    Obj original;
    Obj enhanced;
    String enhancedClassName;
    String originalClassName;
    
    MapperWrapper mapper, defaultMapper;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        defaultMapper = new DefaultMapper(getClass().getClassLoader());
        mapper = new CGLibClassMapper(defaultMapper);

        original = new Obj();
        enhanced = (Obj)Enhancer.create(original.getClass(), NoOp.INSTANCE);
        originalClassName = original.getClass().getName();
        enhancedClassName = enhanced.getClass().getName();

    }

    public void testClassSignature() {
        assertTrue(Enhancer.isEnhanced(enhanced.getClass()));
        assertFalse(originalClassName.equals(enhancedClassName));
    }

    /*
     * Test method for
     * 'CGLibMapper.mapNameToXML(String)'
     */
    public void testMapNameToXMLString() {
        assertEquals(originalClassName, mapper.realMember(Obj.class, enhancedClassName));
        assertFalse(originalClassName.equals(defaultMapper.realMember(Obj.class, enhancedClassName)));
        assertEquals(enhancedClassName, defaultMapper.realMember(Obj.class, enhancedClassName));
    }

    /*
     * Test method for
     * 'CGLibMapper.serializedClass(Class)'
     */
    public void testSerializedClassClass() {
        assertEquals(originalClassName, mapper.serializedClass(enhanced.getClass()));
        assertEquals(originalClassName, mapper.serializedClass(original.getClass()));
        assertFalse(originalClassName.equals(defaultMapper.serializedClass(enhanced.getClass())));
    }

    /*
     * Test method for
     * 'CGLibMapper.serializedMember(Class, String)'
     */
    public void testSerializedMemberClassString() {
        assertEquals(originalClassName, mapper.serializedMember(enhanced.getClass(), enhancedClassName));
        assertEquals(originalClassName, mapper.serializedMember(original.getClass(), originalClassName));
        assertFalse(originalClassName.equals(defaultMapper.serializedMember(enhanced.getClass(), enhancedClassName)));

    }

}
