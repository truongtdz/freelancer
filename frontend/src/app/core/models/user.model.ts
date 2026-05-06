export type UserRole = 'ADMIN' | 'CLIENT' | 'FREELANCER';
export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'BANNED';

export interface User {
  id: number;
  username: string;
  email: string;
  fullName: string;
  avatarUrl?: string;
  role: UserRole;
  status: UserStatus;
}

export type SkillLevel = 'BEGINNER' | 'INTERMEDIATE' | 'EXPERT';

export interface UserSkill {
  skillId: number;
  name: string;
  slug: string;
  level: SkillLevel;
}

export interface UserProfileUpdateRequest {
  fullName?: string;
  phone?: string;
  avatarUrl?: string;
  bio?: string;
  title?: string;
  experienceYears?: number;
  hourlyRate?: number;
  skills?: { skillId: number; level: SkillLevel }[];
}

export interface PaymentInfo {
  id: number;
  bankName: string;
  bankAccountNumber: string;
  bankAccountHolder: string;
  qrCodeUrl?: string;
  isDefault: boolean;
}

export interface PaymentInfoRequest {
  bankName: string;
  bankAccountNumber: string;
  bankAccountHolder: string;
  qrCodeUrl?: string;
  isDefault: boolean;
}

export interface UserProfile extends User {
  phone?: string;
  emailVerified?: boolean;
  bio?: string;
  title?: string;
  experienceYears?: number;
  hourlyRate?: number;
  ratingAvg: number;
  totalReviews: number;
  totalJobsDone: number;
  skills: UserSkill[];
  createdAt?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  fullName: string;
  role: 'CLIENT' | 'FREELANCER';
  phone?: string;
}

// Chỉ có accessToken — KHÔNG có refreshToken
export interface AuthResponse {
  accessToken: string;
  tokenType: 'Bearer';
  expiresIn: number;
  user: User;
}
