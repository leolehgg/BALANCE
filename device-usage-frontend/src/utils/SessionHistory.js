// src/pages/usage/SessionHistory.js
import React, { useState, useEffect } from 'react';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import {
  Box, Typography, Paper, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, TablePagination, Chip, IconButton,
  FormControl, InputLabel, Select, MenuItem, TextField, Button,
  Grid, CircularProgress, Alert
} from '@mui/material';
import { 
  Search as SearchIcon, 
  Refresh as RefreshIcon 
} from '@mui/icons-material';
import { DatePicker } from '@mui/x-date-pickers';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import usageApi from '../../api/usageApi';
import deviceApi from '../../api/deviceApi';

const SessionHistory = () => {
  const [sessions, setSessions] = useState([]);
  const [devices, setDevices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [totalElements, setTotalElements] = useState(0);
  const [filters, setFilters] = useState({
    deviceId: '',
    startDate: null,
    endDate: null
  });

  useEffect(() => {
    fetchDevices();
    fetchSessions();
  }, [page, rowsPerPage]);

  const fetchDevices = async () => {
    try {
      const response = await deviceApi.getAllDevices();
      setDevices(response.data);
    } catch (error) {
      console.error('Error al cargar dispositivos:', error);
      setError('No se pudieron cargar los dispositivos');
    }
  };

  const fetchSessions = async () => {
    setLoading(true);
    setError('');
    
    try {
      const response = await usageApi.getSessions(
        page, 
        rowsPerPage, 
        {
          deviceId: filters.deviceId || null,
          startDate: filters.startDate ? format(filters.startDate, 'yyyy-MM-dd') : null,
          endDate: filters.endDate ? format(filters.endDate, 'yyyy-MM-dd') : null
        }
      );
      
      setSessions(response.data.content);
      setTotalElements(response.data.totalElements);
    } catch (error) {
      console.error('Error al cargar sesiones:', error);
      setError('No se pudieron cargar las sesiones');
    } finally {
      setLoading(false);
    }
  };

  const handleChangePage = (event, newPage) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

// src/pages/usage/SessionHistory.js (continuación)
const handleFilterChange = (event) => {
    setFilters({
      ...filters,
      [event.target.name]: event.target.value
    });
  };

  const handleDateChange = (name, date) => {
    setFilters({
      ...filters,
      [name]: date
    });
  };

  const handleApplyFilters = () => {
    setPage(0); // Resetear a la primera página
    fetchSessions();
  };

  const handleResetFilters = () => {
    setFilters({
      deviceId: '',
      startDate: null,
      endDate: null
    });
    setPage(0);
    // Aplicar filtros reseteados
    setTimeout(() => {
      fetchSessions();
    }, 0);
  };

  const formatDuration = (seconds) => {
    if (!seconds) return '-';
    
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = seconds % 60;
    
    return `${hours > 0 ? `${hours}h ` : ''}${minutes > 0 ? `${minutes}m ` : ''}${secs}s`;
  };

  const formatDateTime = (dateTimeStr) => {
    if (!dateTimeStr) return '-';
    return format(new Date(dateTimeStr), 'dd/MM/yyyy HH:mm:ss');
  };

  return (
    <Box>
      <Typography variant="h4" component="h1" gutterBottom>
        Historial de sesiones
      </Typography>
      
      {/* Filtros */}
      <Paper sx={{ p: 2, mb: 3 }}>
        <Grid container spacing={2} alignItems="flex-end">
          <Grid item xs={12} sm={6} md={3}>
            <FormControl fullWidth variant="outlined">
              <InputLabel id="device-filter-label">Dispositivo</InputLabel>
              <Select
                labelId="device-filter-label"
                name="deviceId"
                value={filters.deviceId}
                onChange={handleFilterChange}
                label="Dispositivo"
              >
                <MenuItem value="">Todos los dispositivos</MenuItem>
                {devices.map((device) => (
                  <MenuItem key={device.id} value={device.id}>
                    {device.name}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>
          
          <Grid item xs={12} sm={6} md={3}>
            <LocalizationProvider dateAdapter={AdapterDateFns} adapterLocale={es}>
              <DatePicker
                label="Fecha inicio"
                value={filters.startDate}
                onChange={(date) => handleDateChange('startDate', date)}
                renderInput={(params) => <TextField {...params} fullWidth />}
              />
            </LocalizationProvider>
          </Grid>
          
          <Grid item xs={12} sm={6} md={3}>
            <LocalizationProvider dateAdapter={AdapterDateFns} adapterLocale={es}>
              <DatePicker
                label="Fecha fin"
                value={filters.endDate}
                onChange={(date) => handleDateChange('endDate', date)}
                renderInput={(params) => <TextField {...params} fullWidth />}
              />
            </LocalizationProvider>
          </Grid>
          
          <Grid item xs={12} sm={6} md={3}>
            <Box sx={{ display: 'flex', gap: 1 }}>
              <Button
                variant="contained"
                startIcon={<SearchIcon />}
                onClick={handleApplyFilters}
              >
                Filtrar
              </Button>
              <Button
                variant="outlined"
                startIcon={<RefreshIcon />}
                onClick={handleResetFilters}
              >
                Resetear
              </Button>
            </Box>
          </Grid>
        </Grid>
      </Paper>
      
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}
      
      {/* Tabla de sesiones */}
      <Paper sx={{ width: '100%', overflow: 'hidden' }}>
        <TableContainer sx={{ maxHeight: 500 }}>
          <Table stickyHeader>
            <TableHead>
              <TableRow>
                <TableCell>Aplicación</TableCell>
                <TableCell>Dispositivo</TableCell>
                <TableCell>Inicio</TableCell>
                <TableCell>Fin</TableCell>
                <TableCell>Duración</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell colSpan={5} align="center">
                    <CircularProgress />
                  </TableCell>
                </TableRow>
              ) : sessions.length > 0 ? (
                sessions.map((session) => (
                  <TableRow key={session.id}>
                    <TableCell>
                      {session.applicationName}
                      <br />
                      <Typography variant="caption" color="text.secondary">
                        {session.packageName}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      {devices.find(d => d.id === session.deviceId)?.name || 'Desconocido'}
                    </TableCell>
                    <TableCell>{formatDateTime(session.startTime)}</TableCell>
                    <TableCell>{formatDateTime(session.endTime)}</TableCell>
                    <TableCell>{formatDuration(session.durationSeconds)}</TableCell>
                  </TableRow>
                ))
              ) : (
                <TableRow>
                  <TableCell colSpan={5} align="center">
                    No se encontraron sesiones
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
        
        <TablePagination
          rowsPerPageOptions={[10, 25, 50]}
          component="div"
          count={totalElements}
          rowsPerPage={rowsPerPage}
          page={page}
          onPageChange={handleChangePage}
          onRowsPerPageChange={handleChangeRowsPerPage}
          labelRowsPerPage="Filas por página:"
          labelDisplayedRows={({ from, to, count }) => `${from}-${to} de ${count}`}
        />
      </Paper>
    </Box>
  );
};

export default SessionHistory;