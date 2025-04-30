// src/types/auth.ts

export interface LoginResponse {
    userId: number;
    accessToken: string;
    refreshToken: string;
    mfaRegistered: boolean;
  }
  
  export interface JoinResponse {
    userId: string;
    name: string;
    email: string;
  }
  