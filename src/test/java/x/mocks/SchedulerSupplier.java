package x.mocks;

import com.google.gwt.core.client.impl.SchedulerImpl;
import com.google.gwt.core.shared.GWT.CustomGWTCreateSupplier;

public class SchedulerSupplier implements CustomGWTCreateSupplier {

	@Override
	public Object create(Class<?> classLiteral) {
		if (classLiteral.equals(SchedulerImpl.class)) {
			return new SchedulerImpl() {
				
			};
		}
		return null;
	}

}
