package com.rahul.workflowEngine.token;

public interface TokenListener {

	public void onSuccess();

	public void onFailure(Throwable error);
}
