package org.ry1.springframework.web.context.request;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

public class FlashMap implements Map<String, Object> {
	private final String attributePrefix = FlashMap.class.getName() + "#" + System.identityHashCode(this) + '.';
	private final String mapAttributeName = attributePrefix + "map";
	private final String keptAttributeName = attributePrefix + "kept";
	private final String keysToKeepAttributeName = attributePrefix + "keep";
	private final String onRequestEndCallbackAttributeName = attributePrefix + "callback";
	
	private boolean keepOnPut = true;
	
	public void setKeepOnPut(boolean keepOnPut) {
		this.keepOnPut = keepOnPut;
	}
	
	/** Moves kept attributes to session at end of request. */
	private final Runnable REQUEST_END_CALLBACK = new Runnable(){
		@SuppressWarnings("unchecked")
		public void run() {
			RequestAttributes requestAttributes = getRequestAttributes();
			HashMap<String, Object> map = getMap();
			HashSet<String> keysToKeep = getKeysToKeep();
			if (keysToKeep.size() > 0) {
				HashMap<String, Object> kept;
				synchronized (requestAttributes.getSessionMutex()) {
					kept = (HashMap<String, Object>) requestAttributes.getAttribute(keptAttributeName, RequestAttributes.SCOPE_GLOBAL_SESSION);
					if (kept == null) {
						kept = new HashMap<String, Object>();
						requestAttributes.setAttribute(keptAttributeName, kept, RequestAttributes.SCOPE_GLOBAL_SESSION);
					}
				}
				for (String key : keysToKeep) {
					Object value = map.get(key);
					kept.put(key, value);
				}
			}
		}
	};
	
	private final RequestAttributes getRequestAttributes() {
		return RequestContextHolder.getRequestAttributes();
	}
	
	/** Returns the map for the current request. The first time this method is called for a particular request,
	 * the map is retrieved from the session. If no map exists in the session, a new one is created for this
	 * request.
	 */
	@SuppressWarnings("unchecked")
	private final HashMap<String, Object> getMap() {
		RequestAttributes requestAttributes = getRequestAttributes();
		HashMap<String, Object> result = (HashMap<String, Object>) requestAttributes.getAttribute(mapAttributeName, RequestAttributes.SCOPE_REQUEST);
		
		if (result == null) {
			synchronized (requestAttributes.getSessionMutex()) {
				result = (HashMap<String, Object>) requestAttributes.getAttribute(keptAttributeName, RequestAttributes.SCOPE_GLOBAL_SESSION);
				if (result != null) {
					requestAttributes.removeAttribute(keptAttributeName, RequestAttributes.SCOPE_GLOBAL_SESSION);
				}
				else {
					result = new HashMap<String, Object>();
				}
				requestAttributes.setAttribute(mapAttributeName, result, RequestAttributes.SCOPE_REQUEST);
			}
		}
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private final HashSet<String> getKeysToKeep() {
		RequestAttributes requestAttributes = getRequestAttributes();
		HashSet<String> result = (HashSet<String>) requestAttributes.getAttribute(keysToKeepAttributeName, RequestAttributes.SCOPE_REQUEST);
		if (result == null) {
			result = new HashSet<String>();
			requestAttributes.setAttribute(keysToKeepAttributeName, result, RequestAttributes.SCOPE_REQUEST);
		}
		return result;
	}
	
	public void clear() {
		getKeysToKeep().clear();
		getMap().clear();
	}

	public boolean containsKey(Object key) {
		return getMap().containsKey(key);
	}

	public boolean containsValue(Object value) {
		return getMap().containsValue(value);
	}

	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		return getMap().entrySet();
	}

	public Object get(Object key) {
		return getMap().get(key);
	}

	public boolean isEmpty() {
		return getMap().isEmpty();
	}

	public Set<String> keySet() {
		return getMap().keySet();
	}

	/** Sets a flash that will be kept available to the next request.
	 */
	public Object put(String key, Object value) {
		if (keepOnPut) {
			keep(key);
		}
		return getMap().put(key, value);
	}

	/** Set all flash entries to be kept available to the next request.
	 */
	public void putAll(Map<? extends String, ? extends Object> t) {
		if (keepOnPut) {
			getKeysToKeep().addAll(t.keySet());
			registerCallback();
		}
		getMap().putAll(t);
	}
	
	/** Keeps the entire current flash available to the next request.
	 */
	public void keep() {
		getKeysToKeep().addAll(getMap().keySet());
	}
	
	/** Keeps a specific flash entry available to the next request.
	 */
	public void keep(String key) {
		getKeysToKeep().add(key);
		registerCallback();
	}
	
	/** Sets a flash that will not be available to the next request, only to the current.
	 */
	public void now(String key, Object value) {
		getMap().put(key, value);
		discard(key);
	}
	
	/** Marks the entire flash to be discarded by the end of the current request.
	 */
	public void discard() {
		getKeysToKeep().clear();
	}
	
	/** Marks a single flash entry to be discarded by the end of the current request.
	 * This is overridden by a call to {@link keep()}.
	 */
	public void discard(String key) {
		getKeysToKeep().remove(key);
	}
	
	private void registerCallback() {
		getRequestAttributes().registerDestructionCallback(onRequestEndCallbackAttributeName, REQUEST_END_CALLBACK, RequestAttributes.SCOPE_REQUEST);
	}

	public Object remove(Object key) {
		getKeysToKeep().remove(key);
		return getMap().remove(key);
	}

	public int size() {
		return getMap().size();
	}

	public Collection<Object> values() {
		return getMap().values();
	}

}
