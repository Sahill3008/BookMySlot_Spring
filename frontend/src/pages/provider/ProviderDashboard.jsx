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
    // Track which slot is being edited { id: null, capacity: 0 }
    const [editingSlot, setEditingSlot] = useState(null);

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

    const handleUpdateCapacity = async (slotId) => {
        try {
            await api.put(`/provider/slots/${slotId}/capacity`, {
                capacity: editingSlot.capacity
            });
            toast.success('Capacity updated');
            setEditingSlot(null);
            fetchMySlots();
        } catch (error) {
            toast.error('Update failed: ' + (error.response?.data?.message || 'Error'));
        }
    };

    const handleExport = async (slotId) => {
        try {
            const response = await api.get(`/provider/slots/${slotId}/report`, { responseType: 'blob' });

            let filename = 'Appointment_Report.xlsx';
            const disposition = response.headers['content-disposition'];
            if (disposition && disposition.indexOf('attachment') !== -1) {
                const filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
                const matches = filenameRegex.exec(disposition);
                if (matches != null && matches[1]) {
                    filename = matches[1].replace(/['"]/g, '');
                }
            }

            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', filename);
            document.body.appendChild(link);
            link.click();
            link.remove();
        } catch (error) {
            console.error(error);
            toast.error('Export failed');
        }
    };

    return (
        <Container sx={{ mt: 4 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Typography variant="h4">Provider Dashboard</Typography>
            </Box>

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
                        <Typography variant="h6">My Schedule</Typography>
                        <List>
                            {slots.map((slot) => (
                                <ListItem key={slot.id} divider>
                                    <ListItemText
                                        primaryTypographyProps={{ component: 'div' }}
                                        secondaryTypographyProps={{ component: 'div' }}
                                        primary={`${new Date(slot.startTime).toLocaleString()} - ${new Date(slot.endTime).toLocaleTimeString()}`}
                                        secondary={
                                            editingSlot && editingSlot.id === slot.id ? (
                                                <Box sx={{ display: 'flex', alignItems: 'center', mt: 1 }}>
                                                    <TextField
                                                        type="number"
                                                        size="small"
                                                        label="Capacity"
                                                        value={editingSlot.capacity}
                                                        onChange={(e) => setEditingSlot({ ...editingSlot, capacity: parseInt(e.target.value) })}
                                                        inputProps={{ min: slot.bookedCount }} // Constraint based on booked count
                                                        sx={{ width: 100, mr: 1 }}
                                                    />
                                                    <Button size="small" variant="contained" onClick={() => handleUpdateCapacity(slot.id)}>Save</Button>
                                                    <Button size="small" onClick={() => setEditingSlot(null)}>Cancel</Button>
                                                </Box>
                                            ) : (
                                                <Box>
                                                    <Typography variant="body2">{slot.isBooked ? "Status: FULL" : `Status: AVAILABLE`}</Typography>
                                                    <Typography variant="caption">Booked: {slot.bookedCount} / {slot.capacity}</Typography>
                                                    <Button
                                                        size="small"
                                                        sx={{ ml: 1, minWidth: 0, p: 0.5 }}
                                                        onClick={() => setEditingSlot({ id: slot.id, capacity: slot.capacity })}
                                                    >
                                                        ✏️
                                                    </Button>
                                                </Box>
                                            )
                                        }
                                    />
                                    <Box sx={{ display: 'flex', gap: 1 }}>
                                        <Button
                                            variant="outlined"
                                            size="small"
                                            onClick={() => handleExport(slot.id)}
                                            title="Download Excel Report"
                                        >
                                            📥 Export
                                        </Button>
                                        <Button
                                            color="error"
                                            variant="outlined"
                                            size="small"
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
                                            Cancel Slot
                                        </Button>
                                    </Box>
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
