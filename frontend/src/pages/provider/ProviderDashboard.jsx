import React, { useEffect, useState } from 'react';
import { Container, Typography, Grid, Paper, TextField, Button, Box, List, ListItem, ListItemText } from '@mui/material';
import api from '../../services/api';
import { toast } from 'react-toastify';

const ProviderDashboard = () => {
    const [slots, setSlots] = useState([]);

    const [newSlot, setNewSlot] = useState({
        startTime: '',
        endTime: '',
        capacity: 1
    });

    useEffect(() => {
        fetchMySlots();
    }, []);

    const fetchMySlots = async () => {
        try {
            const response = await api.get('/provider/slots');
            setSlots(response.data);
        } catch (error) {
            console.error(error);
        }
    };

    const handleCreateSlot = async (e) => {
        e.preventDefault();
        try {
            await api.post('/provider/slots', {
                startTime: newSlot.startTime,
                endTime: newSlot.endTime,
                capacity: newSlot.capacity
            });
            toast.success('Slot created successfully');
            setNewSlot({ startTime: '', endTime: '', capacity: 1 });
            fetchMySlots();
        } catch (error) {
            toast.error('Failed to create slot: ' + (error.response?.data?.message || 'Error'));
        }
    };

    return (
        <Container sx={{ mt: 4 }}>
            <Typography variant="h4" gutterBottom>Provider Dashboard</Typography>

            <Grid container spacing={4}>
                {/* Create Slot Form */}
                <Grid size={{ xs: 12, md: 4 }}>
                    <Paper sx={{ p: 2 }}>
                        <Typography variant="h6">Create New Slot</Typography>
                        <Box component="form" onSubmit={handleCreateSlot} sx={{ mt: 2 }}>
                            <TextField
                                label="Start Time"
                                type="datetime-local"
                                fullWidth
                                margin="normal"
                                InputLabelProps={{ shrink: true }}
                                value={newSlot.startTime}
                                onChange={(e) => setNewSlot({ ...newSlot, startTime: e.target.value })}
                                required
                            />
                            <TextField
                                label="End Time"
                                type="datetime-local"
                                fullWidth
                                margin="normal"
                                InputLabelProps={{ shrink: true }}
                                value={newSlot.endTime}
                                onChange={(e) => setNewSlot({ ...newSlot, endTime: e.target.value })}
                                required
                            />
                            <TextField
                                label="Capacity"
                                type="number"
                                fullWidth
                                margin="normal"
                                value={newSlot.capacity}
                                onChange={(e) => setNewSlot({ ...newSlot, capacity: parseInt(e.target.value) })}
                                inputProps={{ min: 1 }}
                                required
                            />
                            <Button type="submit" variant="contained" fullWidth sx={{ mt: 2 }}>
                                Create Slot
                            </Button>
                        </Box>
                    </Paper>
                </Grid>

                {/* My Slots List */}
                <Grid size={{ xs: 12, md: 8 }}>
                    <Paper sx={{ p: 2 }}>
                        <Typography variant="h6">My Slots</Typography>
                        <List>
                            {slots.map((slot) => (
                                <ListItem key={slot.id} divider>
                                    <ListItemText
                                        primary={`${new Date(slot.startTime).toLocaleString()} - ${new Date(slot.endTime).toLocaleTimeString()}`}

                                        secondary={slot.isBooked ? "Status: FULL" : `Status: AVAILABLE (${slot.bookedCount}/${slot.capacity} booked)`}
                                    />
                                    <Button disabled={slot.isBooked} variant="outlined" size="small">
                                        {slot.isBooked ? 'View Booking' : 'Available'}
                                    </Button>
                                    <Button
                                        color="error"
                                        variant="outlined"
                                        size="small"
                                        sx={{ ml: 2 }}
                                        onClick={async () => {
                                            if (window.confirm('Are you sure you want to delete this slot? If booked, the appointment will be cancelled.')) {
                                                try {
                                                    await api.delete(`/provider/slots/${slot.id}`);
                                                    toast.success('Slot cancelled successfully');
                                                    fetchMySlots();
                                                } catch (error) {
                                                    toast.error('Failed to cancel slot: ' + (error.response?.data?.message || 'Error'));
                                                }
                                            }
                                        }}
                                    >
                                        Cancel
                                    </Button>
                                </ListItem>
                            ))}
                            {slots.length === 0 && <Typography>No slots created yet.</Typography>}
                        </List>
                    </Paper>
                </Grid>
            </Grid>
        </Container>
    );
};

export default ProviderDashboard;
