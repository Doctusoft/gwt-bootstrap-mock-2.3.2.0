package x;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.junit.Before;
import org.junit.BeforeClass;

import com.altirnao.aodocs.common.client.ContextData;
import com.altirnao.aodocs.common.shared.promise.Promise;
import com.doctusoft.gwtmock.Document;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.core.shared.GWT.CustomGWTCreateSupplier;
import com.google.gwt.i18n.client.Constants;
import com.google.gwt.i18n.client.Constants.DefaultStringValue;
import com.google.gwt.i18n.client.Messages;
import com.google.gwt.i18n.client.constants.NumberConstants;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.web.bindery.event.shared.binder.EventBinder;

import x.mocks.BatchRequestServletMock;
import x.mocks.BootstrapSuppliers;
import x.mocks.DummyImageResource;
import x.mocks.MockMessages;
import x.mocks.RemoteServiceProxy;
import x.mocks.SchedulerSupplier;

public class BaseGwtJvmTestCase {
	
	protected static BatchRequestServletMock batchRequestServletMock;

	static <T extends CssResource> T proxyCssResource(final Class<T> cls) {
		return (T) Proxy.newProxyInstance(cls.getClassLoader(), new Class [] { cls }, new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				if (CssResource.class.equals(method.getDeclaringClass())) {
					if (method.getName().equals("ensureInjected")) {
						return true;
					}
				}
				if (method.getDeclaringClass().equals(Object.class)) {
					return method.invoke(proxy, args);
				}
				return method.getName();
			}
		});
	}
	
	@BeforeClass
	public static void setupClass() throws Exception {
		Document.Instance.getBody().setId("content");
		GWT.addCustomSupplier(new CustomGWTCreateSupplier() {
			@Override
			public Object create(Class<?> classLiteral) {
				if (EventBinder.class.isAssignableFrom(classLiteral)) {
					return Proxy.newProxyInstance(classLiteral.getClassLoader(), new Class [] { classLiteral }, new InvocationHandler() {
						@Override
						public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
							if (method.getName().equals("bindEventHandlers")) {
								return new com.google.gwt.event.shared.HandlerRegistration() {
									@Override
									public void removeHandler() {
										// no-op
									}
								};
							}
							if (method.getDeclaringClass().equals(Object.class)) {
								return method.invoke(proxy, args);
							}
							throw new UnsupportedOperationException();
						}
					});
				}
				return null;
			}
		});
		GWT.addCustomSupplier(new CustomGWTCreateSupplier() {
			@Override
			public Object create(Class<?> classLiteral) {
				if (ClientBundle.class.isAssignableFrom(classLiteral)) {
					return Proxy.newProxyInstance(classLiteral.getClassLoader(), new Class [] { classLiteral }, new InvocationHandler() {
						@Override
						public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
							if (CssResource.class.isAssignableFrom(method.getReturnType())) {
								return proxyCssResource((Class)method.getReturnType());
							}
							if (ImageResource.class.isAssignableFrom(method.getReturnType())) {
								return new DummyImageResource();
							}
							if (method.getDeclaringClass().equals(Object.class)) {
								return method.invoke(proxy, args);
							}
							throw new UnsupportedOperationException(method.getName());
						}
					});
				}
				return null;
			}
		});
		GWT.addCustomSupplier(new CustomGWTCreateSupplier() {
			@Override
			public Object create(Class<?> classLiteral) {
				if (Messages.class.isAssignableFrom(classLiteral)) {
					return MockMessages.proxyMessages((Class)classLiteral);
				}
				return null;
			}
		});
		GWT.addCustomSupplier(new CustomGWTCreateSupplier() {
			@Override
			public Object create(Class<?> classLiteral) {
				if (RemoteService.class.isAssignableFrom(classLiteral)) {
					return RemoteServiceProxy.getOrCreateRemoteServiceProxy((Class)classLiteral).getProxy();
				}
				return null;
			}
		});
		GWT.addCustomSupplier(new CustomGWTCreateSupplier() {
			@Override
			public Object create(Class<?> classLiteral) {
				if (Constants.class.isAssignableFrom(classLiteral) && !NumberConstants.class.isAssignableFrom(classLiteral)) {
					return Proxy.newProxyInstance(classLiteral.getClassLoader(), new Class [] { classLiteral }, new InvocationHandler() {
						@Override
						public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
							if (method.getDeclaringClass().equals(Object.class)) {
								return method.invoke(proxy, args);
							}
							return method.getAnnotation(DefaultStringValue.class).value();
						}
					});
				}
				return null;
			}
		});
		GWT.addCustomSupplier(new SchedulerSupplier());
		GWT.addCustomSupplier(new BootstrapSuppliers());
		batchRequestServletMock = new BatchRequestServletMock();
	}
	
	@Before
	public void setup() {
		ContextData.AodocsDomain = new Promise<>();
	}

}
