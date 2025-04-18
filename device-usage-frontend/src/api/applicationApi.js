// src/api/applicationApi.js
import axios from './axios';

const applicationApi = {
  getAllApplications: () => {
    return axios.get('/api/applications');
  },
  
  getApplication: (id) => {
    return axios.get(`/api/applications/${id}`);
  },
  
  updateApplication: (id, applicationData) => {
    return axios.put(`/api/applications/${id}`, applicationData);
  }
};

export default applicationApi;