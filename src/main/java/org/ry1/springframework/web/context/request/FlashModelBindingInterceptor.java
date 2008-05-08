package org.ry1.springframework.web.context.request;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/** Binds all entries in a flash to the model.
 * @author rberdeen
 */
public class FlashModelBindingInterceptor implements HandlerInterceptor {
	private FlashMap flashMap;
	private boolean discardIfNotRedirected = true;
	public void setFlashMap(FlashMap flashMap) {
		this.flashMap = flashMap;
	}
	
	public void setDiscardIfNotRedirected(boolean discardIfNotRedirected) {
		this.discardIfNotRedirected = discardIfNotRedirected;
	}
	
	public void setClearModelOnRedirect(boolean clearModelOnRedirect) {
	}
	
	public void setBindOnRedirect(boolean bindOnRedirect) {
	}

	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		boolean isRedirect = response.containsHeader("Location");
		
		if (!isRedirect && discardIfNotRedirected) {
			flashMap.discard();
		}
	}

	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		if (modelAndView != null) {
			modelAndView.addAllObjects(flashMap);
		}
	}

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		return true;
	}

}
