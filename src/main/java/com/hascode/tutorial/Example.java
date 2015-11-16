package com.hascode.tutorial;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.Callable;

import com.evanlennick.retry4j.CallExecutor;
import com.evanlennick.retry4j.CallResults;
import com.evanlennick.retry4j.RetryConfig;
import com.evanlennick.retry4j.RetryConfigBuilder;
import com.evanlennick.retry4j.exception.RetriesExhaustedException;
import com.evanlennick.retry4j.exception.UnexpectedException;

public class Example {
	public static void main(String[] args) {
		SomeApi api = new SomeApi();

		Callable<String> callable = () -> api.call();

		// @formatter:off
		RetryConfig config = new RetryConfigBuilder()
									.retryOnSpecificExceptions(ApiException.class)
									.withMaxNumberOfTries(6).withDelayBetweenTries(Duration.of(5, ChronoUnit.SECONDS))
									.withFixedBackoff()
								 .build();
		// @formatter:on

		try {
			CallResults<String> results = new CallExecutor(config).execute(callable);
			String result = results.getResult();
			System.out.println("result is: " + result);
		} catch (RetriesExhaustedException ree) {
			System.err.println("retries exhausted..");
		} catch (UnexpectedException ue) {
			System.err.println("we're out..");
		}
	}

	static class ApiException extends Exception {
	}

	static class SomeApi {
		private int count = 1;

		String call() throws ApiException {
			System.out.println("API call #" + count);
			count++;
			if (count < 5) {
				System.out.println("throwing exception..");
				throw new ApiException();
			}

			return UUID.randomUUID().toString().toUpperCase();
		}
	}
}
