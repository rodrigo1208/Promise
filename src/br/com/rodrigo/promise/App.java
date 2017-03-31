package br.com.rodrigo.promise;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App {
	
	private static final String DEFAULT_URL = "https://github.com/iluwatar/java-design-patterns/blob/master/README.md";
	private final ExecutorService executor;
	private final CountDownLatch stopLatch;
	
	private App() {
		executor = Executors.newFixedThreadPool(2);
		stopLatch = new CountDownLatch(2);
	}
	
	public static void main(String[] args) throws InterruptedException {
		
		App app = new App();
		try {
			app.promiseUsage();
		} finally{
			app.stop();
		}
		
	}

	private void promiseUsage() {
		calculateLineCount();
		calculateLowestFreaquencyChar();
	}
	
	private void calculateLowestFreaquencyChar() {
		lowestFrequencyChar()
			.thenAccept(
				charFrequency -> {
					System.out.println("Char com a menor frequencia é: " + charFrequency);
					taskCompleted();
				}
		);
	}

	private void calculateLineCount() {
		countLines().thenAccept(
			count -> {
				System.out.println("Linha contada é: " + count);
				taskCompleted();
			}
		);
	}
	
	private Promise<Character> lowestFrequencyChar(){
		return characterFrequency().thenApply(Util::lowestFrequencyChar);
	}
	
	private Promise<Map<Character, Integer>> characterFrequency(){
		return download(DEFAULT_URL).thenApply(Util::characterFrequency);
	}
	
	private Promise<Integer> countLines(){
		return download(DEFAULT_URL).thenApply(Util::countLines);
	}
	
	private Promise<String> download(String url){
		Promise<String> downloadPromise = new Promise<String>()
				.fulfillInASync(
						() -> {
							return Util.downloadFile(url);
						}
				, executor)
				.onError(
						throwable -> {
							throwable.printStackTrace();
							taskCompleted();
						}
						
				);
		return downloadPromise;
	}

	private void taskCompleted() {
		stopLatch.countDown();
	}

	private void stop() throws InterruptedException {
		stopLatch.await();
		executor.shutdownNow();
		
	}
	
}
