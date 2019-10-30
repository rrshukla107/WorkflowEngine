package com.rahul.workflowEngine.engine;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.rahul.workflowEngine.task.Task;
import com.rahul.workflowEngine.token.CompletionToken;
import com.rahul.workflowEngine.token.LambdaTokenListner;
import com.rahul.workflowEngine.token.TokenListener;

public class WorkflowEngineImpl implements WorkflowEngine {

	private Queue<Task> queue;
	private CountDownLatch latch;
	private ExecutorService executor;
	private TokenListener listner;
	private WorkflowContext context;

	@Override
	public void executeWorkflow(Workflow workflow) {

		initializeEngine(workflow);
		initializeListner();

		executor.execute(() -> {
			executeNextTask();
		});

		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			executor.shutdown();

		}

	}

	private void initializeListner() {
		this.listner = new LambdaTokenListner(() -> {
			this.executeNextTask();
		}, error -> {
		});

	}

	private void initializeEngine(Workflow workflow) {
		this.queue = new LinkedList<Task>(workflow.getWorkflowTasks());
		this.latch = new CountDownLatch(queue.size());
		this.context = workflow.getContext();
		this.executor = Executors.newFixedThreadPool(2);
	}

	private void executeNextTask() {
		if (!queue.isEmpty()) {
			queue.poll().execute(new CompletionToken(this.listner), this.context);
			latch.countDown();
		}
	}

}
