package com.rahul.workflowEngine.token;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public interface FailureToken {

	default CompletionStage<Throwable> failure(Throwable error) {
		return CompletableFuture.completedFuture(error);
	};

}
