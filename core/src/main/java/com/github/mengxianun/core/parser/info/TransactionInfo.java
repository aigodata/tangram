package com.github.mengxianun.core.parser.info;

import java.util.List;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class TransactionInfo {

	public static TransactionInfo create(List<SimpleInfo> simples) {
		return new AutoValue_TransactionInfo(simples);
	}

	public abstract List<SimpleInfo> simples();

}
