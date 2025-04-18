// src/pages/dashboard/components/DailyUsageChart.js
import React from 'react';
import { Box, useTheme } from '@mui/material';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

const DailyUsageChart = ({ applicationUsage }) => {
  const theme = useTheme();

  // Convertir datos para Recharts
  const chartData = Object.entries(applicationUsage || {})
    .map(([name, seconds]) => ({
      name,
      minutes: Math.round(seconds / 60),
    }))
    .sort((a, b) => b.minutes - a.minutes)
    .slice(0, 10); // Top 10 aplicaciones

  const formatMinutes = (minutes) => {
    if (minutes >= 60) {
      const hours = Math.floor(minutes / 60);
      const mins = minutes % 60;
      return `${hours}h ${mins > 0 ? `${mins}m` : ''}`;
    }
    return `${minutes}m`;
  };

  const CustomTooltip = ({ active, payload, label }) => {
    if (active && payload && payload.length) {
      return (
        <Box sx={{ 
          backgroundColor: 'white', 
          p: 1, 
          border: `1px solid ${theme.palette.grey[300]}`,
          boxShadow: 1
        }}>
          <p style={{ margin: 0 }}><strong>{label}</strong></p>
          <p style={{ margin: 0, color: theme.palette.primary.main }}>
            {formatMinutes(payload[0].value)}
          </p>
        </Box>
      );
    }
    return null;
  };

  return (
    <Box sx={{ width: '100%', height: 300, mt: 2 }}>
      {chartData.length > 0 ? (
        <ResponsiveContainer>
          <BarChart
            data={chartData}
            margin={{ top: 20, right: 30, left: 20, bottom: 60 }}
          >
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis 
              dataKey="name" 
              angle={-45} 
              textAnchor="end" 
              height={70} 
            />
            <YAxis 
              tickFormatter={formatMinutes}
              label={{ 
                value: 'Tiempo (minutos)', 
                angle: -90, 
                position: 'insideLeft' 
              }} 
            />
            <Tooltip content={<CustomTooltip />} />
            <Bar 
              dataKey="minutes" 
              fill={theme.palette.primary.main} 
              barSize={30} 
              radius={[4, 4, 0, 0]} 
            />
          </BarChart>
        </ResponsiveContainer>
      ) : (
        <Box sx={{ display: 'flex', height: '100%', alignItems: 'center', justifyContent: 'center' }}>
          No hay datos para mostrar
        </Box>
      )}
    </Box>
  );
};

export default DailyUsageChart;