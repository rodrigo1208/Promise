package br.com.rodrigo.promise;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PromiseSupport<T> implements Future<T> {

	private static final int RUNNING = 1;
	private static final int FAILED = 2;
	private static final int COMPLETED = 3;
	
	private final Object lock;
	
	private volatile int state = RUNNING;
	private T value;
	private Exception exception;
	
	public PromiseSupport() {
		this.lock = new Object();
	}
	
	void fulfill(T value){
		this.value = value;
		this.state = COMPLETED;
		synchronized (lock) {
			lock.notifyAll();
		}
	}
	
	void fulfillExceptionally(Exception exception){
		this.exception = exception;
		this.state = FAILED;
		synchronized (lock) {
			lock.notifyAll();
		}
	}
	
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return false;
	}

	
	@Override
	public boolean isCancelled() {
		return false;
	}
	
	@Override
	public boolean isDone() {
		return state > RUNNING;
	}
	
	@Override
	public T get() throws InterruptedException, ExecutionException {
		if(state == COMPLETED){
			return value;
		} else if(state == FAILED){
			throw new ExecutionException(exception);
		} else {
			synchronized(lock){
				lock.wait();
				if(state == COMPLETED){
					return value;
				} else {
					throw new ExecutionException(exception);
				}
			}
		}		
	}

	@Override
	public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {

		if(state == COMPLETED){
			return value;
		} else if(state == FAILED){
			throw new ExecutionException(exception);
		} else {
			synchronized(lock){
				lock.wait(unit.toMillis(timeout));
				if(state == COMPLETED){
					return value;
				} else if(state == FAILED){
					throw new ExecutionException(exception);
				} else {
					throw new TimeoutException();
				}
				
			}
		}
		
	}

	

	

}
