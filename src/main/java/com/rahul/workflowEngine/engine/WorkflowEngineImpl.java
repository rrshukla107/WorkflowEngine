package com.rahul.workflowEngine.engine;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.rahul.workflowEngine.task.FailureHandler;
import com.rahul.workflowEngine.task.Task;
import com.rahul.workflowEngine.token.CompletionToken;
import com.rahul.workflowEngine.token.LambdaTokenListner;
import com.rahul.workflowEngine.token.TokenListener;

public class WorkflowEngineImpl implements WorkflowEngine {

	private Queue<Task> queue;
	private CountDownLatch latch;
	private ExecutorService executor;
	private TokenListener listner;
	private TokenListener failureListener;
	private FailureHandler errorTask;
	private WorkflowContext context;
	private CompletableFuture<Void> successPromise;

	@Override
	public CompletableFuture<Void> executeWorkflow(Workflow workflow) {

		initializePromise();
		initializeEngine(workflow);
		initializeListener();
		initializeFailureListener();

		executor.execute(() -> {

			executeNextTask();
			try {
				latch.await();
			} catch (InterruptedException e) {
				System.out.println("WORKFLOW DIRECTED TO FAILURE HANDLER");
			}
			this.shutdownWorkflow();

		});

		return successPromise;

	}

	private void initializePromise() {
		this.successPromise = new CompletableFuture<>();

	}

	private void initializeFailureListener() {
		this.failureListener = new LambdaTokenListner(() -> {
			WorkflowEngineImpl.this.shutdownWorkflow();
		}, error -> {
			throw new RuntimeException("Workflow has failed");
		});
	}

	private void shutdownWorkflow() {
		this.executor.shutdownNow();
		successPromise.complete(null);
	}

	private void initializeListener() {
		this.listner = new LambdaTokenListner(() -> {
			this.executeNextTask();
		}, error -> {
			this.errorTask.handle(new CompletionToken(failureListener), this.context, error);
		});

	}

	private void initializeEngine(Workflow workflow) {
		this.queue = new LinkedList<Task>(workflow.getWorkflowTasks());
		this.latch = new CountDownLatch(queue.size());
		this.context = workflow.getContext();
		this.errorTask = workflow.getErrorTask();
		this.executor = Executors.newFixedThreadPool(2);
	}

	private void executeNextTask() {
		if (!queue.isEmpty()) {
			queue.poll().execute(new CompletionToken(this.listner), this.context);
			latch.countDown();
		}
	}

}
