package com.rahul.workflowEngine.engine;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rahul.workflowEngine.task.FailureHandler;
import com.rahul.workflowEngine.task.Task;

class WorkflowEngineImplTest {

	private CountDownLatch latch;

	private class AdderContext implements WorkflowContext {

		private int sum;

		public int getSum() {
			return sum;
		}

		public void setSum(int sum) {
			this.sum = sum;
		}
	}

	@BeforeEach
	public void setup() {
		this.latch = new CountDownLatch(1);
	}

	@Test
	void simulateWorkflow() throws InterruptedException {

		List<Task> tasks = List.of(getTaskWithDelay("Task 1", 2), getTaskWithDelay("Task 2", 3),
				getTaskWithDelay("Task 3", 3));

		Workflow workflow = new WorkflowBuilder().addTasks(tasks).build();

		new WorkflowEngineImpl().executeWorkflow(workflow).thenAccept(v -> {
			System.out.println("Tasks Executed Successfully");
			latch.countDown();
		});

		latch.await();
	}

	@Test
	void simulateWorkflowWithError() throws InterruptedException {

		List<Task> tasks = List.of(this.getTaskWithDelay("Task 1", 2), this.getTaskWithDelay("Task 2", 3),
				this.getTaskWithDelay("Task 3", 3), this.getTaskWithFailure());
		FailureHandler failureHandler = (token, context, error) -> {
			System.out.println("Failure Detected***");
			error.printStackTrace(System.out);
			token.success();
		};
		Workflow workflow = new WorkflowBuilder().addTasks(tasks).addFailureHandler(failureHandler).build();
		new WorkflowEngineImpl().executeWorkflow(workflow).thenAccept(v -> {
			System.out.println("Tasks Executed Successfully");
			latch.countDown();
		});

		latch.await();

	}

	@Test
	void simulateWorkflow_buildResultInContext() throws InterruptedException {

		WorkflowContext adderContext = new AdderContext();

		List<Task> tasks = List.of((token, context) -> {
			((AdderContext) context).setSum(0);
			token.success();
		}, (token, context) -> {
			((AdderContext) context).setSum(((AdderContext) context).getSum() + 5);
			token.success();
		}, (token, context) -> {
			((AdderContext) context).setSum(((AdderContext) context).getSum() + 10);
			token.success();
		});

		Workflow workflow = new WorkflowBuilder().addTasks(tasks).withContext(adderContext).build();
		new WorkflowEngineImpl().executeWorkflow(workflow).thenAccept(v -> {
			Assertions.assertEquals(15, ((AdderContext) adderContext).getSum());
			latch.countDown();
		});
		latch.await();
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
