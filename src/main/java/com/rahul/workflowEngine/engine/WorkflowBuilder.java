package com.rahul.workflowEngine.engine;

import java.util.List;

import com.rahul.workflowEngine.task.FailureHandler;
import com.rahul.workflowEngine.task.Task;

public class WorkflowBuilder {

	private Workflow workflow;

	public WorkflowBuilder() {
		this.workflow = new Workflow();
	}

	public WorkflowBuilder addTasks(List<Task> tasks) {
		this.workflow.setWorkflowTasks(tasks);
		return this;
	}

	public WorkflowBuilder addFailureHandler(FailureHandler failureHandler) {
		this.workflow.setFailureHandler(failureHandler);
		return this;
	}

	public WorkflowBuilder withContext(WorkflowContext context) {
		this.workflow.setContext(context);
		return this;
	}

	public Workflow build() {
		return workflow;
	}
}
