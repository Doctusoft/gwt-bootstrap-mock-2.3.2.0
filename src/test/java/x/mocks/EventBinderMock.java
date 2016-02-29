package x.mocks;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;
import com.google.web.bindery.event.shared.binder.GenericEvent;
import com.google.web.bindery.event.shared.binder.impl.GenericEventHandler;
import com.google.web.bindery.event.shared.binder.impl.GenericEventType;

import lombok.Getter;

public class EventBinderMock<Target, Binder extends EventBinder<Target>> {

	@Getter
	private Binder proxy;
	
	public EventBinderMock(Class<Binder> binderClass) {
		proxy = (Binder) Proxy.newProxyInstance(binderClass.getClassLoader(), new Class [] { binderClass }, new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				if (method.getName().equals("bindEventHandlers")) {
					bindEventHandlers((Target) args[0], (EventBus) args[1]);
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

	public void bindEventHandlers(final Target target, EventBus eventBus) {
		for (final Method method : target.getClass().getMethods()) {
			if (method.isAnnotationPresent(EventHandler.class)) {
				method.setAccessible(true);
				Class<?> eventClass = method.getParameterTypes()[0];
				eventBus.addHandler(GenericEventType.getTypeOf((Class)eventClass), new GenericEventHandler() {
					@Override
					public void handleEvent(GenericEvent event) {
						try {
							method.invoke(target, event);
						} catch (Exception e) {
							throw new RuntimeException("exception invoking event handler: " + method, e);
						}
					}
				});
			}
		}
	}
}
