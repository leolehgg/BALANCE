// src/pages/usage/components/DailyHistoryChart.js
import React from 'react';
import { format, parseISO, subDays, addDays } from 'date-fns';
import { es } from 'date-fns/locale';
import {
  Box, Typography, Paper, Grid, IconButton, 
  TextField, Card, CardContent, Divider
} from '@mui/material';
import { ArrowBack, ArrowForward, DateRange } from '@mui/icons-material';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

const DailyHistoryChart = ({ stats, date, setDate }) => {
  const handleDateChange = (event) => {
    setDate(event.target.value);
  };
  
  const goToPreviousDay = () => {
    const newDate = format(subDays(parseISO(date), 1), 'yyyy-MM-dd');
    setDate(newDate);
  };
  
  const goToNextDay = () => {
    const currentDate = new Date().toISOString().split('T')[0];
    // No permitir seleccionar fechas futuras
    if (date < currentDate) {
      const newDate = format(addDays(parseISO(date), 1), 'yyyy-MM-dd');
      setDate(newDate);
    }
  };
  
  const formatTime = (seconds) => {
    if (!seconds) return '0h 0m';
    
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    
    return `${hours}h ${minutes}m`;
  };
  
  // Datos para el gráfico de uso por horas del día
  const appUsageData = stats?.applicationUsage 
    ? Object.entries(stats.applicationUsage)
        .map(([name, seconds]) => ({ name, value: seconds / 3600 }))
        .sort((a, b) => b.value - a.value)
        .slice(0, 10)
    : [];
  
  const formattedDate = format(parseISO(date), "EEEE, d 'de' MMMM, yyyy", { locale: es });
  
  return (
    <Box sx={{ width: '100%' }}>
      <Paper sx={{ p: 3, mb: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
          <IconButton onClick={goToPreviousDay}>
            <ArrowBack />
          </IconButton>
          
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <DateRange sx={{ mr: 1 }} />
            <Typography variant="h6" sx={{ mr: 2, textTransform: 'capitalize' }}>
              {formattedDate}
            </Typography>
            <TextField
              type="date"
              value={date}
              onChange={handleDateChange}
              size="small"
              InputLabelProps={{ shrink: true }}
            />
          </Box>
          
          <IconButton onClick={goToNextDay} disabled={date >= new Date().toISOString().split('T')[0]}>
            <ArrowForward />
          </IconButton>
        </Box>
        
        <Grid container spacing={3}>
          <Grid item xs={12} md={4}>
            <Card>
              <CardContent>
                <Typography variant="h6">Tiempo total de uso</Typography>
                <Typography variant="h2" color="primary" sx={{ mt: 2 }}>
                  {formatTime(stats?.totalUsageSeconds)}
                </Typography>
                
                <Divider sx={{ my: 2 }} />
                
                <Typography variant="body2">
                  <strong>Tiempo productivo:</strong> {formatTime(stats?.productiveTimeSeconds)}
                </Typography>
                <Typography variant="body2">
                  <strong>Tiempo no productivo:</strong> {formatTime(stats?.nonProductiveTimeSeconds)}
                </Typography>
              </CardContent>
            </Card>
          </Grid>
          
          <Grid item xs={12} md={8}>
            <Typography variant="h6" gutterBottom>
              Top aplicaciones por tiempo de uso
            </Typography>
            <Box sx={{ height: 300 }}>
              <ResponsiveContainer width="100%" height="100%">
                <BarChart
                  data={appUsageData}
                  margin={{ top: 20, right: 30, left: 20, bottom: 70 }}
                >
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis 
                    dataKey="name" 
                    angle={-45} 
                    textAnchor="end" 
                    height={70}
                  />
                  <YAxis label={{ value: 'Horas', angle: -90, position: 'insideLeft' }} />
                  <Tooltip 
                    formatter={(value) => [`${value.toFixed(2)} horas`, 'Tiempo de uso']}
                  />
                  <Legend />
                  <Bar dataKey="value" name="Horas de uso" fill="#8884d8" />
                </BarChart>
              </ResponsiveContainer>
            </Box>
          </Grid>
        </Grid>
      </Paper>
    </Box>
  );
};

export default DailyHistoryChart;