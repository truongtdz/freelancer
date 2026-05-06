export type JobStatus = 'DRAFT' | 'OPEN' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
export type BudgetType = 'FIXED' | 'HOURLY';
export type WorkMode = 'REMOTE' | 'ONSITE' | 'HYBRID';

export interface Category {
  id: number;
  name: string;
  slug: string;
  parentId?: number;
}

export interface Skill {
  id: number;
  name: string;
  slug: string;
}

export interface JobAttachment {
  id: number;
  fileUrl: string;
  fileName: string;
  fileSize: number;
}

export interface UserSummary {
  id: number;
  username: string;
  email: string;
  fullName: string;
  avatarUrl?: string;
  role: string;
  status: string;
}

export interface JobListItem {
  id: number;
  title: string;
  shortDescription: string;
  category: Category;
  budgetMin: number;
  budgetMax: number;
  budgetType: BudgetType;
  workMode: WorkMode;
  status: JobStatus;
  location?: string;
  deadline?: string;
  createdAt: string;
  client: UserSummary;
  skills: Skill[];
  applicationCount: number;
}

export interface JobDetail extends JobListItem {
  description: string;
  updatedAt: string;
  attachments: JobAttachment[];
  canApply: boolean;
  owner: boolean;
}

export interface JobCreateRequest {
  title: string;
  description: string;
  categoryId: number;
  budgetMin: number;
  budgetMax: number;
  budgetType: BudgetType;
  workMode: WorkMode;
  location?: string;
  deadline?: string;
  skillIds: number[];
  attachments?: { fileUrl: string; fileName: string; fileSize: number }[];
}

export interface JobUpdateRequest extends Partial<JobCreateRequest> {}

export interface UploadResponse {
  url: string;
  fileName: string;
  storedName: string;
  fileSize: number;
  contentType: string;
  subFolder: string;
}

export interface JobSearchParams {
  keyword?: string;
  categoryId?: number;
  skillIds?: number[];
  budgetType?: BudgetType;
  workMode?: WorkMode;
  budgetMin?: number;
  budgetMax?: number;
  sort?: string;
  page?: number;
  size?: number;
  status?: JobStatus;
}
