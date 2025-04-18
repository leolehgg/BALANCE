// src/pages/devices/DeviceList.js
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box, Typography, Button, Grid, Card, CardContent,
  CardActions, Chip, Avatar, IconButton, Fab,
  Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, MenuItem, FormControl, InputLabel, Select,
  CircularProgress, Alert
} from '@mui/material';
import {
  Add as AddIcon,
  Smartphone as SmartphoneIcon,
  Laptop as LaptopIcon,
  Watch as WatchIcon,
  Tablet as TabletIcon,
  Devices as DevicesIcon,
  Edit as EditIcon,
  Delete as DeleteIcon
} from '@mui/icons-material';
import { DeviceType } from '../../utils/constants';
import deviceApi from '../../api/deviceApi';
import { Formik, Form, Field } from 'formik';
import * as Yup from 'yup';

const deviceIcons = {
  [DeviceType.SMARTPHONE]: <SmartphoneIcon />,
  [DeviceType.COMPUTER]: <LaptopIcon />,
  [DeviceType.TABLET]: <TabletIcon />,
  [DeviceType.SMARTWATCH]: <WatchIcon />,
  [DeviceType.OTHER]: <DevicesIcon />
};

const deviceSchema = Yup.object({
  name: Yup.string()
    .required('El nombre es obligatorio')
    .max(100, 'El nombre no debe exceder los 100 caracteres'),
  deviceUuid: Yup.string()
    .required('El UUID es obligatorio'),
  type: Yup.string()
    .required('El tipo es obligatorio'),
  platform: Yup.string()
    .required('La plataforma es obligatoria'),
  version: Yup.string()
    .required('La versión es obligatoria')
});

const DeviceList = () => {
  const [devices, setDevices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [selectedDevice, setSelectedDevice] = useState(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    fetchDevices();
  }, []);

  const fetchDevices = async () => {
    setLoading(true);
    setError('');
    
    try {
      const response = await deviceApi.getAllDevices();
      setDevices(response.data);
    } catch (error) {
      console.error('Error al cargar dispositivos:', error);
      setError('No se pudieron cargar los dispositivos');
    } finally {
      setLoading(false);
    }
  };

  const handleOpenDialog = (device = null) => {
    setSelectedDevice(device);
    setDialogOpen(true);
  };

  const handleCloseDialog = () => {
    setDialogOpen(false);
    setSelectedDevice(null);
  };

  const handleOpenDeleteDialog = (device) => {
    setSelectedDevice(device);
    setDeleteDialogOpen(true);
  };

  const handleCloseDeleteDialog = () => {
    setDeleteDialogOpen(false);
    setSelectedDevice(null);
  };

  const handleSubmitDevice = async (values, { setSubmitting, resetForm }) => {
    try {
      if (selectedDevice) {
        // Actualizar dispositivo
        await deviceApi.updateDevice(selectedDevice.id, values);
      } else {
        // Registrar nuevo dispositivo
        await deviceApi.registerDevice(values);
      }
      
      resetForm();
      handleCloseDialog();
      fetchDevices();
    } catch (error) {
      console.error('Error al guardar dispositivo:', error);
      setError('Error al guardar el dispositivo');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeleteDevice = async () => {
    if (!selectedDevice) return;
    
    try {
      await deviceApi.deleteDevice(selectedDevice.id);
      handleCloseDeleteDialog();
      fetchDevices();
    } catch (error) {
      console.error('Error al eliminar dispositivo:', error);
      setError('Error al eliminar el dispositivo');
    }
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" component="h1">
          Mis Dispositivos
        </Typography>
        
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => handleOpenDialog()}
        >
          Nuevo Dispositivo
        </Button>
      </Box>
      
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}
      
      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', my: 5 }}>
          <CircularProgress />
        </Box>
      ) : devices.length > 0 ? (
        <Grid container spacing={3}>
          {devices.map((device) => (
            <Grid item xs={12} sm={6} md={4} key={device.id}>
              <Card 
                sx={{ 
                  height: '100%', 
                  display: 'flex', 
                  flexDirection: 'column',
                  opacity: device.active ? 1 : 0.6
                }}
              >
                <CardContent sx={{ flexGrow: 1 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                    <Avatar
                      sx={{ 
                        bgcolor: device.active ? 'primary.main' : 'grey.400',
                        mr: 2
                      }}
                    >
                      {deviceIcons[device.type] || <DevicesIcon />}
                    </Avatar>
                    <Typography variant="h6" component="div">
                      {device.name}
                    </Typography>
                  </Box>
                  
                  <Box sx={{ mb: 1 }}>
                    <Chip 
                      label={device.type} 
                      size="small" 
                      sx={{ mr: 1, mb: 1 }} 
                    />
                    <Chip 
                      label={device.platform} 
                      size="small" 
                      sx={{ mr: 1, mb: 1 }} 
                    />
                    <Chip 
                      label={`v${device.version}`} 
                      size="small" 
                      sx={{ mb: 1 }} 
                    />
                  </Box>
                  
                  <Typography variant="body2" color="text.secondary">
                    Última sincronización: {device.lastSync ? new Date(device.lastSync).toLocaleString() : 'Nunca'}
                  </Typography>
                  
                  {!device.active && (
                    <Typography variant="body2" color="error" sx={{ mt: 1 }}>
                      Dispositivo inactivo
                    </Typography>
                  )}
                </CardContent>
                <CardActions>
                  <IconButton 
                    size="small" 
                    color="primary"
                    onClick={() => handleOpenDialog(device)}
                  >
                    <EditIcon />
                  </IconButton>
                  <IconButton 
                    size="small" 
                    color="error"
                    onClick={() => handleOpenDeleteDialog(device)}
                  >
                    <DeleteIcon />
                  </IconButton>
                </CardActions>
              </Card>
            </Grid>
          ))}
        </Grid>
      ) : (
        <Box 
          sx={{ 
            p: 5, 
            display: 'flex', 
            flexDirection: 'column', 
            alignItems: 'center',
            justifyContent: 'center',
            textAlign: 'center',
            backgroundColor: 'grey.100',
            borderRadius: 2
          }}
        >
          <DevicesIcon sx={{ fontSize: 60, color: 'grey.500', mb: 2 }} />
          <Typography variant="h6" gutterBottom>
            No tienes dispositivos registrados
          </Typography>
          <Typography variant="body1" sx={{ mb: 3 }}>
            Añade tu primer dispositivo para empezar a hacer seguimiento del tiempo de uso
          </Typography>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => handleOpenDialog()}
          >
            Añadir dispositivo
          </Button>
        </Box>
      )}
      
      {/* Fab para añadir dispositivo en móviles */}
      <Fab 
        color="primary" 
        sx={{ position: 'fixed', bottom: 16, right: 16 }}
        onClick={() => handleOpenDialog()}
      >
        <AddIcon />
      </Fab>
      
      {/* Diálogo para añadir/editar dispositivo */}
      <Dialog 
        open={dialogOpen} 
        onClose={handleCloseDialog}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>
          {selectedDevice ? `Editar ${selectedDevice.name}` : 'Nuevo Dispositivo'}
        </DialogTitle>
        <Formik
          initialValues={selectedDevice ? {
            name: selectedDevice.name,
            deviceUuid: selectedDevice.deviceUuid,
            type: selectedDevice.type,
            platform: selectedDevice.platform,
            version: selectedDevice.version,
            active: selectedDevice.active
          } : {
            name: '',
            deviceUuid: crypto.randomUUID(),
            type: DeviceType.SMARTPHONE,
            platform: '',
            version: '',
            active: true
          }}
          validationSchema={deviceSchema}
          onSubmit={handleSubmitDevice}
        >
          {({ isSubmitting, errors, touched, values, setFieldValue }) => (
            <Form>
              <DialogContent>
                <Grid container spacing={2}>
                  <Grid item xs={12}>
                    <Field
                      as={TextField}
                      name="name"
                      label="Nombre del dispositivo"
                      fullWidth
                      error={touched.name && Boolean(errors.name)}
                      helperText={touched.name && errors.name}
                    />
                  </Grid>
                  
                  <Grid item xs={12}>
                    <Field
                      as={TextField}
                      name="deviceUuid"
                      label="UUID del dispositivo"
                      fullWidth
                      disabled={selectedDevice}
                      error={touched.deviceUuid && Boolean(errors.deviceUuid)}
                      helperText={touched.deviceUuid && errors.deviceUuid}
                    />
                  </Grid>
                  
                  <Grid item xs={12} sm={4}>
                    <FormControl fullWidth>
                      <InputLabel id="device-type-label">Tipo</InputLabel>
                      <Field
                        as={Select}
                        labelId="device-type-label"
                        name="type"
                        label="Tipo"
                        error={touched.type && Boolean(errors.type)}
                      >
                        <MenuItem value={DeviceType.SMARTPHONE}>Smartphone</MenuItem>
                        <MenuItem value={DeviceType.TABLET}>Tablet</MenuItem>
                        <MenuItem value={DeviceType.COMPUTER}>Ordenador</MenuItem>
                        <MenuItem value={DeviceType.SMARTWATCH}>Smartwatch</MenuItem>
                        <MenuItem value={DeviceType.OTHER}>Otro</MenuItem>
                      </Field>
                    </FormControl>
                  </Grid>
                  
                  <Grid item xs={12} sm={4}>
                    <Field
                      as={TextField}
                      name="platform"
                      label="Plataforma"
                      fullWidth
                      error={touched.platform && Boolean(errors.platform)}
                      helperText={touched.platform && errors.platform}
                    />
                  </Grid>
                  
                  <Grid item xs={12} sm={4}>
                    <Field
                      as={TextField}
                      name="version"
                      label="Versión"
                      fullWidth
                      error={touched.version && Boolean(errors.version)}
                      helperText={touched.version && errors.version}
                    />
                  </Grid>
                  
                  {selectedDevice && (
                    <Grid item xs={12}>
                      <FormControl fullWidth>
                        <InputLabel id="device-status-label">Estado</InputLabel>
                        <Select
                          labelId="device-status-label"
                          value={values.active}
                          label="Estado"
                          onChange={(e) => setFieldValue('active', e.target.value)}
                        >
                          <MenuItem value={true}>Activo</MenuItem>
                          <MenuItem value={false}>Inactivo</MenuItem>
                        </Select>
                      </FormControl>
                    </Grid>
                  )}
                </Grid>
              </DialogContent>
              <DialogActions>
                <Button onClick={handleCloseDialog}>Cancelar</Button>
                <Button 
                  type="submit" 
                  variant="contained" 
                  disabled={isSubmitting}
                >
                  {isSubmitting ? <CircularProgress size={24} /> : 'Guardar'}
                </Button>
              </DialogActions>
            </Form>
          )}
        </Formik>
      </Dialog>
      
      {/* Diálogo de confirmación para eliminar */}
      <Dialog
        open={deleteDialogOpen}
        onClose={handleCloseDeleteDialog}
      >
        <DialogTitle>Eliminar dispositivo</DialogTitle>
        <DialogContent>
          <Typography>
            ¿Estás seguro de que quieres eliminar el dispositivo "{selectedDevice?.name}"?
            Esta acción no se puede deshacer.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDeleteDialog}>Cancelar</Button>
          <Button 
            onClick={handleDeleteDevice} 
            color="error"
          >
            Eliminar
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default DeviceList;