/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.storage.apm;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.storage.apm.StorageManager;
import org.eclipse.packagedrone.storage.apm.StorageRegistration;
import org.eclipse.scada.utils.io.RecursiveDeleteVisitor;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class BaseTest
{
    private static Path basePath;

    @BeforeClass
    public static void setup () throws IOException
    {
        basePath = Paths.get ( ".", "target", "test" ).toAbsolutePath ();
        if ( Files.exists ( basePath ) )
        {
            Files.walkFileTree ( basePath, new RecursiveDeleteVisitor () );
        }
    }

    private StorageManager mgr;

    @Before
    public void init ()
    {
        this.mgr = new StorageManager ( basePath );
    }

    @After
    public void cleanup ()
    {
        this.mgr.close ();
    }

    /**
     * Test a simple init and close
     */
    @Test
    public void test1 ()
    {
        final StorageManager mgr = new StorageManager ( basePath );
        mgr.close ();
    }

    /**
     * Test a plain registration and diposal
     */
    @Test
    public void test2a ()
    {
        final StorageManager mgr = new StorageManager ( basePath );
        final StorageRegistration reg = mgr.registerModel ( 1, new MetaKey ( "mock", "1" ), new MockStorageProvider ( "1", "foo" ) );
        reg.unregister ();
        mgr.close ();
    }

    /**
     * Test a registration and unregistration after disposal
     * <p>
     * Although the unregister method is called after the close method, this
     * must not cause any troubles.
     * </p>
     */
    @Test
    public void test2b ()
    {
        final StorageManager mgr = new StorageManager ( basePath );
        final StorageRegistration reg = mgr.registerModel ( 1, new MetaKey ( "mock", "1" ), new MockStorageProvider ( "1", "foo" ) );
        mgr.close ();
        reg.unregister ();
    }

    /**
     * Register a model after the manager was closed. Expect failure!
     */
    @Test ( expected = IllegalStateException.class )
    public void test2c ()
    {
        final StorageManager mgr = new StorageManager ( basePath );
        mgr.close ();
        mgr.registerModel ( 1, new MetaKey ( "mock", "1" ), new MockStorageProvider ( "1", "foo" ) );
    }

    /**
     * Register the same model twice. Expect failure!
     */
    @Test ( expected = IllegalArgumentException.class )
    public void test2d ()
    {
        this.mgr.registerModel ( 1, new MetaKey ( "mock", "1" ), new MockStorageProvider ( "1", "foo" ) );
        this.mgr.registerModel ( 1, new MetaKey ( "mock", "1" ), new MockStorageProvider ( "1", "foo" ) );
    }

    /**
     * Access a model
     */
    @Test
    public void test3a ()
    {
        final StorageRegistration reg = this.mgr.registerModel ( 1, new MetaKey ( "mock", "3a" ), new MockStorageProvider ( "3a", "foo" ) );

        this.mgr.accessRun ( new MetaKey ( "mock", "3a" ), MockStorageViewModel.class, m -> {
            assertEquals ( "foo", m.getValue () );
        } );

        reg.unregister ();
    }

    /**
     * Access and modify a model
     */
    @Test
    public void test3b ()
    {
        final MetaKey key = new MetaKey ( "mock", "3b" );

        final StorageRegistration reg = this.mgr.registerModel ( 1, key, new MockStorageProvider ( "3b", "foo" ) );

        this.mgr.accessRun ( key, MockStorageViewModel.class, m -> {
            assertEquals ( "foo", m.getValue () );
        } );

        this.mgr.modifyRun ( key, MockStorageModel.class, m -> {
            m.setValue ( "bar" );
        } );

        this.mgr.accessRun ( key, MockStorageViewModel.class, m -> {
            assertEquals ( "bar", m.getValue () );
        } );

        this.mgr.modifyRun ( key, MockStorageModel.class, m -> {
            assertEquals ( "bar", m.getValue () );
        } );

        reg.unregister ();
    }

    /**
     * Access and modify a model. During the modification generate an error.
     * Access the model again.
     * <p>
     * This should test of the model is being rolled back due to a failure
     * during the modification
     * </p>
     */
    @Test
    public void test4a ()
    {
        final MetaKey key = new MetaKey ( "mock", "4a" );

        this.mgr.registerModel ( 1, key, new MockStorageProvider ( "4a", "foo" ) );

        this.mgr.accessRun ( key, MockStorageViewModel.class, m -> {
            assertEquals ( "foo", m.getValue () );
        } );

        Exception ex = null;
        try
        {
            this.mgr.modifyRun ( key, MockStorageModel.class, m -> {
                m.setValue ( "bar" );
                throw new RuntimeException ();
            } );
        }
        catch ( final Exception e )
        {
            ex = e;
        }

        assertNotNull ( ex );

        this.mgr.accessRun ( key, MockStorageViewModel.class, m -> {
            assertEquals ( "foo", m.getValue () );
        } );
    }

    /**
     * Access three models in the correct lock order
     */
    @Test
    public void test5a ()
    {
        final MetaKey key1 = new MetaKey ( "mock", "5a1" );
        final MetaKey key2 = new MetaKey ( "mock", "5a2" );
        final MetaKey key3 = new MetaKey ( "mock", "5a3" );

        this.mgr.registerModel ( 1, key1, new MockStorageProvider ( key1.getKey (), "foo" ) );
        this.mgr.registerModel ( 2, key2, new MockStorageProvider ( key2.getKey (), "foo" ) );
        this.mgr.registerModel ( 3, key3, new MockStorageProvider ( key3.getKey (), "foo" ) );

        this.mgr.accessRun ( key1, MockStorageViewModel.class, m1 -> {
            this.mgr.accessRun ( key2, MockStorageViewModel.class, m2 -> {
                this.mgr.accessRun ( key3, MockStorageViewModel.class, m3 -> {
                } );
            } );
        } );
    }

    /**
     * Access the same model ... test re-locking
     */
    @Test
    public void test5b ()
    {
        final MetaKey key1 = new MetaKey ( "mock", "5b1" );

        this.mgr.registerModel ( 1, key1, new MockStorageProvider ( key1.getKey (), "foo" ) );

        this.mgr.accessRun ( key1, MockStorageViewModel.class, m1 -> {
            this.mgr.accessRun ( key1, MockStorageViewModel.class, m2 -> {
                this.mgr.accessRun ( key1, MockStorageViewModel.class, m3 -> {
                } );
            } );
        } );
    }

    /**
     * Lock two models in the wrong lock order. Expect failure!
     */
    @Test ( expected = IllegalStateException.class )
    public void test5c ()
    {
        final MetaKey key1 = new MetaKey ( "mock", "5c1" );
        final MetaKey key2 = new MetaKey ( "mock", "5c2" );

        this.mgr.registerModel ( 1, key1, new MockStorageProvider ( key1.getKey (), "foo" ) );
        this.mgr.registerModel ( 2, key2, new MockStorageProvider ( key2.getKey (), "foo" ) );

        this.mgr.accessRun ( key2, MockStorageViewModel.class, m1 -> {
            this.mgr.accessRun ( key1, MockStorageViewModel.class, m2 -> {
            } );
        } );
    }

    /**
     * Lock three models in the correct lock order. Do it twice.
     */
    @Test
    public void test5d ()
    {
        final MetaKey key1 = new MetaKey ( "mock", "5d1" );
        final MetaKey key2 = new MetaKey ( "mock", "5d2" );
        final MetaKey key3 = new MetaKey ( "mock", "5d3" );

        this.mgr.registerModel ( 1, key1, new MockStorageProvider ( key1.getKey (), "foo" ) );
        this.mgr.registerModel ( 2, key2, new MockStorageProvider ( key2.getKey (), "foo" ) );
        this.mgr.registerModel ( 3, key3, new MockStorageProvider ( key3.getKey (), "foo" ) );

        this.mgr.accessRun ( key1, MockStorageViewModel.class, m1 -> {
            this.mgr.accessRun ( key2, MockStorageViewModel.class, m2 -> {
                this.mgr.accessRun ( key3, MockStorageViewModel.class, m3 -> {
                } );
            } );
        } );

        this.mgr.accessRun ( key1, MockStorageViewModel.class, m1 -> {
            this.mgr.accessRun ( key2, MockStorageViewModel.class, m2 -> {
                this.mgr.accessRun ( key3, MockStorageViewModel.class, m3 -> {
                } );
            } );
        } );
    }

    /**
     * Lock three models in the correct lock order. But re-lock an already
     * locked model. Do it twice.
     */
    @Test
    public void test5e ()
    {
        final MetaKey key1 = new MetaKey ( "mock", "5e1" );
        final MetaKey key2 = new MetaKey ( "mock", "5e2" );
        final MetaKey key3 = new MetaKey ( "mock", "5e3" );

        this.mgr.registerModel ( 1, key1, new MockStorageProvider ( key1.getKey (), "foo" ) );
        this.mgr.registerModel ( 2, key2, new MockStorageProvider ( key2.getKey (), "foo" ) );
        this.mgr.registerModel ( 3, key3, new MockStorageProvider ( key3.getKey (), "foo" ) );

        this.mgr.accessRun ( key1, MockStorageViewModel.class, m1 -> {
            this.mgr.accessRun ( key2, MockStorageViewModel.class, m2 -> {
                this.mgr.accessRun ( key1, MockStorageViewModel.class, m1a -> {
                    this.mgr.accessRun ( key3, MockStorageViewModel.class, m3 -> {
                    } );
                } );
            } );
        } );

        this.mgr.accessRun ( key1, MockStorageViewModel.class, m1 -> {
            this.mgr.accessRun ( key2, MockStorageViewModel.class, m2 -> {
                this.mgr.accessRun ( key1, MockStorageViewModel.class, m1a -> {
                    this.mgr.accessRun ( key3, MockStorageViewModel.class, m3 -> {
                    } );
                } );
            } );
        } );
    }

    /**
     * Lock three models in the wrong lock order. But re-lock an already
     * locked model before. Expect failure!
     */
    @Test ( expected = IllegalStateException.class )
    public void test5f ()
    {
        final MetaKey key1 = new MetaKey ( "mock", "5f1" );
        final MetaKey key2 = new MetaKey ( "mock", "5f2" );
        final MetaKey key3 = new MetaKey ( "mock", "5f3" );

        this.mgr.registerModel ( 1, key1, new MockStorageProvider ( key1.getKey (), "foo" ) );
        this.mgr.registerModel ( 2, key2, new MockStorageProvider ( key2.getKey (), "foo" ) );
        this.mgr.registerModel ( 3, key3, new MockStorageProvider ( key3.getKey (), "foo" ) );

        this.mgr.accessRun ( key1, MockStorageViewModel.class, m1 -> {
            this.mgr.accessRun ( key3, MockStorageViewModel.class, m3 -> {
                this.mgr.accessRun ( key1, MockStorageViewModel.class, m1a -> {
                    this.mgr.accessRun ( key2, MockStorageViewModel.class, m2 -> {
                    } );
                } );
            } );
        } );
    }

    /**
     * Re-lock a model and make changes. Only the outer lock should persist.
     */
    @Test
    public void test5g ()
    {
        final MetaKey key1 = new MetaKey ( "mock", "5g1" );
        final MetaKey key2 = new MetaKey ( "mock", "5g2" );

        this.mgr.registerModel ( 1, key1, new MockStorageProvider ( key1.getKey (), "foo" ) );
        this.mgr.registerModel ( 2, key2, new MockStorageProvider ( key2.getKey (), "foo" ) );

        this.mgr.modifyRun ( key1, MockStorageModel.class, m1 -> {
            this.mgr.accessRun ( key2, MockStorageViewModel.class, m2 -> {
                this.mgr.modifyRun ( key1, MockStorageModel.class, m1a -> {
                    m1a.setValue ( "bar" );
                } );
            } );
        } );

        this.mgr.accessRun ( key1, MockStorageViewModel.class, m1 -> {
            assertEquals ( "bar", m1.getValue () );
        } );
    }

    /**
     * Try to upgrade read lock to a write lock. Expect failure!
     */
    @Test ( expected = IllegalStateException.class )
    public void test5h ()
    {
        final MetaKey key1 = new MetaKey ( "mock", "5h1" );

        this.mgr.registerModel ( 1, key1, new MockStorageProvider ( key1.getKey (), "foo" ) );

        this.mgr.accessRun ( key1, MockStorageViewModel.class, m1r -> {
            this.mgr.modifyRun ( key1, MockStorageModel.class, m1w -> {
            } );
        } );
    }

    /**
     * Re-lock key2 and modify in sub-call and fail in upper call. Expect
     * rollback.
     */
    @Test
    public void test6a ()
    {
        final MetaKey key1 = new MetaKey ( "mock", "6a1" );
        final MetaKey key2 = new MetaKey ( "mock", "6a2" );
        final MetaKey key3 = new MetaKey ( "mock", "6a3" );

        this.mgr.registerModel ( 1, key1, new MockStorageProvider ( key1.getKey (), "foo" ) );
        this.mgr.registerModel ( 2, key2, new MockStorageProvider ( key2.getKey (), "foo" ) );
        this.mgr.registerModel ( 3, key3, new MockStorageProvider ( key3.getKey (), "foo" ) );

        Exception ex = null;
        try
        {
            this.mgr.modifyRun ( key1, MockStorageModel.class, m1 -> {
                m1.setValue ( "bar" );
                this.mgr.modifyRun ( key2, MockStorageModel.class, m2 -> {
                    this.mgr.accessRun ( key3, MockStorageViewModel.class, m3 -> {
                        this.mgr.modifyRun ( key2, MockStorageModel.class, m2a -> {
                            m2a.setValue ( "bar" );
                        } );
                        throw new RuntimeException ( "failure" );
                    } );
                } );
            } );
        }
        catch ( final Exception e )
        {
            ex = e;
        }

        assertNotNull ( ex );

        this.mgr.accessRun ( key1, MockStorageViewModel.class, m1 -> {
            assertEquals ( "foo", m1.getValue () );
        } );
        this.mgr.accessRun ( key2, MockStorageViewModel.class, m2 -> {
            assertEquals ( "foo", m2.getValue () );
        } );
    }

    /**
     * Cause an internal failure by specifying the wrong model class. Expect
     * rollback.
     */
    @Test
    public void test6b ()
    {
        final MetaKey key1 = new MetaKey ( "mock", "6b1" );
        final MetaKey key2 = new MetaKey ( "mock", "6b2" );

        this.mgr.registerModel ( 1, key1, new MockStorageProvider ( key1.getKey (), "foo" ) );
        this.mgr.registerModel ( 2, key2, new MockStorageProvider ( key2.getKey (), "foo" ) );

        Exception ex = null;
        try
        {
            this.mgr.modifyRun ( key1, MockStorageModel.class, m1 -> {
                m1.setValue ( "bar" );

                // the next line specifies the wrong model class
                this.mgr.modifyRun ( key2, String.class, m2 -> {
                    // no-op
                } );
            } );
        }
        catch ( final Exception e )
        {
            ex = e;
        }

        assertNotNull ( ex );

        this.mgr.accessRun ( key1, MockStorageViewModel.class, m1 -> {
            assertEquals ( "foo", m1.getValue () );
        } );
        this.mgr.accessRun ( key2, MockStorageViewModel.class, m2 -> {
            assertEquals ( "foo", m2.getValue () );
        } );
    }

    /**
     * Request the write model in read mode. Expect failure!
     */
    @Test ( expected = Exception.class )
    public void test6c ()
    {
        final MetaKey key1 = new MetaKey ( "mock", "6c1" );

        this.mgr.registerModel ( 1, key1, new MockStorageProvider ( key1.getKey (), "foo" ) );

        this.mgr.accessRun ( key1, MockStorageModel.class, m1 -> {
            m1.setValue ( "bar" );
        } );
    }

    /**
     * Test plain "after" execution.
     */
    @Test
    public void test7a ()
    {
        final LinkedList<String> result = new LinkedList<> ();

        final MetaKey key1 = new MetaKey ( "mock", "7a1" );
        this.mgr.registerModel ( 1, key1, new MockStorageProvider ( key1.getKey (), "foo" ) );

        this.mgr.accessRun ( key1, MockStorageViewModel.class, m1 -> {
            StorageManager.executeAfterPersist ( () -> result.add ( "1" ) );
            // this was executed immediately
            assertArrayEquals ( new Object[] { "1" }, result.toArray () );
        } );

        this.mgr.modifyRun ( key1, MockStorageModel.class, m1 -> {
            StorageManager.executeAfterPersist ( () -> result.add ( "2" ) );
            // this was scheduled for later
            assertArrayEquals ( new Object[] { "1" }, result.toArray () );
        } );

        // finally check all
        System.out.println ( result );
        assertArrayEquals ( new Object[] { "1", "2" }, result.toArray () );
    }

    /**
     * Test plain "after" execution.
     */
    @Test
    public void test7b ()
    {
        final LinkedList<String> result = new LinkedList<> ();

        final MetaKey key1 = new MetaKey ( "mock", "7b1" );
        this.mgr.registerModel ( 1, key1, new MockStorageProvider ( key1.getKey (), "foo" ) );

        this.mgr.modifyRun ( key1, MockStorageModel.class, m1 -> {

            StorageManager.executeAfterPersist ( () -> result.add ( "1" ) );
            // scheduled
            assertArrayEquals ( new Object[] {}, result.toArray () );

            this.mgr.accessRun ( key1, MockStorageViewModel.class, m1a -> {
                StorageManager.executeAfterPersist ( () -> result.add ( "2" ) );
                // should be scheduled as well, since we have an outer modify call
                assertArrayEquals ( new Object[] {}, result.toArray () );
            } );

            // again, no change
            assertArrayEquals ( new Object[] {}, result.toArray () );
        } );

        // finally check all
        System.out.println ( result );
        assertArrayEquals ( new Object[] { "1", "2" }, result.toArray () );
    }

    /**
     * Fetch the value of the read model inside the a modify lock.
     * <p>
     * Expect the read model to contain the content of the write model
     * </p>
     */
    @Test
    public void test8a ()
    {
        final MetaKey key1 = new MetaKey ( "mock", "8a1" );
        this.mgr.registerModel ( 1, key1, new MockStorageProvider ( key1.getKey (), "foo" ) );

        final String value1 = "foooo";

        this.mgr.modifyRun ( key1, MockStorageModel.class, m1 -> {
            m1.setValue ( value1 );
            this.mgr.accessRun ( key1, MockStorageViewModel.class, m1a -> {
                assertEquals ( value1, m1a.getValue () );
            } );
        } );
    }

    /**
     * Fetch the value of the read model inside the a modify lock.
     * <p>
     * Expect the read model to contain the content of the write model
     * </p>
     */
    @Test
    public void test8b ()
    {
        final MetaKey key1 = new MetaKey ( "mock", "8b1" );
        this.mgr.registerModel ( 1, key1, new MockStorageProvider ( key1.getKey (), "foo" ) );

        final String value1 = "foooo";
        final String value2 = "baaar";

        this.mgr.modifyRun ( key1, MockStorageModel.class, m1 -> {
            m1.setValue ( value1 );
            this.mgr.accessRun ( key1, MockStorageViewModel.class, m1a -> {
                assertEquals ( value1, m1a.getValue () );
            } );
        } );

        this.mgr.modifyRun ( key1, MockStorageModel.class, m1 -> {
            m1.setValue ( value2 );
            this.mgr.accessRun ( key1, MockStorageViewModel.class, m1a -> {
                assertEquals ( value2, m1a.getValue () );
            } );
            m1.setValue ( value1 );
            this.mgr.accessRun ( key1, MockStorageViewModel.class, m1a -> {
                assertEquals ( value1, m1a.getValue () );
            } );
        } );
    }

    /**
     * Fetch the value of the read model inside the a modify lock. Access is
     * nested.
     * <p>
     * Expect the read model to contain the content of the write model
     * </p>
     */
    @Test
    public void test8c ()
    {
        final MetaKey key1 = new MetaKey ( "mock", "8c1" );
        this.mgr.registerModel ( 1, key1, new MockStorageProvider ( key1.getKey (), "foo" ) );

        final String value1 = "foooo";
        final String value2 = "baaar";

        this.mgr.modifyRun ( key1, MockStorageModel.class, m1 -> {
            m1.setValue ( value1 );
            this.mgr.accessRun ( key1, MockStorageViewModel.class, m1a -> {
                assertEquals ( value1, m1a.getValue () );
                m1.setValue ( value2 );
                this.mgr.accessRun ( key1, MockStorageViewModel.class, m1c -> {
                    assertEquals ( value2, m1c.getValue () );
                } );
            } );
        } );
    }

    /**
     * Fetch the value of the read model inside the a modify lock. Access is
     * nested.
     * <p>
     * Expect the read model to contain the content of the write model
     * </p>
     */
    @Test
    public void test8d ()
    {
        final MetaKey key1 = new MetaKey ( "mock", "8d1" );
        this.mgr.registerModel ( 1, key1, new MockStorageProvider ( key1.getKey (), "foo" ) );

        final String value1 = "foooo";
        final String value2 = "baaar";

        this.mgr.modifyRun ( key1, MockStorageModel.class, m1 -> {
            m1.setValue ( value1 );
            this.mgr.accessRun ( key1, MockStorageViewModel.class, m1a -> {
                assertEquals ( value1, m1a.getValue () );
                m1.setValue ( value2 );
                this.mgr.accessRun ( key1, MockStorageViewModel.class, m1c -> {
                    assertEquals ( value2, m1c.getValue () );
                } );
            } );
        } );
    }

}
