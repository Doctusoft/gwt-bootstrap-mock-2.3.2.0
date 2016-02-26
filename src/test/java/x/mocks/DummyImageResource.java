package x.mocks;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;

public class DummyImageResource implements ImageResource {

	@Override
	public String getName() {
		return "";
	}

	@Override
	public int getHeight() {
		return 0;
	}

	@Override
	public int getLeft() {
		return 0;
	}

	@Override
	public SafeUri getSafeUri() {
		return UriUtils.fromSafeConstant("");
	}

	@Override
	public int getTop() {
		return 0;
	}

	@Override
	public String getURL() {
		return "";
	}

	@Override
	public int getWidth() {
		return 0;
	}

	@Override
	public boolean isAnimated() {
		return false;
	}

}
