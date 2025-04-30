import axios from "../lib/axios";
import { Transaction } from "../src/types";

export const getTransactions = async (): Promise<Transaction[]> => {
  const response = await axios.get<Transaction[]>("/transactions");
  return response.data;
};
