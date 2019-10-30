package com.rahul.workflowEngine.token;

import java.util.function.Consumer;

public class LambdaTokenListner implements TokenListener {

	private Procedure onSuccessDoThis;
	private Consumer<Throwable> onFailureDoThis;

	public LambdaTokenListner(Procedure onSuccessDoThis, Consumer<Throwable> onFailureDoThis) {
		this.onSuccessDoThis = onSuccessDoThis;
		this.onFailureDoThis = onFailureDoThis;
	}

	@Override
	public void onSuccess() {
		this.onSuccessDoThis.perform();
	}

	@Override
	public void onFailure(Throwable error) {
		this.onFailureDoThis.accept(error);
	}

}
