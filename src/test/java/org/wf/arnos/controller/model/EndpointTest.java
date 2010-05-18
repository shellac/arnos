/*
 *  © University of Bristol
 */

package org.wf.arnos.controller.model;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author chris
 */
public class EndpointTest {

    @Test
    public void testMiscFunctions() {
        System.out.println("testMiscFunctions");

        String initUri = "TestURI";
        Endpoint ep = new Endpoint(initUri);

        String expectedResult = "$HGBW%@";
        assertEquals(initUri,ep.getLocation());
        ep.setLocation(expectedResult);
        assertEquals(expectedResult, ep.getLocation());

        assertEquals(true,ep.toString().contains(expectedResult));
        assertEquals(false,ep.toString().contains(initUri));

        ep.setLocation(null);
        assertEquals(0, ep.hashCode());
    }

    @Test
    public void testEquals()
    {
        System.out.println("testEquals");

        String initUri = "TestURI";
        Endpoint ep = new Endpoint(initUri);

        assertEquals(true,ep.equals(ep));
        assertEquals(false,ep.equals(initUri));
        assertEquals(false,ep.equals(new Project(initUri)));
        assertEquals(false,ep.equals(new Endpoint(initUri+"other")));
        assertEquals(true,ep.equals(new Endpoint(initUri)));

    }

    @Test
    public void testCompareTo()
    {
        System.out.println("testCompareTo");

        String initUri = "TestURI";
        Endpoint ep = new Endpoint(initUri);
        assertEquals(true,0 > ep.compareTo(null));
        assertEquals(0,ep.compareTo(ep));
        assertEquals(0,ep.compareTo(new Endpoint(initUri)));

        assertEquals(true,0 > ep.compareTo(new Endpoint("U")));
        assertEquals(true,0 < ep.compareTo(new Endpoint("S")));
  }

    @Test
    public void testIdentifierGeneration()
    {
        System.out.println("testIdentifierGeneration");

        String initUri = "TestURI";
        Endpoint ep = new Endpoint(initUri);
        assertTrue(!ep.getIdentifier().equals(""));
  }
}