// src/api/deviceApi.js
import axios from './axios';

const deviceApi = {
  getAllDevices: () => {
    return axios.get('/api/devices');
  },
  
  getDevice: (id) => {
    return axios.get(`/api/devices/${id}`);
  },
  
  registerDevice: (deviceData) => {
    return axios.post('/api/devices', deviceData);
  },
  
  updateDevice: (id, deviceData) => {
    return axios.put(`/api/devices/${id}`, deviceData);
  },
  
  deleteDevice: (id) => {
    return axios.delete(`/api/devices/${id}`);
  }
};

export default deviceApi;