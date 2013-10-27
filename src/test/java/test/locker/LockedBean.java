/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.locker;

import hhf.locker.annotations.Locked;
import hhf.locker.annotations.LockKey;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Singleton;
import javax.inject.Inject;
import org.slf4j.Logger;

/**
 *
 * @author François Achache
 */
@Singleton
public class LockedBean {

	private long WAITING = 200;
	@Inject
	private Logger logger;
	private Map<String, Integer> map = new HashMap<String, Integer>();

	public void reset() {
		map.clear();
	}
	/**
	 * Cette méthode à un mutex sur key, c'est à dire que si 2 process call cette methode avec le même argument. le 2e process attendra la fin du 1er
	 * @param key
	 * @return 
	 */
	@Locked
	public Integer lockedMethodWithAnnotedArgument(@LockKey final String key) {
		try {
			Integer value = map.get(key);
			Thread.sleep(WAITING);
			if (value == null) {
				map.put(key, 0);
			} else {
				map.put(key, value + 1);
			}
		} catch (InterruptedException ex) {
		}
		logger.debug("Valeur {} pour la clé {}", map.get(key), key);
		return map.get(key);
	}

	/**
	 * En revanche cette methode n'a pas de regle de lock
	 * @param key
	 * @return 
	 */
	public Integer unlockedMethodWithAnnotedArgument(final String key) {
		return lockedMethodWithAnnotedArgument(key);
	}
	
	/**
	 * les methodes annotées "lockedMethodWithAnnotation" se lock mutuellement
	 * @param key
	 * @return 
	 */
	@Locked("lockedMethodWithAnnotation")
	public Integer lockedMethod1WithAnnotation(@LockKey final String key) {
		try {
			Integer value = map.get(key);
			Thread.sleep(WAITING);
			if (value == null) {
				map.put(key, 0);
			} else {
				map.put(key, value + 1);
			}
		} catch (InterruptedException ex) {
		}
		logger.debug("Valeur {} pour la clé {}", map.get(key), key);
		return map.get(key);
	}

	@Locked("lockedMethodWithAnnotation")
	public Integer lockedMethod2WithAnnotation(@LockKey final String key) {
		try {
			Integer value = map.get(key);
			Thread.sleep(WAITING);
			if (value == null) {
				map.put(key, 0);
			} else {
				map.put(key, value + 1);
			}
		} catch (InterruptedException ex) {
		}
		logger.debug("Valeur {} pour la clé {}", map.get(key), key);
		return map.get(key);
	}
}
