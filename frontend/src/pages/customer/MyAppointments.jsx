import React, { useEffect, useState } from 'react';
import { Container, Typography, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Button, Chip } from '@mui/material';
import api from '../../services/api';
import { toast } from 'react-toastify';

const MyAppointments = () => {
    const [appointments, setAppointments] = useState([]);

    useEffect(() => {
        fetchAppointments();
    }, []);

    const fetchAppointments = async () => {
        try {
            const response = await api.get('/appointments/my');
            setAppointments(response.data);
        } catch (error) {
            console.error(error);
        }
    };

    const handleCancel = async (id) => {
        if (!window.confirm('Are you sure you want to cancel this appointment?')) return;
        try {
            await api.delete(`/appointments/${id}`);
            toast.success('Appointment cancelled');
            fetchAppointments();
        } catch (error) {
            toast.error('Cancellation failed: ' + (error.response?.data?.message || 'Error'));
        }
    };

    return (
        <Container sx={{ mt: 4 }}>
            <Typography variant="h4" gutterBottom>My Appointments</Typography>
            <TableContainer component={Paper}>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>ID</TableCell>
                            <TableCell>Provider</TableCell>
                            <TableCell>Time</TableCell>
                            <TableCell>Status</TableCell>
                            <TableCell>Action</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {appointments.map((appt) => (
                            <TableRow key={appt.id}>
                                <TableCell>{appt.id}</TableCell>
                                <TableCell>{appt.slot.providerName}</TableCell>
                                <TableCell>
                                    {new Date(appt.slot.startTime).toLocaleString()}
                                </TableCell>
                                <TableCell>
                                    <Chip
                                        label={appt.status}
                                        color={appt.status === 'BOOKED' ? 'success' : 'default'}
                                    />
                                </TableCell>
                                <TableCell>
                                    {appt.status === 'BOOKED' && (
                                        <Button
                                            variant="outlined"
                                            color="error"
                                            size="small"
                                            onClick={() => handleCancel(appt.id)}
                                        >
                                            Cancel
                                        </Button>
                                    )}
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>
        </Container>
    );
};

export default MyAppointments;
