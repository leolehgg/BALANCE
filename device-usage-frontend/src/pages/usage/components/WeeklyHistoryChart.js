// src/pages/usage/components/WeeklyHistoryChart.js
import React from 'react';
import { format, parseISO, subWeeks, addWeeks, startOfWeek, endOfWeek } from 'date-fns';
import { es } from 'date-fns/locale';
import {
  Box, Typography, Paper, Grid, IconButton, 
  TextField, Card, CardContent, Divider
} from '@mui/material';
import { ArrowBack, ArrowForward, DateRange } from '@mui/icons-material';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, 
  LineChart, Line, PieChart, Pie, Cell } from 'recharts';

const WeeklyHistoryChart = ({ stats, startDate, setStartDate }) => {
  const handleDateChange = (event) => {
    setStartDate(event.target.value);
  };
  
  const goToPreviousWeek = () => {
    const newDate = format(subWeeks(parseISO(startDate), 1), 'yyyy-MM-dd');
    setStartDate(newDate);
  };
  
  const goToNextWeek = () => {
    const currentDate = new Date().toISOString().split('T')[0];
    // No permitir seleccionar fechas futuras
    if (startDate < currentDate) {
      const newDate = format(addWeeks(parseISO(startDate), 1), 'yyyy-MM-dd');
      setStartDate(newDate);
    }
  };
  
  const formatTime = (seconds) => {
    if (!seconds) return '0h 0m';
    
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    
    return `${hours}h ${minutes}m`;
  };
  
  // Formatear fechas para mostrar
  const weekStart = startOfWeek(parseISO(startDate), { locale: es });
  const weekEnd = endOfWeek(parseISO(startDate), { locale: es });
  const formattedDateRange = `${format(weekStart, "d 'de' MMMM", { locale: es })} - ${format(weekEnd, "d 'de' MMMM, yyyy", { locale: es })}`;
  
  // Preparar datos para el gráfico de tendencia diaria
  const dailyTrendData = stats?.dailyStats 
    ? Object.entries(stats.dailyStats).map(([day, data]) => ({
        name: format(parseISO(data.date), 'EEEE', { locale: es }),
        value: data.totalUsageSeconds / 3600,
        productive: data.productiveTimeSeconds / 3600,
        nonProductive: data.nonProductiveTimeSeconds / 3600
      }))
    : [];
  
  // Datos para el gráfico de aplicaciones
  const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884D8'];
  
  return (
    <Box sx={{ width: '100%' }}>
      <Paper sx={{ p: 3, mb: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
          <IconButton onClick={goToPreviousWeek}>
            <ArrowBack />
          </IconButton>
          
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <DateRange sx={{ mr: 1 }} />
            <Typography variant="h6" sx={{ mr: 2 }}>
              {formattedDateRange}
            </Typography>
            <TextField
              type="date"
              value={startDate}
              onChange={handleDateChange}
              size="small"
              InputLabelProps={{ shrink: true }}
            />
          </Box>
          
          <IconButton onClick={goToNextWeek} disabled={startDate >= new Date().toISOString().split('T')[0]}>
            <ArrowForward />
          </IconButton>
        </Box>
        
        <Grid container spacing={3}>
          <Grid item xs={12} md={4}>
            <Card>
              <CardContent>
                <Typography variant="h6">Resumen semanal</Typography>
                <Typography variant="h3" color="primary" sx={{ mt: 2 }}>
                  {formatTime(stats?.totalUsageSeconds)}
                </Typography>
                
                <Divider sx={{ my: 2 }} />
                
                <Typography variant="body2">
                  <strong>Media diaria:</strong> {formatTime(stats?.totalUsageSeconds / 7)}
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
              Tendencia diaria
            </Typography>
            <Box sx={{ height: 300 }}>
              <ResponsiveContainer width="100%" height="100%">
                <LineChart
                  data={dailyTrendData}
                  margin={{ top: 20, right: 30, left: 20, bottom: 10 }}
                >
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="name" />
                  <YAxis label={{ value: 'Horas', angle: -90, position: 'insideLeft' }} />
                  <Tooltip />
                  <Legend />
                  <Line type="monotone" dataKey="value" name="Total" stroke="#8884d8" />
                  <Line type="monotone" dataKey="productive" name="Productivo" stroke="#82ca9d" />
                  <Line type="monotone" dataKey="nonProductive" name="No productivo" stroke="#ff8042" />
                </LineChart>
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

export default WeeklyHistoryChart;