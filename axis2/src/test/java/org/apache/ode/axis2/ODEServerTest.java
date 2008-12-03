package org.apache.ode.axis2;

import junit.framework.TestCase;

import javax.servlet.ServletException;
import java.io.File;

import org.junit.Test;

/**
 *
 */
public class ODEServerTest extends TestCase {

    public void testNonExistingRootDir() {
        String ghostDir = "/" + System.currentTimeMillis();
        assertFalse("This test requires a non existing directory", new File(ghostDir).isDirectory());
        System.setProperty("org.apache.ode.rootDir", ghostDir);
        try {
            new ODEServer().init((String) null, null);
            fail("Should throw an IllegalArgumentException if the root dir does not exist");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } catch (ServletException se) {
            fail("Should throw an IllegalArgumentException if the root dir does not exist");
        }finally {
            // reset to avoid side effects
            System.getProperties().remove("org.apache.ode.rootDir");
        }
    }

    public void testNonExistingConfigDir() {
        String ghostDir = "/" + System.currentTimeMillis();
        assertFalse("This test requires a non existing directory", new File(ghostDir).isDirectory());
        System.setProperty("org.apache.ode.configDir", ghostDir);
        try {
            new ODEServer().init(System.getProperty("user.dir"), null);
            fail("Should throw an IllegalArgumentException if the config dir does not exist");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } catch (ServletException se) {
            fail("Should throw an IllegalArgumentException if the config dir does not exist");
        }finally {
            // reset to avoid side effects
            System.getProperties().remove("org.apache.ode.configDir");
        }
    }
}
