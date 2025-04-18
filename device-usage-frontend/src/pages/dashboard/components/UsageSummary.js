// src/pages/dashboard/components/UsageSummary.js
import React from 'react';
import { Box, Typography, LinearProgress, Grid } from '@mui/material';

const UsageSummary = ({ productiveTime, nonProductiveTime, totalTime }) => {
  const productivePercentage = totalTime ? Math.round((productiveTime / totalTime) * 100) : 0;
  const nonProductivePercentage = totalTime ? Math.round((nonProductiveTime / totalTime) * 100) : 0;

  const formatTime = (seconds) => {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    
    return `${hours}h ${minutes}m`;
  };

  return (
    <Grid container spacing={2}>
      <Grid item xs={12}>
        <Typography variant="subtitle1">Tiempo productivo</Typography>
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
          <Box sx={{ width: '100%', mr: 1 }}>
            <LinearProgress 
              variant="determinate" 
              value={productivePercentage} 
              color="success"
              sx={{ height: 10, borderRadius: 5 }}
            />
          </Box>
          <Box sx={{ minWidth: 35 }}>
            <Typography variant="body2" color="text.secondary">{`${productivePercentage}%`}</Typography>
          </Box>
        </Box>
        <Typography variant="body2" color="text.secondary">
          {formatTime(productiveTime)}
        </Typography>
      </Grid>
      
      <Grid item xs={12}>
        <Typography variant="subtitle1">Tiempo no productivo</Typography>
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
          <Box sx={{ width: '100%', mr: 1 }}>
            <LinearProgress 
              variant="determinate" 
              value={nonProductivePercentage} 
              color="error"
              sx={{ height: 10, borderRadius: 5 }}
            />
          </Box>
          <Box sx={{ minWidth: 35 }}>
            <Typography variant="body2" color="text.secondary">{`${nonProductivePercentage}%`}</Typography>
          </Box>
        </Box>
        <Typography variant="body2" color="text.secondary">
          {formatTime(nonProductiveTime)}
        </Typography>
      </Grid>
    </Grid>
  );
};

export default UsageSummary;