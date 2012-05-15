/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.core.concurrent;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.tcf.core.concurrent.interfaces.IProxyDescriptor;
import org.eclipse.tcf.te.tcf.core.concurrent.internal.DefaultProxyDescriptor;

/**
 * BlockingCallProxy is a utility class. It can create an instance of a proxy class for the specified
 * interface that dispatches method invocations to the delegate object. This delegate object must
 * implement the same interface.
 * <p>
 * When the method dispatched is an asynchronous invocation which calls back in the dispatching
 * thread, the proxy instance will block the method call until the callback happens.
 * <p>
 * Take IFileSystem service as an example. The following method in it:
 * <pre>
 * IToken read(IFileHandle handle, long offset, int len, DoneRead done);
 * </pre>
 * <p>
 * This is an asynchronous method which will return immediately after its invocation. When the read
 * operation is done, the callback parameter "done", which implements the interface
 * <code>DoneRead</code>, is invoked to pass back the reading result.
 * <p>
 * Now if you want to invoke this method and wait for the result, you can use the following code
 * with this class:
 *
 * <pre>
 * IFileSystem service = ... // Get the file system service.
 * IFileSystem proxy = BlockingCallProxy.newInstance(IFileSystem.class, service);
 * final byte[][] result = new byte[1][];
 * proxy.read(handle, offset, len, new DoneRead(){
 * 		public void doneRead(IToken token, FileSystemException error, byte[] data, boolean eof) {
 * 		if(error == null)
 * 				result[0] = data; //Save the reading result.
 *      }
 * });
 * output.write(result[0]); // Now use the reading result.
 * ...
 * </pre>
 *
 * In the above code, the line that does the read operation over the proxy object will block and
 * will not return until "doneRead" of the callback is called. The line that follows the read
 * operation can safely consume the read data.
 * <p>
 * <em>Note that all method call over the proxy must be made <b>OUTSIDE</b> of
 * the dispatching thread.</em> If it is called inside of the dispatching thread, the call will be
 * blocked forever.
 * <p>
 * The methods that require blocking invocation are those which have a callback parameter. The callback
 * parameter is identified by a IProxyDescriptor which could tell which parameter is the callback.
 *
 * @see IProxyDescriptor
 * @see org.eclipse.tcf.services.IFileSystem
 * @see DefaultProxyDescriptor
 */
public class BlockingCallProxy implements InvocationHandler {
	// The default timeout waiting for blocked invocations.
	private static final long DEFAULT_TIMEOUT = 60000L;

	/**
	 * Create an instance of a proxy class for the specified interface that dispatches method
	 * invocations to the delegate object. This delegate object must implement the same interface.
	 *
	 * @param proxyInterface The proxy interface for the proxy class.
	 * @param delegate The delegate object which the proxy dispatches the method invocations to.
	 * @return The proxy instance.
	 */
	public static <T> T newInstance(Class<T> proxyInterface, T delegate) {
		return newInstance(proxyInterface, DefaultProxyDescriptor.getProxyDescriptor(proxyInterface), delegate);
	}

	/**
	 * Create an instance of a proxy class for the specified interface that dispatches method
	 * invocations to the delegate object. This delegate object must implement the same interface.
	 *
	 * @param proxyInterface The proxy interface for the proxy class.
	 * @param proxyDescriptor The proxy descriptor for the proxy class.
	 * @param delegate The delegate object which the proxy dispatches the method invocations to.
	 * @return The proxy instance.
	 */
	public static <T> T newInstance(Class<T> proxyInterface, IProxyDescriptor proxyDescriptor, T delegate) {
		Assert.isTrue(proxyInterface != null && delegate != null);
		ClassLoader classLoader = proxyInterface.getClassLoader();
		InvocationHandler handler = new BlockingCallProxy(proxyDescriptor, delegate);
		return (T) Proxy.newProxyInstance(classLoader, new Class[] { proxyInterface }, handler);
	}

	// The timeout waiting for blocked invocations.
	private long timeout;
	// Callback descriptor
	private IProxyDescriptor proxyDescriptor;
	// The delegate object which the proxy dispatches the method invocations to.
	/* default */ Object delegate;

	/**
	 * Create an instance of BlockingCall as the dynamic invocation handler of the proxy class.
	 *
	 * @param delegate The delegate object which the proxy dispatches the method invocations to.
	 */
	private BlockingCallProxy(IProxyDescriptor proxyDescriptor, Object delegate) {
		this.timeout = DEFAULT_TIMEOUT;
		this.delegate = delegate;
		this.proxyDescriptor = proxyDescriptor;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method,
	 * java.lang.Object[])
	 */
	@Override
	public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
		Assert.isTrue(!Protocol.isDispatchThread());
		Rendezvous rendezvous = prepareArguments(method, args);
		if (proxyDescriptor.isDispatchMethod(method)) {
			// If the method should be called inside the dispatching thread.
			return dispatchCall(method, args, rendezvous);
		}
		// Call it directly.
		return directCall(method, args, rendezvous);
	}

	/**
	 * Prepare the proxy calling arguments based on the invocation method and its arguments.
	 * If the method being invoked is a proxy method, then replace the callback argument with
	 * a dynamic generated proxy that could delegate the invocation and unblock the callback.
	 * Return a rendezvous object to be used to block the call. If it is not a proxy method, then
	 * just return null object.
	 *
	 * @param method The method being called.
	 * @param args The invocation arguments.
	 * @return The rendezvous object of the proxy call or null if no blocking is needed.
	 */
	private Rendezvous prepareArguments(final Method method, final Object[] args) {
	    if (proxyDescriptor.isProxyMethod(method)) {
			// Find the callback parameter index.
			int index = proxyDescriptor.getCallbackIndex(method);
			if (index != -1) {
				// Replace the callback with a proxy that block the call.
				Rendezvous rendezvous = new Rendezvous();
				Class<?>[] aTypes = method.getParameterTypes();
				args[index] = Proxy.newProxyInstance(aTypes[index].getClassLoader(),
								new Class[] { aTypes[index] }, new DoneHandler(rendezvous, args[index]));
				return rendezvous;
			}
		}
	    return null;
    }

	/**
	 * The invocation handler of the callback proxy. Used to delegate the callback invocation
	 * and unblock the rendezvous object.
	 */
	static private class DoneHandler implements InvocationHandler {
		// The callback handler that delegates the invocation.
		private Object done;
		// The rendezvous that unblocks the invocation.
		private Rendezvous rendezvous;
		/**
		 * Constructor with the two arguments to initialize the two fields.
		 *
		 * @param rendezvous The rendezvous object to unblock the invocation
		 * @param done The callback handler that delegates the invocation.
		 */
		public DoneHandler(Rendezvous rendezvous, Object done) {
			this.rendezvous = rendezvous;
			this.done = done;
		}
		/*
		 * (non-Javadoc)
		 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
		 */
		@Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object result = method.invoke(done, args);
            rendezvous.arrive();
            return result;
        }
	}

	/**
	 * Invoke the call directly in the current thread, using the rendezvous object to block the call.
	 * If the rendezvous object is null, then just invoke the call directly.
	 *
	 * @param method The method to be invoked.
	 * @param args The argument of the invocation.
	 * @param rendezvous The rendezvous object to wait for the call.
	 * @return The invocation result.
	 * @throws Throwable Thrown during invocation.
	 */
	private Object directCall(Method method, Object[] args, Rendezvous rendezvous) throws Throwable {
		Object ret = method.invoke(delegate, args);
		if (rendezvous != null) {
			rendezvous.waiting(timeout);
		}
		return ret;
	}

	/**
	 * Dispatch the method invocation in the dispatching thread, using the rendezvous object to block
	 * the call. If the rendezvous object is null, then just invoke the call in the dispatching thread.
	 *
	 * @param method The method to be invoked.
	 * @param args The argument of the invocation.
	 * @param rendezvous The rendezvous object to wait for the call.
	 * @return The invocation result.
	 * @throws Throwable Thrown during invocation.
	 */
	private Object dispatchCall(final Method method, final Object[] args, final Rendezvous rendezvous) throws Throwable {
		final Object[] returns = new Object[1];
		final Exception[] exceptions = new Exception[1];
		Protocol.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				try {
					returns[0] = method.invoke(delegate, args);
				}
				catch (Exception e) {
					exceptions[0] = e;
				}
			}
		});
		if (rendezvous != null) {
			rendezvous.waiting(timeout);
		}
		if (exceptions[0] != null) throw exceptions[0];
		return returns[0];
	}
}