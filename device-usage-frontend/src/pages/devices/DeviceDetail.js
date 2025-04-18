// src/pages/devices/DeviceDetail.js
import React from 'react';
import { Box, Typography } from '@mui/material';
import { useParams } from 'react-router-dom';

const DeviceDetail = () => {
  const { id } = useParams();
  
  return (
    <Box>
      <Typography variant="h4" component="h1" gutterBottom>
        Detalle del dispositivo {id}
      </Typography>
      <Typography>
        Esta página está en desarrollo.
      </Typography>
    </Box>
  );
};

export default DeviceDetail;