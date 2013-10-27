/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.locker;

import java.util.concurrent.Future;
import javax.ejb.*;
import javax.inject.Inject;

/**
 *
 * @author François Achache
 */
@Stateless
@LocalBean
public class AsyncBean {

	@Inject
	private LockedBean lockedBean;

	public void reset() {
		lockedBean.reset();
	}

	@Asynchronous
	public Future<Integer> accessLockedMethodWithAnnotedArgument(String key) {
		return new AsyncResult<Integer>(lockedBean.lockedMethodWithAnnotedArgument(key));
	}

	@Asynchronous
	public Future<Integer> accessUnlockedMethodWithAnnotedArgument(String key) {
		return new AsyncResult<Integer>(lockedBean.unlockedMethodWithAnnotedArgument(key));
	}

	@Asynchronous
	public Future<Integer> accessLockedMethod1WithAnnotation(String key) {
		return new AsyncResult<Integer>(lockedBean.lockedMethod1WithAnnotation(key));
	}

	@Asynchronous
	public Future<Integer> accessLockedMethod2WithAnnotation(String key) {
		return new AsyncResult<Integer>(lockedBean.lockedMethod2WithAnnotation(key));
	}
}
