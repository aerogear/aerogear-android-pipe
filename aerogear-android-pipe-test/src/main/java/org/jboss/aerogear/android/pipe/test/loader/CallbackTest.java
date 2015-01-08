/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.android.pipe.test.loader;

import android.app.Activity;
import android.app.Fragment;
import org.jboss.aerogear.android.pipe.callback.AbstractActivityCallback;
import org.jboss.aerogear.android.pipe.callback.AbstractFragmentCallback;
import org.jboss.aerogear.android.pipe.loader.LoaderAdapter;
import org.jboss.aerogear.android.pipe.loader.ReadLoader;
import org.jboss.aerogear.android.pipe.test.util.UnitTestUtils;
import org.jboss.aerogear.android.pipe.loader.LoaderAdapter.CallbackHandler;
import org.jboss.aerogear.android.pipe.test.util.PatchedActivityInstrumentationTestCase;
import org.jboss.aerogear.android.pipe.test.MainActivity;
import org.jboss.aerogear.android.pipe.Pipe;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class CallbackTest extends PatchedActivityInstrumentationTestCase<MainActivity> {

    public CallbackTest() {
        super(MainActivity.class);
    }

    public void testPassModernFragmentCallbacks() throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        Fragment fragment = Mockito.mock(Fragment.class);

        LoaderAdapter adapter = new LoaderAdapter(fragment, getActivity(), mock(Pipe.class), "ignore");
        VoidFragmentCallback fragmentCallback = new VoidFragmentCallback();
        ReadLoader loader = Mockito.mock(ReadLoader.class);
        Mockito.when(loader.getCallback()).thenReturn(fragmentCallback);

        Object data = "Data";
        CallbackHandler handler = new CallbackHandler(adapter, loader, data);
        handler.run();
        assertTrue(fragmentCallback.successCalled);
        assertNull(UnitTestUtils.getSuperPrivateField(fragmentCallback, "fragment"));

    }

    public void testFailModernFragmentCallbacks() throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        Fragment fragment = Mockito.mock(Fragment.class);

        LoaderAdapter adapter = new LoaderAdapter(fragment, getActivity(), mock(Pipe.class), "ignore");
        VoidFragmentCallback fragmentCallback = new VoidFragmentCallback();
        ReadLoader loader = Mockito.mock(ReadLoader.class);
        Mockito.when(loader.getCallback()).thenReturn(fragmentCallback);
        Mockito.when(loader.hasException()).thenReturn(true);
        Mockito.when(loader.getException()).thenReturn(new RuntimeException("This is only a test exception."));

        Object data = "Data";
        CallbackHandler handler = new CallbackHandler(adapter, loader, data);
        handler.run();
        assertTrue(fragmentCallback.failCalled);
        assertNull(UnitTestUtils.getSuperPrivateField(fragmentCallback, "fragment"));

    }

    public void testPassModernActivityCallbacks() throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        Activity activity = Mockito.mock(Activity.class);

        LoaderAdapter adapter = new LoaderAdapter(activity, mock(Pipe.class), "ignore");
        VoidActivityCallback activityCallback = new VoidActivityCallback();
        ReadLoader loader = Mockito.mock(ReadLoader.class);
        Mockito.when(loader.getCallback()).thenReturn(activityCallback);

        Object data = "Data";
        CallbackHandler handler = new CallbackHandler(adapter, loader, data);
        handler.run();
        assertTrue(activityCallback.successCalled);
        assertNull(UnitTestUtils.getSuperPrivateField(activityCallback, "activity"));

    }

    public void testFailModernActivityCallbacks() throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        Activity activity = Mockito.mock(Activity.class);

        LoaderAdapter adapter = new LoaderAdapter(activity, mock(Pipe.class), "ignore");
        VoidActivityCallback activityCallback = new VoidActivityCallback();
        ReadLoader loader = Mockito.mock(ReadLoader.class);
        Mockito.when(loader.getCallback()).thenReturn(activityCallback);
        Mockito.when(loader.hasException()).thenReturn(true);
        Mockito.when(loader.getException()).thenReturn(new RuntimeException("This is only a test exception."));

        Object data = "Data";
        CallbackHandler handler = new CallbackHandler(adapter, loader, data);
        handler.run();
        assertTrue(activityCallback.failCalled);
        assertNull(UnitTestUtils.getSuperPrivateField(activityCallback, "activity"));

    }

    private static class VoidFragmentCallback extends AbstractFragmentCallback<Object> {

        boolean successCalled = false;
        boolean failCalled = false;

        public VoidFragmentCallback() {
            super("HashCode");
        }

        @Override
        public void onSuccess(Object data) {
            assertNotNull(getFragment());
            successCalled = true;
        }

        @Override
        public void onFailure(Exception e) {
            assertNotNull(getFragment());
            failCalled = true;
        }

    }

    private static class VoidActivityCallback extends AbstractActivityCallback<Object> {

        boolean successCalled = false;
        boolean failCalled = false;

        public VoidActivityCallback() {
            super("HashCode");
        }

        @Override
        public void onSuccess(Object data) {
            assertNotNull(getActivity());
            successCalled = true;
        }

        @Override
        public void onFailure(Exception e) {
            assertNotNull(getActivity());
            failCalled = true;
        }

    }

}
