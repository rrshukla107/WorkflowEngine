package com.rahul.workflowEngine.task;

import com.rahul.workflowEngine.engine.WorkflowContext;
import com.rahul.workflowEngine.token.Token;

@FunctionalInterface
public interface Task {

	void execute(Token token, WorkflowContext context);

}
