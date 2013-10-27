/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hhf.locker.beans;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;

/**
 *
 * @author François Achache
 */
@Singleton
public class SemaphoreManager {

	private Map<String, Semaphore> semaphores = new HashMap<String, Semaphore>();

	/**
	 * Demande un sémaphore pour une clé donnée
	 *
	 * @param key
	 * @return
	 */
	@Lock(LockType.READ)
	public Semaphore getSemaphore(final String key) {
		return semaphores.get(key);
	}

	/**
	 * Crée un sémaphore pour une clé donnée
	 *
	 * @param key
	 * @return
	 */
	@Lock(LockType.WRITE)
	public Semaphore createSemaphore(final String key) {
		Semaphore sem = new Semaphore(1, true);
		semaphores.put(key, sem);
		return sem;
	}

	/**
	 * Supprime une entrée
	 *
	 * @param key
	 * @return
	 */
	@Lock(LockType.WRITE)
	public void releaseSemaphore(final String key) {
		if (semaphores.containsKey(key)) {
			Semaphore sem = semaphores.get(key);
			// le semaphore n'est pas encore liberer
			if (!sem.hasQueuedThreads()) { // si aucun process n'attend, on le supprime de la map
				semaphores.remove(key);
			}
			sem.release(); // et enfin on libere
		}
	}
}
