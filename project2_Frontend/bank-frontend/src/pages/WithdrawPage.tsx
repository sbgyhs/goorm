import React, { useEffect, useState } from "react";
import axios from "../lib/axios";
import { useNavigate } from "react-router-dom";

interface GetMyAccountResponse {
  accountNumber: string;
  balance: number;
  createdAt: string;
}

export default function WithdrawPage() {
  const [amount, setAmount] = useState("");
  const [accountNumber, setAccountNumber] = useState("");
  const [message, setMessage] = useState("");
  const [balance, setBalance] = useState<number | null>(null);
  const [memo, setMemo] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    axios
      .get<GetMyAccountResponse[]>("/accounts/me")
      .then((res) => {
        setAccountNumber(res.data[0].accountNumber);
        setBalance(res.data[0].balance);
      })
      .catch(() => setMessage("❌ 계좌 정보를 가져오지 못했습니다."));
  }, []);

  const handleWithdraw = async () => {
    const amountNumber = Number(amount);

    if (!amount || isNaN(amountNumber)) {
      setMessage("❌ 숫자로 된 출금 금액을 입력해주세요.");
      return;
    }

    if (amountNumber <= 0) {
      setMessage("❌ 출금 금액은 0보다 커야 합니다.");
      return;
    }

    if (balance !== null && amountNumber > balance) {
      setMessage("❌ 잔액보다 많은 금액은 출금할 수 없습니다.");
      return;
    }

    try {
      await axios.post("/transactions/withdraw", {
        fromAccountNumber: accountNumber,
        amount: amountNumber,
        memo,
      });
      navigate("/transactions");
    } catch (err) {
      console.error(err);
      setMessage("❌ 출금 중 문제가 발생했습니다. 다시 시도해주세요.");
    }
  };

  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-gray-50 px-4">
      <div className="fixed top-6 right-6 z-50">
        <button
          onClick={() => navigate("/transactions")}
          className="bg-gray-700 hover:bg-gray-800 text-white px-4 py-2 rounded-lg flex items-center gap-2"
        >
          🏠 홈
        </button>
      </div>

      <h1 className="text-2xl font-bold mb-6">💸 출금하기</h1>

      {balance !== null && (
        <p className="mb-4 text-gray-700 text-lg">
          현재 잔액:{" "}
          <span className="font-semibold text-blue-700">
            {balance.toLocaleString()}원
          </span>
        </p>
      )}

      <form
        onSubmit={(e) => {
          e.preventDefault();
          handleWithdraw();
        }}
        className="w-full max-w-md flex flex-col gap-4"
      >
        <div className="flex flex-col">
          <label className="text-sm text-gray-600 mb-1">내 계좌</label>
          <input
            type="text"
            value={accountNumber}
            readOnly
            className="border px-4 py-2 rounded-md bg-gray-100 text-gray-600"
          />
        </div>

        <input
          type="number"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
          placeholder="출금 금액"
          className="border px-4 py-2 rounded-md"
        />

        <input
          type="text"
          placeholder="메모 (선택사항)"
          value={memo}
          onChange={(e) => setMemo(e.target.value)}
          className="border px-4 py-2 rounded-md"
        />

        <button
          type="submit"
          className="bg-red-600 hover:bg-red-700 text-white w-full py-3 rounded-md font-semibold"
        >
          출금
        </button>

        {message && (
          <p className="mt-4 text-red-500 text-sm text-center">{message}</p>
        )}
      </form>
    </div>
  );
}
