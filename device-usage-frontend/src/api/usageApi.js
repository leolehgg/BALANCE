// src/api/usageApi.js
import axios from './axios';

const usageApi = {
  startSession: (data) => {
    return axios.post('/api/usage/sessions/start', data);
  },
  
  endSession: (id, endTime) => {
    return axios.post(`/api/usage/sessions/${id}/end`, { endTime });
  },
  
  getSessions: (page = 0, size = 10, filters = {}) => {
    let url = `/api/usage/sessions?page=${page}&size=${size}`;
    
    if (filters.deviceId) url += `&deviceId=${filters.deviceId}`;
    if (filters.startDate) url += `&startDate=${filters.startDate}`;
    if (filters.endDate) url += `&endDate=${filters.endDate}`;
    
    return axios.get(url);
  },
  
  getDailyStats: (date, deviceId) => {
    let url = `/api/usage/stats/daily?date=${date}`;
    if (deviceId) url += `&deviceId=${deviceId}`;
    return axios.get(url);
  },
  
  getWeeklyStats: (startOfWeek, deviceId) => {
    let url = `/api/usage/stats/weekly?startOfWeek=${startOfWeek}`;
    if (deviceId) url += `&deviceId=${deviceId}`;
    return axios.get(url);
  }
};

export default usageApi;