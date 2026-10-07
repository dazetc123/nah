import { BrowserRouter, Route, Routes } from 'react-router-dom'
import { ThemeProvider } from './lib/theme'
import { ToastProvider } from './lib/toast'
import { AuthProvider } from './lib/auth'
import AppShell from './components/layout/AppShell'
import RequireManager from './components/layout/RequireManager'

import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import ForgotPasswordPage from './pages/ForgotPasswordPage'
import ForceChangePasswordPage from './pages/ForceChangePasswordPage'
import OAuthCallbackPage from './pages/OAuthCallbackPage'

import HomePage from './pages/HomePage'
import AccountsPage from './pages/AccountsPage'
import RoleAssignmentPage from './pages/RoleAssignmentPage'
import VehiclesPage from './pages/VehiclesPage'
import PlantsPage from './pages/PlantsPage'
import ProfilePage from './pages/ProfilePage'
import ConcreteTypesPage from './pages/ConcreteTypesPage'
import VehicleReportPage from './pages/VehicleReportPage'
import ConstructionPage from './pages/ConstructionPage'
import ConcreteOrderPage from './pages/ConcreteOrderPage'
import CustomerOrdersPage from './pages/CustomerOrdersPage'
import DriversPage from './pages/DriversPage'
import DriverTripsPage from './pages/DriverTripsPage'
import RevenueStatsPage from './pages/RevenueStatsPage'

export default function App() {
  return (
    <ThemeProvider>
      <ToastProvider>
        <AuthProvider>
          <BrowserRouter>
            <Routes>
              <Route path="/dang-nhap" element={<LoginPage />} />
              <Route path="/dang-ky" element={<RegisterPage />} />
              <Route path="/quen-mat-khau" element={<ForgotPasswordPage />} />
              <Route path="/doi-mat-khau-lan-dau" element={<ForceChangePasswordPage />} />
              <Route path="/oauth2/callback" element={<OAuthCallbackPage />} />
              {/* Spring Security commonly uses /login?error=... as its OAuth failure URL. */}
              <Route path="/login" element={<OAuthCallbackPage />} />
              <Route path="/oauth2/error" element={<OAuthCallbackPage />} />

              <Route element={<AppShell />}>
                <Route path="/" element={<HomePage />} />
                <Route path="/ho-so" element={<ProfilePage />} />
                <Route path="/bao-cao-xe" element={<VehicleReportPage />} />
                <Route path="/cong-trinh" element={<ConstructionPage />} />
                <Route path="/dat-be-tong" element={<ConcreteOrderPage />} />
                <Route path="/don-hang" element={<CustomerOrdersPage />} />
                <Route path="/lich-trinh" element={<DriverTripsPage />} />
                <Route element={<RequireManager />}>
                  <Route path="/tai-khoan" element={<AccountsPage />} />
                  <Route path="/phan-quyen" element={<RoleAssignmentPage />} />
                  <Route path="/xe" element={<VehiclesPage />} />
                  <Route path="/tai-xe" element={<DriversPage />} />
                  <Route path="/tram-tron" element={<PlantsPage />} />
                  <Route path="/loai-be-tong" element={<ConcreteTypesPage />} />
                  <Route path="/doanh-thu" element={<RevenueStatsPage />} />
                </Route>
              </Route>
            </Routes>
          </BrowserRouter>
        </AuthProvider>
      </ToastProvider>
    </ThemeProvider>
  )
}
