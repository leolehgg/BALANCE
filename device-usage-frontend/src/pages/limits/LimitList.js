// src/pages/limits/LimitList.js
import React, { useState, useEffect } from 'react';
import {
  Box, Typography, Button, Paper, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Chip, IconButton, Dialog, DialogTitle, DialogContent,
  DialogActions, TextField, FormControl, InputLabel, Select, MenuItem,
  Grid, CircularProgress, Alert, Card, CardContent, Fab
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Timer as TimerIcon
} from '@mui/icons-material';
import { Formik, Form, Field } from 'formik';
import * as Yup from 'yup';
import limitApi from '../../api/limitApi';
import categoryApi from '../../api/categoryApi';
import applicationApi from '../../api/applicationApi.js';

const limitSchema = Yup.object({
  dailyLimitMinutes: Yup.number()
    .required('El límite diario es obligatorio')
    .min(1, 'El límite debe ser al menos 1 minuto')
    .max(1440, 'El límite no puede exceder 24 horas'),
  daysOfWeek: Yup.string()
    .matches(/^[1-7](,[1-7])*$|^$/, 'Formato de días inválido (e.g., "1,2,3,4,5")')
});

const formatDaysOfWeek = (daysString) => {
  if (!daysString) return 'Todos los días';
  
  const days = daysString.split(',').map(Number);
  const dayNames = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'];
  
  return days.map(day => dayNames[day - 1]).join(', ');
};

const LimitList = () => {
  const [limits, setLimits] = useState([]);
  const [categories, setCategories] = useState([]);
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [selectedLimit, setSelectedLimit] = useState(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [limitType, setLimitType] = useState('category');

  useEffect(() => {
    fetchLimits();
    fetchCategories();
    fetchApplications();
  }, []);

  const fetchLimits = async () => {
    setLoading(true);
    setError('');
    
    try {
      const response = await limitApi.getAllLimits();
      setLimits(response.data);
    } catch (error) {
      console.error('Error al cargar límites:', error);
      setError('No se pudieron cargar los límites de uso');
    } finally {
      setLoading(false);
    }
  };

  const fetchCategories = async () => {
    try {
      const response = await categoryApi.getAllCategories();
      setCategories(response.data);
    } catch (error) {
      console.error('Error al cargar categorías:', error);
    }
  };

  const fetchApplications = async () => {
    try {
      const response = await applicationApi.getAllApplications();
      setApplications(response.data);
    } catch (error) {
      console.error('Error al cargar aplicaciones:', error);
    }
  };

  const handleOpenDialog = (limit = null) => {
    setSelectedLimit(limit);
    setLimitType(limit ? (limit.categoryId ? 'category' : 'application') : 'category');
    setDialogOpen(true);
  };

  const handleCloseDialog = () => {
    setDialogOpen(false);
    setSelectedLimit(null);
  };

  const handleOpenDeleteDialog = (limit) => {
    setSelectedLimit(limit);
    setDeleteDialogOpen(true);
  };

  const handleCloseDeleteDialog = () => {
    setDeleteDialogOpen(false);
    setSelectedLimit(null);
  };

  const handleSubmitLimit = async (values, { setSubmitting, resetForm }) => {
    try {
      const limitData = {
        dailyLimitMinutes: values.dailyLimitMinutes,
        daysOfWeek: values.daysOfWeek
      };
      
      // Agregar categoryId o applicationId según el tipo de límite
      if (limitType === 'category') {
        limitData.categoryId = values.targetId;
      } else {
        limitData.applicationId = values.targetId;
      }
      
      if (selectedLimit) {
        // Actualizar límite
        await limitApi.updateLimit(selectedLimit.id, limitData);
      } else {
        // Crear nuevo límite
        await limitApi.createLimit(limitData);
      }
      
      resetForm();
      handleCloseDialog();
      fetchLimits();
    } catch (error) {
      console.error('Error al guardar límite:', error);
      setError('Error al guardar el límite de uso');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeleteLimit = async () => {
    if (!selectedLimit) return;
    
    try {
      await limitApi.deleteLimit(selectedLimit.id);
      handleCloseDeleteDialog();
      fetchLimits();
    } catch (error) {
      console.error('Error al eliminar límite:', error);
      setError('Error al eliminar el límite de uso');
    }
  };

  const formatTime = (minutes) => {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    
    if (hours > 0) {
      return `${hours}h ${mins > 0 ? `${mins}m` : ''}`;
    }
    return `${mins}m`;
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" component="h1">
          Límites de uso
        </Typography>
        
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => handleOpenDialog()}
        >
          Nuevo Límite
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
      ) : limits.length > 0 ? (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Tipo</TableCell>
                <TableCell>Nombre</TableCell>
                <TableCell>Límite diario</TableCell>
                <TableCell>Días aplicables</TableCell>
                <TableCell>Acciones</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {limits.map((limit) => (
                <TableRow key={limit.id}>
                  <TableCell>
                    <Chip 
                      label={limit.categoryId ? 'Categoría' : 'Aplicación'} 
                      size="small" 
                      color={limit.categoryId ? 'primary' : 'secondary'}
                    />
                  </TableCell>
                  <TableCell>
                    {limit.categoryId ? limit.categoryName : limit.applicationName}
                  </TableCell>
                  <TableCell>{formatTime(limit.dailyLimitMinutes)}</TableCell>
                  <TableCell>{formatDaysOfWeek(limit.daysOfWeek)}</TableCell>
                  <TableCell>
                    <IconButton 
                      size="small" 
                      color="primary"
                      onClick={() => handleOpenDialog(limit)}
                    >
                      <EditIcon />
                    </IconButton>
                    <IconButton 
                      size="small" 
                      color="error"
                      onClick={() => handleOpenDeleteDialog(limit)}
                    >
                      <DeleteIcon />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
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
          <TimerIcon sx={{ fontSize: 60, color: 'grey.500', mb: 2 }} />
          <Typography variant="h6" gutterBottom>
            No tienes límites de uso configurados
          </Typography>
          <Typography variant="body1" sx={{ mb: 3 }}>
            Establece límites para controlar tu tiempo de uso en categorías o aplicaciones específicas
          </Typography>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => handleOpenDialog()}
          >
            Añadir límite
          </Button>
        </Box>
      )}
      
      {/* Fab para añadir límite en móviles */}
      <Fab 
        color="primary" 
        sx={{ position: 'fixed', bottom: 16, right: 16 }}
        onClick={() => handleOpenDialog()}
      >
        <AddIcon />
      </Fab>
      
      {/* Diálogo para añadir/editar límite */}
      <Dialog 
        open={dialogOpen} 
        onClose={handleCloseDialog}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>
          {selectedLimit ? 'Editar límite' : 'Nuevo límite de uso'}
        </DialogTitle>
        <Formik
          initialValues={selectedLimit ? {
            targetId: selectedLimit.categoryId || selectedLimit.applicationId,
            dailyLimitMinutes: selectedLimit.dailyLimitMinutes,
            daysOfWeek: selectedLimit.daysOfWeek || ''
          } : {
            targetId: '',
            dailyLimitMinutes: 60,
            daysOfWeek: '1,2,3,4,5,6,7'
          }}
          validationSchema={limitSchema}
          onSubmit={handleSubmitLimit}
        >
          {({ isSubmitting, errors, touched, values, setFieldValue }) => (
            <Form>
              <DialogContent>
                <Grid container spacing={2}>
                  {!selectedLimit && (
                    <Grid item xs={12}>
                      <FormControl fullWidth>
                        <InputLabel id="limit-type-label">Tipo de límite</InputLabel>
                        <Select
                          labelId="limit-type-label"
                          value={limitType}
                          label="Tipo de límite"
                          onChange={(e) => {
                            setLimitType(e.target.value);
                            setFieldValue('targetId', '');
                          }}
                        >
                          <MenuItem value="category">Categoría</MenuItem>
                          <MenuItem value="application">Aplicación</MenuItem>
                        </Select>
                      </FormControl>
                    </Grid>
                  )}
                  
                  <Grid item xs={12}>
                    <FormControl fullWidth error={touched.targetId && Boolean(errors.targetId)}>
                      <InputLabel id="target-select-label">
                        {limitType === 'category' ? 'Categoría' : 'Aplicación'}
                      </InputLabel>
                      <Field
                        as={Select}
                        labelId="target-select-label"
                        name="targetId"
                        label={limitType === 'category' ? 'Categoría' : 'Aplicación'}
                        disabled={selectedLimit}
                      >
                        {limitType === 'category' ? (
                          categories.map((category) => (
                            <MenuItem key={category.id} value={category.id}>
                              {category.name}
                            </MenuItem>
                          ))
                        ) : (
                          applications.map((app) => (
                            <MenuItem key={app.id} value={app.id}>
                              {app.name}
                            </MenuItem>
                          ))
                        )}
                      </Field>
                    </FormControl>
                  </Grid>
                  
                  <Grid item xs={12} sm={6}>
                    <Field
                      as={TextField}
                      name="dailyLimitMinutes"
                      label="Límite diario (minutos)"
                      type="number"
                      fullWidth
                      InputProps={{ inputProps: { min: 1, max: 1440 } }}
                      error={touched.dailyLimitMinutes && Boolean(errors.dailyLimitMinutes)}
                      helperText={touched.dailyLimitMinutes && errors.dailyLimitMinutes}
                    />
                  </Grid>
                  
                  <Grid item xs={12} sm={6}>
                    <Field
                      as={TextField}
                      name="daysOfWeek"
                      label="Días de la semana (1-7)"
                      fullWidth
                      placeholder="1,2,3,4,5,6,7"
                      error={touched.daysOfWeek && Boolean(errors.daysOfWeek)}
                      helperText={touched.daysOfWeek && errors.daysOfWeek}
                    />
                    <Typography variant="caption" color="text.secondary">
                      1=Lun, 2=Mar, ..., 7=Dom. Vacío = todos los días
                    </Typography>
                  </Grid>
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
        <DialogTitle>Eliminar límite</DialogTitle>
        <DialogContent>
          <Typography>
            ¿Estás seguro de que quieres eliminar este límite de uso?
            Esta acción no se puede deshacer.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDeleteDialog}>Cancelar</Button>
          <Button 
            onClick={handleDeleteLimit} 
            color="error"
          >
            Eliminar
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default LimitList;