import api from './api';
import { LoginRequest, RegisterRequest, User } from '@/types';

export const authService = {
  login: async (credentials: LoginRequest): Promise<User> => {
    const response = await api.post('/api/auth/login', credentials);
    return response.data;
  },

  register: async (userData: RegisterRequest): Promise<void> => {
    await api.post('/api/auth/register', userData);
  },

  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  },

  getCurrentUser: (): User | null => {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  },

  setUser: (user: User) => {
    localStorage.setItem('token', user.token);
    localStorage.setItem('user', JSON.stringify(user));
  },
};
