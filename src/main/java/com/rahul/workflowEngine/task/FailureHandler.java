package com.rahul.workflowEngine.task;

import com.rahul.workflowEngine.engine.WorkflowContext;
import com.rahul.workflowEngine.token.Token;

@FunctionalInterface
public interface FailureHandler {

	void handle(Token token, WorkflowContext context, Throwable error);
}
