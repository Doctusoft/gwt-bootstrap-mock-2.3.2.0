package x;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.altirnao.aodocs.common.shared.HistoryUtils;
import com.altirnao.aodocs.feature.rpcbatch.client.RequestHelper;
import com.altirnao.aodocs.feature.rpcbatch.shared.documentrequest.ListDocumentsByViewRequest;
import com.altirnao.aodocs.view.client.view.DocumentListView;
import com.altirnao.aodocs.view.client.view.DocumentViewer;
import com.altirnao.aodocs.view.client.view.SingleDocumentView;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.impl.SchedulerImpl;
import com.google.gwt.user.client.Window;

import x.data.DataContextBuilder;
import x.mocks.BatchRequestServletMock.LoadRequestMonitor;

public class TestViewer extends BaseGwtJvmTestCase {
	
	@Test
	public void testLibraryChooserPageLoads() throws Exception {
		new DataContextBuilder().basicLibrary().basicClass().basicUser().apply(batchRequestServletMock);
//		Window.Location.setQueryString("&locale=en_US&aodocs-domain=rapps-test.revevol.eu");
//		Mockito.when(RemoteServiceProxy.getOrCreateRemoteServiceProxy(XsrfTokenService.class).getSpy().getNewXsrfToken()).thenReturn(new XsrfToken("xx"));
		new DocumentViewer().onModuleLoad();
		((SchedulerImpl)Scheduler.get()).executeDeferredCommands();
//		Document.Instance.printFormatted(new PrintWriter(System.out));
	}

	@Test
	public void testSingleDocViewEditButton() {
		new DataContextBuilder().basicLibrary().basicClass().basicUser().simpleDocument().apply(batchRequestServletMock);
		Window.Location.setHash("Menu_viewDoc/LibraryId_libraryId/DocumentId_documentId");
		DocumentViewer documentViewer = new DocumentViewer();
		documentViewer.onModuleLoad();
		((SchedulerImpl)Scheduler.get()).executeDeferredCommands();
		SingleDocumentView singleDocumentView = (SingleDocumentView) documentViewer.getPageMap().get(HistoryUtils.MENU_ITEM_HISTORY_PREFIX + HistoryUtils.DOCUMENT_VIEW_PAGE);
		Window.mockableWindow = Mockito.spy(Window.mockableWindow);
		singleDocumentView.getHeaderPanel().getEditButton().click();
		Mockito.verify(Window.mockableWindow).open(Mockito.eq("/editor.html#Menu_configeditDoc/LibraryId_libraryId/DocumentId_documentId/From_http:*2F*2Flocalhost*2FdocumentViewer.html#Menu_viewDoc*2FLibraryId_libraryId*2FDocumentId_documentId"), Mockito.eq("_top"), (String) Mockito.isNull());
	}

	@Test
	public void testCheckoutButtonVisibleInCICOMode() {
		new DataContextBuilder().basicLibrary().basicClass().basicUser().simpleDocument().enableCICO().apply(batchRequestServletMock);
		Window.Location.setHash("Menu_viewDoc/LibraryId_libraryId/DocumentId_documentId");
		DocumentViewer documentViewer = new DocumentViewer();
		documentViewer.onModuleLoad();
		((SchedulerImpl)Scheduler.get()).executeDeferredCommands();
		SingleDocumentView singleDocumentView = (SingleDocumentView) documentViewer.getPageMap().get(HistoryUtils.MENU_ITEM_HISTORY_PREFIX + HistoryUtils.DOCUMENT_VIEW_PAGE);
		Assert.assertTrue(singleDocumentView.getHeaderPanel().getCheckOutButton().getButton().isVisible());
	}

	@Test
	public void testCheckoutButtonHiddenIfTheDocHasDraftVersion() {
		DataContextBuilder builder = new DataContextBuilder().basicLibrary().basicClass().basicUser().simpleDocument().enableCICO().apply(batchRequestServletMock);
		builder.documentVO.setHasDraftVersion(true);
		builder.documentVO.setCheckedOutBy(builder.userInfo.getEmail());
		Window.Location.setHash("Menu_viewDoc/LibraryId_libraryId/DocumentId_documentId");
		DocumentViewer documentViewer = new DocumentViewer();
		documentViewer.onModuleLoad();
		((SchedulerImpl)Scheduler.get()).executeDeferredCommands();
		SingleDocumentView singleDocumentView = (SingleDocumentView) documentViewer.getPageMap().get(HistoryUtils.MENU_ITEM_HISTORY_PREFIX + HistoryUtils.DOCUMENT_VIEW_PAGE);
		Assert.assertFalse(singleDocumentView.getHeaderPanel().getCheckOutButton().getButton().isVisible());
	}
	
	@Test
	public void testDocListViewSearchSendsTheRPCRequest() throws Exception {
		DataContextBuilder builder = new DataContextBuilder().basicLibrary().basicClass().basicUser().basicView().apply(batchRequestServletMock);
		Window.Location.setHash("Menu_listDoc/LibraryId_libraryId/ViewId_viewId");
		DocumentViewer documentViewer = new DocumentViewer();
		documentViewer.onModuleLoad();
		((SchedulerImpl)Scheduler.get()).executeDeferredCommands();
		DocumentListView docListView = (DocumentListView) documentViewer.getPageMap().get(HistoryUtils.MENU_ITEM_HISTORY_PREFIX + HistoryUtils.DOCUMENT_LIST_PAGE);
		RequestHelper.invalidate(ListDocumentsByViewRequest.class);
		docListView.getSearchPanel().getSearchTextBox().setValue("x");
		docListView.getSearchPanel().getSearchButton().click();
		((SchedulerImpl)Scheduler.get()).executeDeferredCommands();
		LoadRequestMonitor monitor = batchRequestServletMock.getLoadRequestMonitor();
		Mockito.inOrder(monitor).verify(monitor, Mockito.times(2)).loadRequestExecuted(Mockito.isA(ListDocumentsByViewRequest.class));
	}
}
