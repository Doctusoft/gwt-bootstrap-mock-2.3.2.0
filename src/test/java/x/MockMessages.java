package x;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.google.gwt.i18n.client.Messages;
import com.google.gwt.i18n.client.Messages.DefaultMessage;

public class MockMessages {
	
	public static <T extends Messages> T proxyMessages(final Class<T> msgClass) {
		return (T) Proxy.newProxyInstance(msgClass.getClassLoader(), new Class [] { msgClass }, new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				if (method.getDeclaringClass().equals(msgClass)) {
					return method.getAnnotation(DefaultMessage.class).value();
				}
				if (method.getDeclaringClass().equals(Object.class)) {
					return method.invoke(proxy, args);
				}
				throw new UnsupportedOperationException(method.getName());
			}
		});
	}

}
