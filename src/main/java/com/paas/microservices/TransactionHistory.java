package com.paas.microservices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TransactionHistory {
	public final List<TransactionRow> transactions;

	public TransactionHistory(List<TransactionRow> transactions) {
		this.transactions = Collections.unmodifiableList(new ArrayList<>(transactions));
	}

	public TransactionHistory buildNewHistoryWith(TransactionRow newRow) {
		List<TransactionRow> newRows = new ArrayList<>(transactions);
		newRows.add(newRow);
		return new TransactionHistory(newRows);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((transactions == null) ? 0 : transactions.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TransactionHistory other = (TransactionHistory) obj;
		if (transactions == null) {
			if (other.transactions != null)
				return false;
		} else if (!transactions.equals(other.transactions))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TransactionHistory [transactions=" + transactions + "]";
	}
}
