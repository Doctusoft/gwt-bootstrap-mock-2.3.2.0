package x;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.altirnao.aodocs.common.client.ContextData;
import com.altirnao.aodocs.common.shared.AODocsResultList;
import com.altirnao.aodocs.common.shared.CheckInCheckOutMode;
import com.altirnao.aodocs.common.shared.DocumentClassEditorType;
import com.altirnao.aodocs.common.shared.LoadResult;
import com.altirnao.aodocs.common.shared.LoadResult.DummyLoadResult;
import com.altirnao.aodocs.common.shared.PermissionLevel;
import com.altirnao.aodocs.common.shared.UserInfo;
import com.altirnao.aodocs.common.shared.entity.Domain;
import com.altirnao.aodocs.common.shared.entity.Library;
import com.altirnao.aodocs.common.shared.promise.Promise;
import com.altirnao.aodocs.common.shared.service.AuthRemoteService;
import com.altirnao.aodocs.common.shared.vo.CreatableDocumentClassesVO;
import com.altirnao.aodocs.common.shared.vo.DocumentClassVO;
import com.altirnao.aodocs.common.shared.vo.DocumentVO;
import com.altirnao.aodocs.common.shared.vo.LoadDocumentVO;
import com.altirnao.aodocs.feature.rpcbatch.shared.BatchRequest;
import com.altirnao.aodocs.feature.rpcbatch.shared.BatchedRemoteService;
import com.altirnao.aodocs.feature.rpcbatch.shared.LoadRequest;
import com.altirnao.aodocs.feature.rpcbatch.shared.ResultOrException;
import com.altirnao.aodocs.feature.rpcbatch.shared.auditrequest.AuditUserLoginRequest;
import com.altirnao.aodocs.feature.rpcbatch.shared.authrequest.CheckAccessRequest;
import com.altirnao.aodocs.feature.rpcbatch.shared.authrequest.CheckBasicAccessRequest;
import com.altirnao.aodocs.feature.rpcbatch.shared.classrequest.ListCreatableDocumentClassesRequest;
import com.altirnao.aodocs.feature.rpcbatch.shared.documentrequest.LoadSingleDocumentRequest;
import com.altirnao.aodocs.feature.rpcbatch.shared.libraryrequest.GetLibraryRequest;
import com.altirnao.aodocs.feature.rpcbatch.shared.libraryrequest.ListAvailableLibrariesRequest;
import com.altirnao.aodocs.feature.rpcbatch.shared.messagerequest.DynamicMessageVOsWrapper;
import com.altirnao.aodocs.feature.rpcbatch.shared.messagerequest.DynamicMessagesRequest;
import com.altirnao.aodocs.feature.rpcbatch.shared.viewrequest.ListAllViewsRequest;
import com.doctusoft.gwtmock.Document;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.core.shared.GWT.CustomGWTCreateSupplier;
import com.google.gwt.i18n.client.Messages;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.web.bindery.event.shared.binder.EventBinder;

public class BaseGwtJvmTestCase {
	
	protected static UserInfo userInfo = new UserInfo();
	protected static Library testLibrary = new Library("libraryId", "Test Library");
	
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
		GWT.addCustomSupplier(new SchedulerSupplier());
		GWT.addCustomSupplier(new BootstrapSuppliers());
		AuthRemoteService authService = RemoteServiceProxy.getOrCreateRemoteServiceProxy(AuthRemoteService.class).getSpy();
		Mockito.when(authService.checkDomainAccess(Mockito.anyString())).thenReturn(userInfo);
		setupBatch();
	}
	
	private static void setupBatch() throws Exception {
		BatchedRemoteService batchedService = RemoteServiceProxy.getOrCreateRemoteServiceProxy(BatchedRemoteService.class).getSpy();
		testLibrary.setCurrentUserPermissionLevel(PermissionLevel.ADMINISTRATOR);
		Mockito.when(batchedService.executeBatch(Mockito.any(BatchRequest.class))).thenAnswer(new Answer<ArrayList<ResultOrException>>() {
			@Override
			public ArrayList<ResultOrException> answer(InvocationOnMock invocation) throws Throwable {
				BatchRequest batchRequest = invocation.getArgumentAt(0, BatchRequest.class);
				ArrayList<ResultOrException> result = new ArrayList<>();
				for (LoadRequest request : batchRequest.getRequests()) {
					System.out.println(request);
					LoadResult resultValue = null;
					if (request instanceof CheckBasicAccessRequest || request instanceof CheckAccessRequest) {
						resultValue = userInfo;
					}
					if (request instanceof AuditUserLoginRequest) {
						resultValue = DummyLoadResult.DUMMY;
					}
					if (request instanceof ListAllViewsRequest) {
						resultValue = new AODocsResultList<>(new ArrayList<>());
					}
					if (request instanceof ListAvailableLibrariesRequest) {
						resultValue = new AODocsResultList<>(ImmutableList.of(testLibrary));
					}
					if (request instanceof DynamicMessagesRequest) {
						resultValue = DynamicMessageVOsWrapper.builder().dynamicMessageVOs((Map)Maps.newHashMap()).build();
					}
					if (request instanceof ListCreatableDocumentClassesRequest) {
						resultValue = new CreatableDocumentClassesVO(new ArrayList());
					}
					if (request instanceof GetLibraryRequest) {
						resultValue = testLibrary;
					}
					if (request instanceof LoadSingleDocumentRequest) {
						DocumentClassVO classVO = DocumentClassVO.builder().build();
						classVO.setId("classId");
						classVO.setDisplayName("Test Class");
						classVO.setEditorType(DocumentClassEditorType.GWT);
						DocumentVO documentVO = new DocumentVO();
						documentVO.setRichText("");
						documentVO.setClassVO(classVO);
						documentVO.setLastModified(new Date());
						documentVO.setCreationDate(new Date());
						documentVO.setTitle("Test doc");
						documentVO.setKind(DocumentVO.DOCUMENT_KIND);
						documentVO.setLibrary("libraryId");
						documentVO.setCheckInCheckOutMode(CheckInCheckOutMode.NONE);
						documentVO.setCanWrite(true);
						documentVO.setShowClassName(true);
						documentVO.setClassDisplayName("Test Class");
						resultValue = LoadDocumentVO.builder().documentVO(documentVO).build();
					}
					ResultOrException resultOrException = new ResultOrException();
					resultOrException.setResult(resultValue);
					result.add(resultOrException);
				}
				return result;
			}
		});
	}

	@Before
	public void setup() {
		ContextData.AodocsDomain = new Promise<>();
		userInfo.setLocale("en_US");
		userInfo.setEmail("admin@rapps-test.revevol.eu");
		userInfo.setDomain(new Domain("rapps-test.revevol.eu"));
	}

}
