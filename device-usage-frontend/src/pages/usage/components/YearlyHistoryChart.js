// src/pages/usage/components/YearlyHistoryChart.js
import React from 'react';
import { format, subYears, addYears } from 'date-fns';
import { es } from 'date-fns/locale';
import {
  Box, Typography, Paper, Grid, IconButton, 
  TextField, Card, CardContent, Divider
} from '@mui/material';
import { ArrowBack, ArrowForward, CalendarToday } from '@mui/icons-material';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, 
  LineChart, Line, PieChart, Pie, Cell } from 'recharts';

const YearlyHistoryChart = ({ stats, year, setYear }) => {
  const handleYearChange = (event) => {
    setYear(event.target.value);
  };
  
  const goToPreviousYear = () => {
    const newYear = (parseInt(year) - 1).toString();
    setYear(newYear);
  };
  
  const goToNextYear = () => {
    const currentYear = new Date().getFullYear().toString();
    // No permitir seleccionar años futuros
    if (year < currentYear) {
      const newYear = (parseInt(year) + 1).toString();
      setYear(newYear);
    }
  };
  
  const formatTime = (seconds) => {
    if (!seconds) return '0h 0m';
    
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    
    return `${hours}h ${minutes}m`;
  };
  
  // Nombres de los meses en español
  const months = [
    'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
    'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'
  ];
  
  // Preparar datos para el gráfico de tendencia mensual
  const monthlyTrendData = stats?.monthlyUsageTrend 
    ? Object.entries(stats.monthlyUsageTrend).map(([month, seconds]) => ({
        name: months[parseInt(month) - 1],
        value: seconds / 3600
      }))
    : [];
  
  // Datos para el gráfico de aplicaciones
  const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884D8'];
  
  return (
    <Box sx={{ width: '100%' }}>
      <Paper sx={{ p: 3, mb: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
          <IconButton onClick={goToPreviousYear}>
            <ArrowBack />
          </IconButton>
          
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <CalendarToday sx={{ mr: 1 }} />
            <Typography variant="h6" sx={{ mr: 2 }}>
              {year}
            </Typography>
            <TextField
              type="number"
              value={year}
              onChange={handleYearChange}
              size="small"
              InputLabelProps={{ shrink: true }}
              inputProps={{ min: "2020", max: new Date().getFullYear() }}
            />
          </Box>
          
          <IconButton onClick={goToNextYear} disabled={year >= new Date().getFullYear().toString()}>
            <ArrowForward />
          </IconButton>
        </Box>
        
        <Grid container spacing={3}>
          <Grid item xs={12} md={4}>
            <Card>
              <CardContent>
                <Typography variant="h6">Resumen anual</Typography>
                <Typography variant="h3" color="primary" sx={{ mt: 2 }}>
                  {formatTime(stats?.totalUsageSeconds)}
                </Typography>
                
                <Divider sx={{ my: 2 }} />
                
                <Typography variant="body2">
                  <strong>Media mensual:</strong> {formatTime(stats?.totalUsageSeconds / Object.keys(stats?.monthlyStats || {}).length)}
                </Typography>
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
              Tendencia mensual
            </Typography>
            <Box sx={{ height: 300 }}>
              <ResponsiveContainer width="100%" height="100%">
                <BarChart
                  data={monthlyTrendData}
                  margin={{ top: 20, right: 30, left: 20, bottom: 10 }}
                >
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="name" />
                  <YAxis label={{ value: 'Horas', angle: -90, position: 'insideLeft' }} />
                  <Tooltip formatter={(value) => [`${value.toFixed(2)} horas`, 'Tiempo de uso']} />
                  <Legend />
                  <Bar dataKey="value" name="Horas de uso" fill="#8884d8" />
                </BarChart>
              </ResponsiveContainer>
            </Box>
          </Grid>
          
          <Grid item xs={12} md={6}>
            <Typography variant="h6" gutterBottom>
              Top 5 aplicaciones
            </Typography>
            <Box sx={{ height: 300 }}>
              {stats?.topApplications && stats.topApplications.length > 0 ? (
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={stats.topApplications.map(app => ({
                        name: app.name,
                        value: app.durationSeconds / 3600
                      }))}
                      cx="50%"
                      cy="50%"
                      labelLine={true}
                      outerRadius={100}
                      fill="#8884d8"
                      dataKey="value"
                      label={({name, percent}) => `${name} (${(percent * 100).toFixed(0)}%)`}
                    >
                      {
                        stats.topApplications.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                        ))
                      }
                    </Pie>
                    <Tooltip formatter={(value) => [`${value.toFixed(2)} horas`, 'Tiempo de uso']} />
                  </PieChart>
                </ResponsiveContainer>
              ) : (
                <Box sx={{ display: 'flex', height: '100%', alignItems: 'center', justifyContent: 'center' }}>
                  <Typography>No hay datos para mostrar</Typography>
                </Box>
              )}
            </Box>
          </Grid>
        </Grid>
      </Paper>
    </Box>
  );
};

export default YearlyHistoryChart;