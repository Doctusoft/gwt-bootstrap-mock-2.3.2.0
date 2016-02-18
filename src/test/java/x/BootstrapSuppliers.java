package x;

import com.github.gwtbootstrap.client.ui.config.ColumnSizeConfigurator;
import com.google.gwt.core.shared.GWT.CustomGWTCreateSupplier;

public class BootstrapSuppliers implements CustomGWTCreateSupplier {

	@Override
	public Object create(Class<?> classLiteral) {
		if (ColumnSizeConfigurator.class.equals(classLiteral)) {
			return new ColumnSizeConfigurator() {
				@Override
				public int getMinimumSpanSize() {
					return 1;
				}
				
				@Override
				public int getMinimumOffsetSize() {
					return 1;
				}
				
				@Override
				public int getMaximumSpanSize() {
					return 10;
				}
				
				@Override
				public int getMaximumOffsetSize() {
					return 10;
				}
			};
		}
		return null;
	}

}
