package com.rahul.workflowEngine.engine;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import com.rahul.workflowEngine.task.Task;

class WorkflowEngineImplTest {

	@Test
	void test() {

		List<Task> tasks = List.of(getTaskWithDelay("Task 1", 2), getTaskWithDelay("Task 2", 3),
				getTaskWithDelay("Task 3", 3));

		Workflow workflow = new WorkflowBuilder().addTasks(tasks).build();

		new WorkflowEngineImpl().executeWorkflow(workflow);
	}

	private Task getTaskWithDelay(String taskName, int seconds) {
		return token -> {
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
