import React, { useEffect, useState } from 'react';
import { Container, Typography, Grid, Card, CardContent, Button } from '@mui/material';
import api from '../../services/api';
import { toast } from 'react-toastify';
import { useAuth } from '../../context/AuthContext';

const SlotList = () => {
    const [slots, setSlots] = useState([]);
    const { user } = useAuth();

    useEffect(() => {
        fetchSlots();
    }, []);

    const fetchSlots = async () => {
        try {
            const response = await api.get('/slots');
            setSlots(response.data);
        } catch (error) {
            console.error(error);
        }
    };

    const handleBook = async (slotId) => {
        if (!user) {
            toast.info("Please login to book");
            return;
        }
        try {
            await api.post('/appointments', { slotId });
            toast.success('Booked successfully!');
            fetchSlots(); // Refresh list to remove booked slot
        } catch (error) {
            toast.error('Booking failed: ' + (error.response?.data?.message || error.message));
        }
    };

    return (
        <Container sx={{ mt: 4 }}>
            <Typography variant="h4" gutterBottom>Available Appointments</Typography>
            <Grid container spacing={3}>
                {slots.map((slot) => (
                    <Grid size={{ xs: 12, sm: 6, md: 4 }} key={slot.id}>
                        <Card>
                            <CardContent>
                                <Typography variant="h6">{slot.providerName}</Typography>
                                <Typography color="textSecondary">
                                    {new Date(slot.startTime).toLocaleString()} -
                                    {new Date(slot.endTime).toLocaleTimeString()}
                                </Typography>
                                <Button
                                    variant="contained"
                                    color="primary"
                                    fullWidth
                                    sx={{ mt: 2 }}
                                    onClick={() => handleBook(slot.id)}
                                    disabled={!!user && user.role !== 'ROLE_CUSTOMER'}
                                >
                                    {user && user.role === 'ROLE_PROVIDER' ? 'Provider View' : 'Book Now'}
                                </Button>
                            </CardContent>
                        </Card>
                    </Grid>
                ))}
                {slots.length === 0 && <Typography>No slots available.</Typography>}
            </Grid>
        </Container>
    );
};

export default SlotList;
