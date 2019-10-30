package com.rahul.workflowEngine.token;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public interface SuccessToken {

	default CompletionStage<Boolean> success() {
		return CompletableFuture.completedFuture(true);
	};
}
