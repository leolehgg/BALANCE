// src/pages/dashboard/components/TopAppsChart.js
import React from 'react';
import { Box, Typography, useTheme } from '@mui/material';
import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer, Legend } from 'recharts';

const TopAppsChart = ({ applicationUsage }) => {
  const theme = useTheme();

  // Convertir datos para Recharts
  const chartData = Object.entries(applicationUsage || {})
    .map(([name, seconds]) => ({
      name,
      value: seconds,
    }))
    .sort((a, b) => b.value - a.value)
    .slice(0, 5); // Top 5 aplicaciones

  const formatTime = (seconds) => {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    
    if (hours > 0) {
      return `${hours}h ${minutes}m`;
    }
    return `${minutes}m`;
  };

  const COLORS = [
    theme.palette.primary.main,
    theme.palette.primary.light,
    theme.palette.secondary.main,
    theme.palette.secondary.light,
    theme.palette.info.main,
  ];

  const CustomTooltip = ({ active, payload }) => {
    if (active && payload && payload.length) {
      return (
        <Box sx={{ 
          backgroundColor: 'white', 
          p: 1, 
          border: `1px solid ${theme.palette.grey[300]}`,
          boxShadow: 1
        }}>
          <Typography variant="body2"><strong>{payload[0].name}</strong></Typography>
          <Typography variant="body2" sx={{ color: payload[0].color }}>
            {formatTime(payload[0].value)}
          </Typography>
        </Box>
      );
    }
    return null;
  };

  return (
    <Box sx={{ width: '100%', height: 300, mt: 2 }}>
      {chartData.length > 0 ? (
        <ResponsiveContainer>
          <PieChart>
            <Pie
              data={chartData}
              cx="50%"
              cy="50%"
              innerRadius={60}
              outerRadius={80}
              paddingAngle={5}
              dataKey="value"
              label={({ name }) => name}
            >
              {chartData.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
              ))}
            </Pie>
            <Tooltip content={<CustomTooltip />} />
          </PieChart>
        </ResponsiveContainer>
      ) : (
        <Box sx={{ display: 'flex', height: '100%', alignItems: 'center', justifyContent: 'center' }}>
          No hay datos para mostrar
        </Box>
      )}
    </Box>
  );
};

export default TopAppsChart;