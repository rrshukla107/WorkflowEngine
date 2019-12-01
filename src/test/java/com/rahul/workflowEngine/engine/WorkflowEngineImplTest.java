package com.rahul.workflowEngine.engine;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import com.rahul.workflowEngine.task.Task;

class WorkflowEngineImplTest {

	@Test
	void simulateWorkflow() {

		List<Task> tasks = List.of(getTaskWithDelay("Task 1", 2), getTaskWithDelay("Task 2", 3),
				getTaskWithDelay("Task 3", 3));

		Workflow workflow = new WorkflowBuilder().addTasks(tasks).build();

		new WorkflowEngineImpl().executeWorkflow(workflow).thenAccept(v -> {
			System.out.println("Tasks Executed Successfully");
		});
	}

	@Test
	void simulateWorkflowWithError() {

		List<Task> tasks = List.of(this.getTaskWithDelay("Task 1", 2), this.getTaskWithDelay("Task 2", 3),
				this.getTaskWithDelay("Task 3", 3), this.getTaskWithFailure());
		Workflow workflow = new WorkflowBuilder().addTasks(tasks).addFailureTask((token, context, error) -> {
			System.out.println("Failure Detected");
			error.printStackTrace(System.out);
		}).build();
		new WorkflowEngineImpl().executeWorkflow(workflow).thenAccept(v -> {
			System.out.println("Tasks Executed Successfully");
		});

	}

	private Task getTaskWithFailure() {
		return (token, context) -> {
			token.failure(new RuntimeException("This is a failure"));
		};
	}

	private Task getTaskWithDelay(String taskName, int seconds) {
		return (token, context) -> {
			try {
				TimeUnit.SECONDS.sleep(seconds);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println(taskName);
			token.success();
		};
	}

}
