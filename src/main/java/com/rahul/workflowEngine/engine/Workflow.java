package com.rahul.workflowEngine.engine;

import java.util.ArrayList;
import java.util.List;

import com.rahul.workflowEngine.task.Task;

public class Workflow {

	private List<Task> workflowTasks;
	private WorkflowContext context;

	public Workflow() {
		workflowTasks = new ArrayList<Task>();
	}

	public List<Task> getWorkflowTasks() {
		return workflowTasks;
	}

	public void setWorkflowTasks(List<Task> workflowTasks) {
		this.workflowTasks = workflowTasks;
	}

	public WorkflowContext getContext() {
		return context;
	}

	public void setContext(WorkflowContext context) {
		this.context = context;
	}

}
