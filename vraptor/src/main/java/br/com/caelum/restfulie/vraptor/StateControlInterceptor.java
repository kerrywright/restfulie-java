package br.com.caelum.restfulie.vraptor;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import br.com.caelum.vraptor.InterceptionException;
import br.com.caelum.vraptor.core.InterceptorStack;
import br.com.caelum.vraptor.core.RequestInfo;
import br.com.caelum.vraptor.core.Routes;
import br.com.caelum.vraptor.interceptor.Interceptor;
import br.com.caelum.vraptor.ioc.RequestScoped;
import br.com.caelum.vraptor.resource.HttpMethod;
import br.com.caelum.vraptor.resource.ResourceMethod;
import br.com.caelum.vraptor.rest.Restfulie;
import br.com.caelum.vraptor.rest.StateResource;
import br.com.caelum.vraptor.view.Status;

/**
 * Intercepts invocations to state control's intercepted controllers.
 * 
 * @author guilherme silveira
 * @author pedro mariano
 */
@SuppressWarnings("unchecked")
@RequestScoped
public class StateControlInterceptor<T extends StateResource> implements Interceptor {

	private final StateControl<T> control;
	private final List<Class> controllers;
	private final Status status;
	private final Restfulie restfulie;
	private final Routes routes;
	private final RequestInfo info;
	private final ParameterizedTypeSearcher searcher = new ParameterizedTypeSearcher();

	public StateControlInterceptor(StateControl<T> control, Restfulie restfulie, Status status, RequestInfo info, Routes routes) {
		this.control = control;
		this.restfulie = restfulie;
		this.status = status;
		this.info = info;
		this.routes = routes;
		this.controllers = Arrays.asList(control.getControllers());
	}

	public boolean accepts(ResourceMethod method) {
		return controllers.contains(method.getResource().getType()) && method.getMethod().isAnnotationPresent(Transition.class);
	}

	public void intercept(InterceptorStack stack, ResourceMethod method,
			Object instance) throws InterceptionException {
		ParameterizedType type = searcher.search(control.getClass());
		if(analyzeImplementation(method,type)) {
			stack.next(method, instance);
		}
	}

	private boolean analyzeImplementation(ResourceMethod method,
			ParameterizedType parameterized) {
		Type parameterType = parameterized.getActualTypeArguments()[0];
		Class found = (Class) parameterType;
		T resource = retrieveResource(found);
		if(resource==null) {
			status.notFound();
			return false;
		}
		if(allows(resource, method.getMethod())) {
			return true;
		}
		status.methodNotAllowed(allowedMethods());
		return false;
	}

	private EnumSet<HttpMethod> allowedMethods() {
		EnumSet<HttpMethod> allowed = routes.allowedMethodsFor(info.getRequestedUri());
		allowed.remove(HttpMethod.of(info.getRequest()));
		return allowed;
	}


	private T retrieveResource(Class found) {
		String parameterName = lowerFirstChar(found.getSimpleName()) + ".id";
		String id = info.getRequest().getParameter(parameterName);
		T resource = control.retrieve(id);
		return resource;
	}

	private boolean allows(T resource, Method method) {
		try {
			List<br.com.caelum.vraptor.rest.Transition> transitions = resource.getFollowingTransitions(restfulie);
			for (br.com.caelum.vraptor.rest.Transition transition : transitions) {
				if(transition.matches(method)) {
					return true;
				}
			}
			return false;
		} finally {
			restfulie.clear();
		}
	}

	private String lowerFirstChar(String simpleName) {
		if(simpleName.length()==1) {
			return simpleName.toLowerCase();
		}
		return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
	}

}
