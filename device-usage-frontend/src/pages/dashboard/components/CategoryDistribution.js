// src/pages/dashboard/components/CategoryDistribution.js
import React from 'react';
import { Box, Typography, Grid, Paper, useTheme } from '@mui/material';

const CategoryDistribution = ({ categoryUsage, totalTime }) => {
  const theme = useTheme();

  // Formatea el tiempo en horas y minutos
  const formatTime = (seconds) => {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    
    if (hours > 0) {
      return `${hours}h ${minutes}m`;
    }
    return `${minutes}m`;
  };

  // Calcular porcentajes y ordenar categorías
  const categories = Object.entries(categoryUsage || {})
    .map(([name, seconds]) => ({
      name,
      seconds,
      percentage: totalTime ? (seconds / totalTime) * 100 : 0
    }))
    .sort((a, b) => b.seconds - a.seconds);

  // Colores para las categorías
  const getCategoryColor = (index) => {
    const colors = [
      theme.palette.primary.main,
      theme.palette.secondary.main,
      theme.palette.info.main,
      theme.palette.success.main,
      theme.palette.warning.main,
    ];
    return colors[index % colors.length];
  };

  return (
    <Box sx={{ mt: 2 }}>
      {categories.length > 0 ? (
        <Grid container spacing={2}>
          {categories.map((category, index) => (
            <Grid item xs={12} key={category.name}>
              <Box sx={{ mb: 1 }}>
                <Typography variant="body1" sx={{ fontWeight: 'medium' }}>
                  {category.name}
                </Typography>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <Box
                    sx={{
                      height: 8,
                      width: '100%',
                      backgroundColor: theme.palette.grey[200],
                      borderRadius: 5,
                      mr: 2,
                      overflow: 'hidden',
                    }}
                  >
                    <Box
                      sx={{
                        height: '100%',
                        width: `${category.percentage}%`,
                        backgroundColor: getCategoryColor(index),
                        borderRadius: 5,
                      }}
                    />
                  </Box>
                  <Typography variant="body2" color="text.secondary">
                    {formatTime(category.seconds)}
                  </Typography>
                </Box>
              </Box>
            </Grid>
          ))}
        </Grid>
      ) : (
        <Box sx={{ display: 'flex', height: 200, alignItems: 'center', justifyContent: 'center' }}>
          <Typography variant="body1">No hay datos para mostrar</Typography>
        </Box>
      )}
    </Box>
  );
};

export default CategoryDistribution;