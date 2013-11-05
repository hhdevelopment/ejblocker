/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.hhdev.locker;

import fr.hhdev.locker.annotations.LockKey;
import fr.hhdev.locker.annotations.Locked;
import fr.hhdev.locker.beans.SemaphoreManager;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.concurrent.Semaphore;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import org.slf4j.Logger;

/**
 *
 * @author François Achache
 */
@Locked
@Interceptor
public class Locker {

	@EJB
	private SemaphoreManager manager;
	@Inject
	private Logger logger;

	/**
	 * Methode interceptor La classe doit être un singleton la methode ou la classe doit être annoté LockType.READ
	 *
	 * @param ic
	 * @return
	 * @throws Exception
	 */
	@AroundInvoke
	protected Object executing(InvocationContext ic) throws Exception {
		Class<?> target = ic.getTarget().getClass();
		Method m = ic.getMethod();
		Object result;
		// Si aucun argument n'est annoté avec LockerKey et que la methode n'est pas annoté Locked, on laisse faire le Singleton
		LockType lockType = LockType.READ;
		if (target.isAnnotationPresent(Singleton.class)) {
			lockType = LockType.WRITE;
		}
		// Détermine si le lock du singleton est bien débraillé,par defaut les methodes d'un singleton sont à LockType.WRITE
		if (m.isAnnotationPresent(Lock.class)) { // possibilité de débraillé sur la methode
			Lock an = m.getAnnotation(Lock.class);
			lockType = an.value();
		} else if (target.isAnnotationPresent(Lock.class)) { // ou sur la classes
			Lock an = target.getAnnotation(Lock.class);
			lockType = an.value();
		}
		if (lockType.equals(LockType.WRITE)) {
			logger.error("Method Locked on javax.ejb.Singleton must be LockType.READ. Directly on the method, or on class.");
			result = ic.proceed();
		} else {
			// construction d'une clé pour le sémaphore, à partir du nom de la classe et des argument annoté LockerKey
			String keyFromMethod;
			Locked locked = m.getAnnotation(Locked.class);
			if (!locked.value().isEmpty()) {
				keyFromMethod = locked.value();
			} else {
				keyFromMethod = ic.getTarget().getClass().getName() + "." + m.getName();
			}
			StringBuilder keyLock = new StringBuilder(keyFromMethod);
			int paramIdx = 0;
			for (Annotation[] a : m.getParameterAnnotations()) {
				for (Annotation anno : a) {
					if (anno.annotationType().equals(LockKey.class)) {
						String methodName = ((LockKey) anno).method();
						Object param = ic.getParameters()[paramIdx];
						Method method = param.getClass().getMethod(methodName);
						keyLock.append(method.invoke(param));
					}
				}
				paramIdx++;
			}
			String key = keyLock.toString();
			// récupération du sémpahore
			Semaphore sem = manager.getSemaphore(key);
			if (sem == null) {
				sem = manager.createSemaphore(key);
			}
			logger.debug("Nombre de thread en attente avant aquire pour {} : {}", key, sem.getQueueLength());
			sem.acquire(); // aquisition du verrou ou attente
			try {
				result = ic.proceed();
			} finally {
				manager.releaseSemaphore(key); // libération et peut etre suppression
			}
		}
		return result;
	}
}
