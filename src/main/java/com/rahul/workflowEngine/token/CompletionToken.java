package com.rahul.workflowEngine.token;

public class CompletionToken implements Token {

	private TokenListener listner;

	public CompletionToken(TokenListener listner) {
		this.listner = listner;

	}

	@Override
	public void success() {
		listner.onSuccess();

	}

	@Override
	public void failure(Throwable error) {
		listner.onFailure(error);
	}

}
