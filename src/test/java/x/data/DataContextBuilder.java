package x.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.mockito.Mockito;

import com.altirnao.aodocs.common.shared.AODocsResultList;
import com.altirnao.aodocs.common.shared.CheckInCheckOutMode;
import com.altirnao.aodocs.common.shared.DocumentClassEditorType;
import com.altirnao.aodocs.common.shared.DocumentPagingLoadResultImpl;
import com.altirnao.aodocs.common.shared.LoadResult.DummyLoadResult;
import com.altirnao.aodocs.common.shared.PermissionLevel;
import com.altirnao.aodocs.common.shared.ResourceAccessDeniedException;
import com.altirnao.aodocs.common.shared.SimplePagingLoadResult;
import com.altirnao.aodocs.common.shared.UserInfo;
import com.altirnao.aodocs.common.shared.entity.Domain;
import com.altirnao.aodocs.common.shared.entity.Library;
import com.altirnao.aodocs.common.shared.entity.View;
import com.altirnao.aodocs.common.shared.service.AuthRemoteService;
import com.altirnao.aodocs.common.shared.vo.CreatableDocumentClassesVO;
import com.altirnao.aodocs.common.shared.vo.DocumentClassVO;
import com.altirnao.aodocs.common.shared.vo.DocumentVO;
import com.altirnao.aodocs.common.shared.vo.DocumentViewVO;
import com.altirnao.aodocs.common.shared.vo.FieldDefinitionVO;
import com.altirnao.aodocs.common.shared.vo.FolderAndDocumentListVO;
import com.altirnao.aodocs.common.shared.vo.LoadDocumentVO;
import com.altirnao.aodocs.common.shared.vo.SimpleDocumentClassWithMandatoryCheckVO;
import com.altirnao.aodocs.common.shared.vo.ViewListWithFolderVisibilityVO;
import com.altirnao.aodocs.feature.rpcbatch.shared.auditrequest.AuditUserLoginRequest;
import com.altirnao.aodocs.feature.rpcbatch.shared.authrequest.CheckAccessRequest;
import com.altirnao.aodocs.feature.rpcbatch.shared.authrequest.CheckBasicAccessRequest;
import com.altirnao.aodocs.feature.rpcbatch.shared.classrequest.ListCreatableDocumentClassesRequest;
import com.altirnao.aodocs.feature.rpcbatch.shared.documentrequest.ListDocumentsByViewRequest;
import com.altirnao.aodocs.feature.rpcbatch.shared.documentrequest.LoadSingleDocumentRequest;
import com.altirnao.aodocs.feature.rpcbatch.shared.libraryrequest.GetLibraryRequest;
import com.altirnao.aodocs.feature.rpcbatch.shared.libraryrequest.ListAvailableLibrariesRequest;
import com.altirnao.aodocs.feature.rpcbatch.shared.messagerequest.DynamicMessageVOsWrapper;
import com.altirnao.aodocs.feature.rpcbatch.shared.messagerequest.DynamicMessagesRequest;
import com.altirnao.aodocs.feature.rpcbatch.shared.viewrequest.ListAllViewsRequest;
import com.altirnao.aodocs.feature.rpcbatch.shared.viewrequest.ListViewWithOpenDriveRequest;
import com.altirnao.aodocs.feature.rpcbatch.shared.viewrequest.LoadDocumentViewDataRequest;
import com.altirnao.aodocs.feature.search.shared.SearchProblem;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import x.mocks.BatchRequestServletMock;
import x.mocks.RemoteServiceProxy;

public class DataContextBuilder {

	public UserInfo userInfo;
	public Library library;
	public DocumentClassVO classVO;
	public DocumentVO documentVO;
	public View view;
	public DocumentViewVO documentViewVO;
	
	public DataContextBuilder basicUser() {
		userInfo = new UserInfo();
		userInfo.setLocale("en_US");
		userInfo.setEmail("admin@rapps-test.revevol.eu");
		userInfo.setDomain(new Domain("rapps-test.revevol.eu"));
		return this;
	}
	
	public DataContextBuilder basicLibrary() {
		library = new Library("libraryId", "Test Library");
		library.setCurrentUserPermissionLevel(PermissionLevel.ADMINISTRATOR);
		library.setRootFolderId("0");
		return this;
	}
	
	public DataContextBuilder basicClass() {
		classVO = DocumentClassVO.builder().build();
		classVO.setId("classId");
		classVO.setDisplayName("Test Class");
		classVO.setEditorType(DocumentClassEditorType.GWT);
		classVO.setMetafields(new ArrayList<FieldDefinitionVO>());
		return this;
	}
	
	public DataContextBuilder simpleDocument() {
		Preconditions.checkNotNull(library);
		Preconditions.checkNotNull(classVO);
		documentVO = new DocumentVO();
		documentVO.setRichText("");
		documentVO.setClassVO(classVO);
		documentVO.setLastModified(new Date());
		documentVO.setCreationDate(new Date());
		documentVO.setTitle("Test doc");
		documentVO.setKind(DocumentVO.DOCUMENT_KIND);
		documentVO.setLibrary("libraryId");
		documentVO.setCheckInCheckOutMode(CheckInCheckOutMode.NONE);
		documentVO.setCanWrite(true);
		documentVO.setWriteAccessDisregardingTheCheckOutState(true);
		documentVO.setShowClassName(true);
		documentVO.setClassDisplayName("Test Class");
		return this;
	}
	
	public DataContextBuilder basicView() {
		Preconditions.checkNotNull(classVO);
		view = new View("viewId");
		view.setClassName(classVO.getDisplayName());
		view.setDisplayColumns(ImmutableList.of("Title"));
		documentViewVO = DocumentViewVO.builder().view(view).classVO(classVO).build();
		documentViewVO.setColumnMapping(new HashMap(ImmutableMap.<String, String>of("Title", "Title")));
		return this;
	}
	
	public DataContextBuilder enableCICO() {
		Preconditions.checkNotNull(documentVO);
		documentVO.setCheckInCheckOutMode(CheckInCheckOutMode.SIMPLE);
		return this;
	}

	public DataContextBuilder apply(BatchRequestServletMock batchMock) {
		try {
			AuthRemoteService authService = RemoteServiceProxy.getOrCreateRemoteServiceProxy(AuthRemoteService.class).getSpy();
			Mockito.when(authService.checkDomainAccess(Mockito.anyString())).thenReturn(userInfo);
			Supplier<UserInfo> userInfoSupplier = new Supplier<UserInfo>() {
				@Override
				public UserInfo get() {
					return userInfo;
				}
			};
			batchMock.registerSupplier(CheckAccessRequest.class, userInfoSupplier);
			batchMock.registerSupplier(CheckBasicAccessRequest.class, userInfoSupplier);
			batchMock.registerSupplier(AuditUserLoginRequest.class, new Supplier<DummyLoadResult>() {
				@Override
				public DummyLoadResult get() {
					return (DummyLoadResult) DummyLoadResult.DUMMY;
				}
			});
			batchMock.registerSupplier(ListAllViewsRequest.class, new Supplier<AODocsResultList<View>>() {
				@Override
				public AODocsResultList<View> get() {
					return new AODocsResultList<View>(new ArrayList<View>());
				}
			});
			batchMock.registerSupplier(ListAvailableLibrariesRequest.class, new Supplier<AODocsResultList<Library>>() {
				@Override
				public AODocsResultList<Library> get() {
					return new AODocsResultList<>(ImmutableList.of(library));
				}
			});
			batchMock.registerSupplier(DynamicMessagesRequest.class, new Supplier<DynamicMessageVOsWrapper>() {
				@Override
				public DynamicMessageVOsWrapper get() {
					return DynamicMessageVOsWrapper.builder().dynamicMessageVOs((Map)Maps.newHashMap()).build();
				}
			});
			batchMock.registerSupplier(ListCreatableDocumentClassesRequest.class, new Supplier<CreatableDocumentClassesVO>() {
				@Override
				public CreatableDocumentClassesVO get() {
					return new CreatableDocumentClassesVO(ImmutableList.<SimpleDocumentClassWithMandatoryCheckVO>of());
				}
			});
			batchMock.registerSupplier(GetLibraryRequest.class, new Supplier<Library>() {
				@Override
				public Library get() {
					return library;
				}
			});
			batchMock.registerSupplier(LoadSingleDocumentRequest.class, new Supplier<LoadDocumentVO>() {
				@Override
				public LoadDocumentVO get() {
					return LoadDocumentVO.builder().documentVO(documentVO).build();
				}
			});
			batchMock.registerSupplier(ListViewWithOpenDriveRequest.class, new Supplier<ViewListWithFolderVisibilityVO>() {
				@Override
				public ViewListWithFolderVisibilityVO get() {
					ViewListWithFolderVisibilityVO viewList = new ViewListWithFolderVisibilityVO();
					viewList.setViewList(new ArrayList<View>());
					return viewList;
				}
			});
			batchMock.registerSupplier(LoadDocumentViewDataRequest.class, new Supplier<DocumentViewVO>() {
				@Override
				public DocumentViewVO get() {
					return documentViewVO;
				}
			});
			batchMock.registerSupplier(ListDocumentsByViewRequest.class, new Supplier<SimplePagingLoadResult<FolderAndDocumentListVO>>() {
				@Override
				public SimplePagingLoadResult<FolderAndDocumentListVO> get() {
					SimplePagingLoadResult<FolderAndDocumentListVO> result =
						new DocumentPagingLoadResultImpl(new ArrayList<FolderAndDocumentListVO>(), 0, 0, true, null, new HashSet<SearchProblem>());
					return result;
				}
			});
		} catch (ResourceAccessDeniedException e) {
			throw new RuntimeException();
		}
		return this;
	}
}
