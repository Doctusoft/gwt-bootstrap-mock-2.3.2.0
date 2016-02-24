package x;

import java.io.PrintWriter;

import org.junit.Test;
import org.mockito.Mockito;

import com.altirnao.aodocs.common.shared.HistoryUtils;
import com.altirnao.aodocs.view.client.view.DocumentViewer;
import com.altirnao.aodocs.view.client.view.SingleDocumentView;
import com.doctusoft.gwtmock.Document;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.impl.SchedulerImpl;
import com.google.gwt.user.client.Window;

public class TestViewer extends BaseGwtJvmTestCase {
	
	@Test
	public void testLibraryChooserPageLoads() throws Exception {
//		Window.Location.setQueryString("&locale=en_US&aodocs-domain=rapps-test.revevol.eu");
//		Mockito.when(RemoteServiceProxy.getOrCreateRemoteServiceProxy(XsrfTokenService.class).getSpy().getNewXsrfToken()).thenReturn(new XsrfToken("xx"));
		new DocumentViewer().onModuleLoad();
		((SchedulerImpl)Scheduler.get()).executeDeferredCommands();
//		Document.Instance.printFormatted(new PrintWriter(System.out));
	}

	@Test
	public void testSingleDocViewEditButton() {
		Window.Location.setHash("Menu_viewDoc/LibraryId_libraryId/DocumentId_documentId");
		DocumentViewer documentViewer = new DocumentViewer();
		documentViewer.onModuleLoad();
		((SchedulerImpl)Scheduler.get()).executeDeferredCommands();
		Document.Instance.printFormatted(new PrintWriter(System.out));
		SingleDocumentView singleDocumentView = (SingleDocumentView) documentViewer.getPageMap().get(HistoryUtils.MENU_ITEM_HISTORY_PREFIX + HistoryUtils.DOCUMENT_VIEW_PAGE);
		Window.mockableWindow = Mockito.spy(Window.mockableWindow);
		singleDocumentView.getHeaderPanel().getEditButton().click();
		Mockito.verify(Window.mockableWindow).open(Mockito.eq("/editor.html#Menu_configeditDoc/LibraryId_libraryId/DocumentId_documentId/From_http:*2F*2Flocalhost*2FdocumentViewer.html#Menu_viewDoc*2FLibraryId_libraryId*2FDocumentId_documentId"), Mockito.eq("_top"), (String) Mockito.isNull());
	}

}
