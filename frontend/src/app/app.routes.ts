import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { roleGuard } from './core/auth/role.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/jobs', pathMatch: 'full' },

  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login.component').then(m => m.LoginComponent),
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./features/auth/register/register.component').then(m => m.RegisterComponent),
  },
  {
    path: 'forbidden',
    loadComponent: () =>
      import('./features/auth/forbidden.component').then(m => m.ForbiddenComponent),
  },

  // Jobs
  {
    path: 'jobs',
    loadComponent: () =>
      import('./features/jobs/list/job-list.component').then(m => m.JobListComponent),
  },
  {
    path: 'jobs/create',
    canActivate: [roleGuard(['CLIENT'])],
    loadComponent: () =>
      import('./features/jobs/create/job-create.component').then(m => m.JobCreateComponent),
  },
  {
    path: 'jobs/:id/edit',
    canActivate: [roleGuard(['CLIENT'])],
    loadComponent: () =>
      import('./features/jobs/edit/job-edit.component').then(m => m.JobEditComponent),
  },
  {
    path: 'jobs/:jobId/apply',
    canActivate: [roleGuard(['FREELANCER'])],
    loadComponent: () =>
      import('./features/applications/apply-form/apply-form.component').then(
        m => m.ApplyFormComponent
      ),
  },
  {
    path: 'jobs/:id/applications',
    canActivate: [roleGuard(['CLIENT'])],
    loadComponent: () =>
      import('./features/applications/job-applications/job-applications.component').then(
        m => m.JobApplicationsComponent
      ),
  },
  {
    path: 'jobs/:id',
    loadComponent: () =>
      import('./features/jobs/detail/job-detail.component').then(m => m.JobDetailComponent),
  },
  {
    path: 'my/jobs',
    canActivate: [roleGuard(['CLIENT'])],
    loadComponent: () =>
      import('./features/jobs/my-jobs/my-jobs.component').then(m => m.MyJobsComponent),
  },

  // Applications
  {
    path: 'my/applications',
    canActivate: [roleGuard(['FREELANCER'])],
    loadComponent: () =>
      import('./features/applications/my-applications/my-applications.component').then(
        m => m.MyApplicationsComponent
      ),
  },

  // Contracts
  {
    path: 'my/contracts',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/contracts/my-contracts/my-contracts.component').then(
        m => m.MyContractsComponent
      ),
  },
  {
    path: 'contracts/:id',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/contracts/contract-detail/contract-detail.component').then(
        m => m.ContractDetailComponent
      ),
  },

  // Profile
  {
    path: 'profile/me/edit',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/profile/profile-edit/profile-edit.component').then(
        m => m.ProfileEditComponent
      ),
  },
  {
    path: 'profile/me/payment-info',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/profile/payment-info/payment-info.component').then(
        m => m.PaymentInfoComponent
      ),
  },
  {
    path: 'profile/me',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/profile/profile-view/profile-view.component').then(
        m => m.ProfileViewComponent
      ),
  },
  {
    path: 'profile/:id',
    loadComponent: () =>
      import('./features/profile/profile-view/profile-view.component').then(
        m => m.ProfileViewComponent
      ),
  },

  // Notifications
  {
    path: 'notifications',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/notifications/list/notification-list.component').then(
        m => m.NotificationListComponent
      ),
  },

  // Payment
  {
    path: 'payment/success',
    loadComponent: () =>
      import('./features/payment/success/payment-success.component').then(
        m => m.PaymentSuccessComponent
      ),
  },
  {
    path: 'payment/cancel',
    loadComponent: () =>
      import('./features/payment/cancel/payment-cancel.component').then(
        m => m.PaymentCancelComponent
      ),
  },

  // Admin
  {
    path: 'admin',
    canActivate: [roleGuard(['ADMIN'])],
    loadComponent: () =>
      import('./features/admin/admin-shell.component').then(m => m.AdminShellComponent),
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/admin/dashboard/admin-dashboard.component').then(
            m => m.AdminDashboardComponent
          ),
      },
      {
        path: 'contracts',
        loadComponent: () =>
          import('./features/admin/contracts/admin-contracts.component').then(
            m => m.AdminContractsComponent
          ),
      },
      {
        path: 'payouts',
        loadComponent: () =>
          import('./features/admin/payouts/admin-payouts.component').then(
            m => m.AdminPayoutsComponent
          ),
      },
      {
        path: 'disputes',
        loadComponent: () =>
          import('./features/admin/disputes/admin-disputes.component').then(
            m => m.AdminDisputesComponent
          ),
      },
      {
        path: 'users',
        loadComponent: () =>
          import('./features/admin/users/admin-users.component').then(m => m.AdminUsersComponent),
      },
      {
        path: 'settings',
        loadComponent: () =>
          import('./features/admin/settings/admin-settings.component').then(
            m => m.AdminSettingsComponent
          ),
      },
    ],
  },

  {
    path: '**',
    loadComponent: () =>
      import('./features/not-found/not-found.component').then(m => m.NotFoundComponent),
  },
];
