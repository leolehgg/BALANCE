// src/api/authApi.js
import axios from './axios';

const authApi = {
  login: (username, password) => {
    return axios.post('/api/auth/signin', { username, password });
  },
  
  register: (username, email, password) => {
    return axios.post('/api/auth/signup', { username, email, password });
  },
  
  logout: () => {
    return axios.post('/api/auth/signout');
  }
};

export default authApi;