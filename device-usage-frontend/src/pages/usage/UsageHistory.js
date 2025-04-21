// src/pages/usage/UsageHistory.js
import React, { useState, useEffect } from 'react';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import {
  Box, Typography, Grid, Paper, Tabs, Tab, 
  FormControl, Select, MenuItem, InputLabel,
  CircularProgress, Alert
} from '@mui/material';
import usageApi from '../../api/usageApi';
import deviceApi from '../../api/deviceApi';
import DailyHistoryChart from './components/DailyHistoryChart';
import WeeklyHistoryChart from './components/WeeklyHistoryChart';
import MonthlyHistoryChart from './components/MonthlyHistoryChart';
import YearlyHistoryChart from './components/YearlyHistoryChart';

const UsageHistory = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [tabValue, setTabValue] = useState(0);
  const [devices, setDevices] = useState([]);
  const [selectedDevice, setSelectedDevice] = useState('all');
  
  // Estado para cada tipo de vista
  const [dailyStats, setDailyStats] = useState(null);
  const [weeklyStats, setWeeklyStats] = useState(null);
  const [monthlyStats, setMonthlyStats] = useState(null);
  const [yearlyStats, setYearlyStats] = useState(null);
  
  // Fechas seleccionadas para cada vista
  const [selectedDate, setSelectedDate] = useState(format(new Date(), 'yyyy-MM-dd'));
  const [selectedWeek, setSelectedWeek] = useState(format(new Date(), 'yyyy-MM-dd'));
  const [selectedMonth, setSelectedMonth] = useState(format(new Date(), 'yyyy-MM'));
  const [selectedYear, setSelectedYear] = useState(format(new Date(), 'yyyy'));

  useEffect(() => {
    const fetchDevices = async () => {
      try {
        const response = await deviceApi.getAllDevices();
        setDevices(response.data);
      } catch (error) {
        console.error('Error al cargar dispositivos:', error);
        setError('No se pudieron cargar los dispositivos');
      }
    };

    fetchDevices();
  }, []);

  useEffect(() => {
    const fetchStats = async () => {
      setLoading(true);
      setError('');
      
      try {
        const deviceId = selectedDevice === 'all' ? null : selectedDevice;
        
        switch (tabValue) {
          case 0: // Diario
            const dailyResponse = await usageApi.getDailyStats(selectedDate, deviceId);
            setDailyStats(dailyResponse.data);
            break;
          case 1: // Semanal
            const weeklyResponse = await usageApi.getWeeklyStats(selectedWeek, deviceId);
            setWeeklyStats(weeklyResponse.data);
            break;
          case 2: // Mensual
            const monthlyResponse = await usageApi.getMonthlyStats(selectedMonth, deviceId);
            setMonthlyStats(monthlyResponse.data);
            break;
          case 3: // Anual
            const yearlyResponse = await usageApi.getYearlyStats(selectedYear, deviceId);
            setYearlyStats(yearlyResponse.data);
            break;
          default:
            break;
        }
      } catch (error) {
        console.error('Error al cargar estadísticas:', error);
        setError('No se pudieron cargar las estadísticas');
      } finally {
        setLoading(false);
      }
    };

    fetchStats();
  }, [tabValue, selectedDate, selectedWeek, selectedMonth, selectedYear, selectedDevice]);

  const handleTabChange = (event, newValue) => {
    setTabValue(newValue);
  };

  const handleDeviceChange = (event) => {
    setSelectedDevice(event.target.value);
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" component="h1">
          Historial de uso
        </Typography>
        
        <FormControl sx={{ minWidth: 200 }}>
          <InputLabel id="device-select-label">Dispositivo</InputLabel>
          <Select
            labelId="device-select-label"
            id="device-select"
            value={selectedDevice}
            label="Dispositivo"
            onChange={handleDeviceChange}
          >
            <MenuItem value="all">Todos los dispositivos</MenuItem>
            {devices.map((device) => (
              <MenuItem key={device.id} value={device.id}>
                {device.name}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
      </Box>
      
      <Paper sx={{ mb: 3 }}>
        <Tabs 
          value={tabValue} 
          onChange={handleTabChange} 
          centered
          variant="fullWidth"
        >
          <Tab label="Diario" />
          <Tab label="Semanal" />
          <Tab label="Mensual" />
          <Tab label="Anual" />
        </Tabs>
      </Paper>
      
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}
      
      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', my: 5 }}>
          <CircularProgress />
        </Box>
      ) : (
        <>
          {tabValue === 0 && dailyStats && (
            <DailyHistoryChart 
              stats={dailyStats} 
              date={selectedDate}
              setDate={setSelectedDate}
            />
          )}
          
          {tabValue === 1 && weeklyStats && (
            <WeeklyHistoryChart 
              stats={weeklyStats}
              startDate={selectedWeek}
              setStartDate={setSelectedWeek}
            />
          )}
          
          {tabValue === 2 && monthlyStats && (
            <MonthlyHistoryChart 
              stats={monthlyStats}
              month={selectedMonth}
              setMonth={setSelectedMonth}
            />
          )}
          
          {tabValue === 3 && yearlyStats && (
            <YearlyHistoryChart 
              stats={yearlyStats}
              year={selectedYear}
              setYear={setSelectedYear}
            />
          )}
        </>
      )}
    </Box>
  );
};

export default UsageHistory;