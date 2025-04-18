// src/api/limitApi.js
import axios from './axios';

const limitApi = {
  getAllLimits: () => {
    return axios.get('/api/limits');
  },
  
  getLimit: (id) => {
    return axios.get(`/api/limits/${id}`);
  },
  
  createLimit: (limitData) => {
    return axios.post('/api/limits', limitData);
  },
  
  updateLimit: (id, limitData) => {
    return axios.put(`/api/limits/${id}`, limitData);
  },
  
  deleteLimit: (id) => {
    return axios.delete(`/api/limits/${id}`);
  }
};

export default limitApi;