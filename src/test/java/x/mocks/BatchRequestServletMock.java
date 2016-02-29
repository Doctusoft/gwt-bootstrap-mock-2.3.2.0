package x.mocks;

import java.util.ArrayList;
import java.util.Map;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.altirnao.aodocs.common.shared.LoadResult;
import com.altirnao.aodocs.common.shared.ResourceAccessDeniedException;
import com.altirnao.aodocs.feature.rpcbatch.shared.BatchRequest;
import com.altirnao.aodocs.feature.rpcbatch.shared.BatchedRemoteService;
import com.altirnao.aodocs.feature.rpcbatch.shared.LoadRequest;
import com.altirnao.aodocs.feature.rpcbatch.shared.ResultOrException;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;

import lombok.Getter;

public class BatchRequestServletMock {
	
	protected Map<Class<?>, Supplier<Object>> responseSuppliers = Maps.newHashMap();
	
	@Getter
	protected LoadRequestMonitor loadRequestMonitor = Mockito.spy(LoadRequestMonitor.class); 
	
	public BatchRequestServletMock() {
		BatchedRemoteService batchedService = RemoteServiceProxy.getOrCreateRemoteServiceProxy(BatchedRemoteService.class).getSpy();
		try {
			Mockito.when(batchedService.executeBatch(Mockito.any(BatchRequest.class))).thenAnswer(new Answer<ArrayList<ResultOrException>>() {
				@Override
				public ArrayList<ResultOrException> answer(InvocationOnMock invocation) throws Throwable {
					BatchRequest batchRequest = invocation.getArgumentAt(0, BatchRequest.class);
					ArrayList<ResultOrException> result = new ArrayList<>();
					for (LoadRequest request : batchRequest.getRequests()) {
						System.out.println(request);
						loadRequestMonitor.loadRequestExecuted(request);
						Supplier<?> supplier = responseSuppliers.get(request.getClass());
						if (supplier == null)
							throw new RuntimeException("No supplier for: " + request);
						LoadResult resultValue = (LoadResult) supplier.get();
						ResultOrException resultOrException = new ResultOrException();
						resultOrException.setResult(resultValue);
						result.add(resultOrException);
					}
					return result;
				}
			});
		} catch (ResourceAccessDeniedException e) {
			throw new RuntimeException();
		}
	}

	public <T extends LoadResult> void registerSupplier(Class<? extends LoadRequest<T>> requestClass, Supplier<T> supplier) {
		responseSuppliers.put((Class) requestClass, (Supplier) supplier);
	}
	
	public interface LoadRequestMonitor {
		public void loadRequestExecuted(LoadRequest loadRequest);
	}
}
