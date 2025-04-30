export interface Transaction {
    transactionId: string;
    fromAccountNumber: string;
    toAccountNumber: string;
    type: "deposit" | "withdrawal" | "transfer";
    amount: number;
    memo: string;
    status: string;
    balanceAfter: number;
    createdAt: string;
  }
  