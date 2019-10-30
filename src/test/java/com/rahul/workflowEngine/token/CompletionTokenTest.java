package com.rahul.workflowEngine.token;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CompletionTokenTest {
	private Token token;

	@Test
	public void whenTokenSuccess_ReturnTrue() {

		final AtomicBoolean wasOnSuccessCalled = new AtomicBoolean();
		token = new CompletionToken(new LambdaTokenListner(() -> {
			wasOnSuccessCalled.set(true);
		}, error -> {
		}));

		token.success();
		assertTrue(wasOnSuccessCalled.get());

	}

	@Test
	public void whenFailure_ReturnException() {
		final AtomicBoolean wasOnSuccessCalled = new AtomicBoolean();
		token = new CompletionToken(new LambdaTokenListner(() -> {
			wasOnSuccessCalled.set(true);
		}, error -> {
			Assertions.assertThrows(RuntimeException.class, () -> {
				throw error;
			});
		}));
		token.failure(new RuntimeException("This is sample error"));
	}

}
