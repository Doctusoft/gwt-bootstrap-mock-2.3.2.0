package x;

import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.altirnao.aodocs.common.shared.AODocsResultList;
import com.altirnao.aodocs.common.shared.LoadResult;
import com.altirnao.aodocs.common.shared.LoadResult.DummyLoadResult;
import com.altirnao.aodocs.common.shared.UserInfo;
import com.altirnao.aodocs.common.shared.entity.Domain;
import com.altirnao.aodocs.common.shared.entity.Library;
import com.altirnao.aodocs.common.shared.service.AuthRemoteService;
import com.altirnao.aodocs.feature.rpcbatch.shared.BatchRequest;
import com.altirnao.aodocs.feature.rpcbatch.shared.BatchedRemoteService;
import com.altirnao.aodocs.feature.rpcbatch.shared.LoadRequest;
import com.altirnao.aodocs.feature.rpcbatch.shared.ResultOrException;
import com.altirnao.aodocs.feature.rpcbatch.shared.auditrequest.AuditUserLoginRequest;
import com.altirnao.aodocs.feature.rpcbatch.shared.authrequest.CheckBasicAccessRequest;
import com.altirnao.aodocs.feature.rpcbatch.shared.libraryrequest.ListAvailableLibrariesRequest;
import com.altirnao.aodocs.feature.rpcbatch.shared.viewrequest.ListAllViewsRequest;
import com.altirnao.aodocs.view.client.view.DocumentViewer;
import com.doctusoft.gwtmock.Document;
import com.google.common.collect.ImmutableList;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.impl.SchedulerImpl;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.core.shared.GWT.CustomGWTCreateSupplier;
import com.google.gwt.i18n.client.Messages;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.web.bindery.event.shared.binder.EventBinder;

//@RunWith(GwtMockTestRunner.class)
public class TestViewer {
	
	static <T extends CssResource> T proxyCssResource(final Class<T> cls) {
		return (T) Proxy.newProxyInstance(cls.getClassLoader(), new Class [] { cls }, new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				if (CssResource.class.equals(method.getDeclaringClass())) {
					if (method.getName().equals("ensureInjected")) {
						return true;
					}
				}
				if (method.getDeclaringClass().equals(cls)) {
					return method.getName();
				}
				if (method.getDeclaringClass().equals(Object.class)) {
					return method.invoke(proxy, args);
				}
				throw new UnsupportedOperationException();
			}
		});
	}
	
	@Before
	public void setup() {
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
		GWT.addCustomSupplier(new SchedulerSupplier());
		GWT.addCustomSupplier(new BootstrapSuppliers());
	}
	
	@Test
	public void testViewer() throws Exception {
//		Window.Location.setQueryString("&locale=en_US&aodocs-domain=rapps-test.revevol.eu");
//		Mockito.when(RemoteServiceProxy.getOrCreateRemoteServiceProxy(XsrfTokenService.class).getSpy().getNewXsrfToken()).thenReturn(new XsrfToken("xx"));
		AuthRemoteService authService = RemoteServiceProxy.getOrCreateRemoteServiceProxy(AuthRemoteService.class).getSpy();
		BatchedRemoteService batchedService = RemoteServiceProxy.getOrCreateRemoteServiceProxy(BatchedRemoteService.class).getSpy();
		final UserInfo userInfo = new UserInfo();
		userInfo.setLocale("en_US");
		userInfo.setEmail("admin@rapps-test.revevol.eu");
		userInfo.setDomain(new Domain("rapps-test.revevol.eu"));
		Mockito.when(authService.checkDomainAccess(Mockito.anyString())).thenReturn(userInfo);
		Mockito.when(batchedService.executeBatch(Mockito.any(BatchRequest.class))).thenAnswer(new Answer<ArrayList<ResultOrException>>() {
			@Override
			public ArrayList<ResultOrException> answer(InvocationOnMock invocation) throws Throwable {
				BatchRequest batchRequest = invocation.getArgumentAt(0, BatchRequest.class);
				ArrayList<ResultOrException> result = new ArrayList<>();
				for (LoadRequest request : batchRequest.getRequests()) {
					System.out.println(request);
					LoadResult resultValue = null;
					if (request instanceof CheckBasicAccessRequest) {
						resultValue = userInfo;
					}
					if (request instanceof AuditUserLoginRequest) {
						resultValue = DummyLoadResult.DUMMY;
					}
					if (request instanceof ListAllViewsRequest) {
						resultValue = new AODocsResultList<>(new ArrayList<>());
					}
					if (request instanceof ListAvailableLibrariesRequest) {
						Library library = new Library("asd", "asd");
						resultValue = new AODocsResultList<>(ImmutableList.of(library));
					}
					ResultOrException resultOrException = new ResultOrException();
					resultOrException.setResult(resultValue);
					result.add(resultOrException);
				}
				return result;
			}
		});
		new DocumentViewer().onModuleLoad();
		((SchedulerImpl)Scheduler.get()).executeDeferredCommands();
		Document.Instance.printFormatted(new PrintWriter(System.out));
	}

}
