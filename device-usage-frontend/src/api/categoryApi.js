// src/api/categoryApi.js
import axios from './axios';

const categoryApi = {
  getAllCategories: () => {
    return axios.get('/api/categories');
  },
  
  getCategory: (id) => {
    return axios.get(`/api/categories/${id}`);
  }
};

export default categoryApi;