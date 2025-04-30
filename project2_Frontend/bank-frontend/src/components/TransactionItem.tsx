// src/components/TransactionItem.tsx
import React from "react";
import { useNavigate } from "react-router-dom";

interface TransactionItemProps {
  type: string;
  amount: number;
  date: string;
  account?: string;
}

const TransactionItem: React.FC<TransactionItemProps> = ({ type, amount, date, account }) => {
  const navigate = useNavigate();

  return (
    <div className="transaction-item flex justify-between items-center border-b py-3 px-2">
      <div className="transaction-details">
        <div className="text-lg font-semibold">{type}</div>
        <div className={`transaction-amount ${type === "입금" ? "text-green-600" : "text-red-500"}`}>
          {amount.toLocaleString()}원
        </div>
        <div className="transaction-date text-sm text-gray-500">{date}</div>
        {account && <div className="text-sm text-gray-500">받는 계좌: {account}</div>}
      </div>

      {/* ✅ 동작 버튼들 */}
      <div className="flex gap-2">
        <button onClick={() => navigate("/deposit")} className="bg-green-200 px-3 py-1 rounded text-sm">입금</button>
        <button onClick={() => navigate("/withdraw")} className="bg-yellow-200 px-3 py-1 rounded text-sm">출금</button>
        <button onClick={() => navigate("/transfer")} className="bg-blue-200 px-3 py-1 rounded text-sm">송금</button>
      </div>
    </div>
  );
};

export default TransactionItem;
