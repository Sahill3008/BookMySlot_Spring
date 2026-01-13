import React, { useEffect, useState } from 'react';
import { Container, Typography, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Tabs, Tab, Box } from '@mui/material';
import api from '../../services/api';

const AdminDashboard = () => {
    const [tabValue, setTabValue] = useState(0);
    const [appointments, setAppointments] = useState([]);

    useEffect(() => {
        if (tabValue === 0) {
            fetchAppointments();
        }
    }, [tabValue]);

    const fetchAppointments = async () => {
        try {
            const response = await api.get('/admin/appointments');
            setAppointments(response.data);
        } catch (error) {
            console.error(error);
        }
    };

    return (
        <Container sx={{ mt: 4 }}>
            <Typography variant="h4" gutterBottom>Admin Dashboard</Typography>
            <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 2 }}>
                <Tabs value={tabValue} onChange={(e, val) => setTabValue(val)}>
                    <Tab label="All Appointments" />
                    <Tab label="User Management (TODO)" />
                </Tabs>
            </Box>

            {tabValue === 0 && (
                <TableContainer component={Paper}>
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell>ID</TableCell>
                                <TableCell>Customer</TableCell>
                                <TableCell>Provider</TableCell>
                                <TableCell>Date</TableCell>
                                <TableCell>Status</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {appointments.map((appt) => (
                                <TableRow key={appt.id}>
                                    <TableCell>{appt.id}</TableCell>
                                    <TableCell>{appt.customerName}</TableCell>
                                    <TableCell>{appt.slot.providerName}</TableCell>
                                    <TableCell>{new Date(appt.slot.startTime).toLocaleString()}</TableCell>
                                    <TableCell>{appt.status}</TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </TableContainer>
            )}

            {tabValue === 1 && (
                <Typography>User Management Feature Placeholder</Typography>
            )}
        </Container>
    );
};

export default AdminDashboard;
