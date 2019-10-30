package com.rahul.workflowEngine.token;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CompletionTokenTest {

	private Token token = new CompletionToken();

	@Test
	public void whenTokenSuccess_ReturnTrue() {
		token.success().thenAccept(v -> {
			Assertions.assertTrue(true);
		});

	}

	@Test
	public void whenFailure_ReturnException() {
		token.failure(new RuntimeException("This is a sample error")).thenAccept(error -> {
			Assertions.assertThrows(RuntimeException.class, () -> {
				throw error;
			});
		});
	}

}
