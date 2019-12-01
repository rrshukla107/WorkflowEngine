package com.rahul.workflowEngine.engine;

import java.util.concurrent.CompletableFuture;

public interface WorkflowEngine {

	public CompletableFuture<Void> executeWorkflow(Workflow workflow);
}
