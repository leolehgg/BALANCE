// src/pages/dashboard/Dashboard.js
import React, { useState, useEffect } from 'react';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import {
  Box, Typography, Grid, Paper, Divider, 
  FormControl, Select, MenuItem, InputLabel,
  CircularProgress, Alert
} from '@mui/material';
import usageApi from '../../api/usageApi';
import deviceApi from '../../api/deviceApi';
import DailyUsageChart from './components/DailyUsageChart';
import TopAppsChart from './components/TopAppsChart';
import UsageSummary from './components/UsageSummary';
import CategoryDistribution from './components/CategoryDistribution';

const Dashboard = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [dailyStats, setDailyStats] = useState(null);
  const [devices, setDevices] = useState([]);
  const [selectedDevice, setSelectedDevice] = useState('all');
  const [selectedDate, setSelectedDate] = useState(format(new Date(), 'yyyy-MM-dd'));

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
    const fetchDailyStats = async () => {
      setLoading(true);
      setError('');
      
      try {
        const deviceId = selectedDevice === 'all' ? null : selectedDevice;
        const response = await usageApi.getDailyStats(selectedDate, deviceId);
        setDailyStats(response.data);
      } catch (error) {
        console.error('Error al cargar estadísticas:', error);
        setError('No se pudieron cargar las estadísticas del día');
      } finally {
        setLoading(false);
      }
    };

    fetchDailyStats();
  }, [selectedDate, selectedDevice]);

  const handleDeviceChange = (event) => {
    setSelectedDevice(event.target.value);
  };

  const formatDate = (dateString) => {
    return format(new Date(dateString), "d 'de' MMMM, yyyy", { locale: es });
  };

  const formatTime = (seconds) => {
    if (!seconds) return '0h 0m';
    
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    
    return `${hours}h ${minutes}m`;
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" component="h1">
          Dashboard
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
      
      <Typography variant="h6" sx={{ mb: 2 }}>
        Estadísticas del {formatDate(selectedDate)}
      </Typography>
      
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}
      
      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', my: 5 }}>
          <CircularProgress />
        </Box>
      ) : dailyStats ? (
        <Grid container spacing={3}>
          <Grid item xs={12} md={6}>
            <Paper sx={{ p: 3, height: '100%' }}>
            // src/pages/dashboard/Dashboard.js (continuación)
              <Typography variant="h6">Tiempo de uso total</Typography>
              <Typography variant="h2" sx={{ my: 2, color: 'primary.main' }}>
                {formatTime(dailyStats.totalUsageSeconds)}
              </Typography>
              <Divider sx={{ my: 2 }} />
              <UsageSummary
                productiveTime={dailyStats.productiveTimeSeconds}
                nonProductiveTime={dailyStats.nonProductiveTimeSeconds}
                totalTime={dailyStats.totalUsageSeconds}
              />
            </Paper>
          </Grid>
          
          <Grid item xs={12} md={6}>
            <Paper sx={{ p: 3, height: '100%' }}>
              <Typography variant="h6">Uso por categoría</Typography>
              <CategoryDistribution
                categoryUsage={dailyStats.categoryUsage}
                totalTime={dailyStats.totalUsageSeconds}
              />
            </Paper>
          </Grid>
          
          <Grid item xs={12} md={8}>
            <Paper sx={{ p: 3 }}>
              <Typography variant="h6">Aplicaciones más usadas</Typography>
              <DailyUsageChart applicationUsage={dailyStats.applicationUsage} />
            </Paper>
          </Grid>
          
          <Grid item xs={12} md={4}>
            <Paper sx={{ p: 3, height: '100%' }}>
              <Typography variant="h6">Top 5 aplicaciones</Typography>
              <TopAppsChart applicationUsage={dailyStats.applicationUsage} />
            </Paper>
          </Grid>
        </Grid>
      ) : (
        <Box sx={{ p: 3, textAlign: 'center' }}>
          <Typography variant="body1">No hay datos para mostrar.</Typography>
        </Box>
      )}
    </Box>
  );
};

export default Dashboard;