import React, { useEffect, useState } from 'react';
import { Container, Typography, Grid, Card, CardContent, Button, Alert } from '@mui/material';
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
            const data = response.data;
            if (data.content && Array.isArray(data.content)) {
                setSlots(data.content);
            } else if (Array.isArray(data)) {
                setSlots(data);
            } else {
                setSlots([]); // Fallback to empty to prevent crash
            }

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
            await api.post('/appointments', { slotId }); // This looks like /appointments -> if base is /api, then /api/appointments. Matches Controller.
            toast.success('Booked successfully!');
            fetchSlots();
        } catch (error) {
            toast.error('Booking failed: ' + (error.response?.data?.message || error.message));
        }
    };



    return (
        <Container sx={{ mt: 4 }}>
            <Typography variant="h4" gutterBottom>Available Appointments</Typography>


            <Grid container spacing={3}>
                {(Array.isArray(slots) ? slots : []).map((slot) => (
                    <Grid size={{ xs: 12, sm: 6, md: 4 }} key={slot.id}>
                        <Card>
                            <CardContent>
                                <Typography variant="h6">{slot.providerName}</Typography>
                                <Typography color="textSecondary">
                                    {new Date(slot.startTime).toLocaleString()} -
                                    {new Date(slot.endTime).toLocaleTimeString()}
                                </Typography>
                                <Typography variant="body2" color={slot.bookedCount >= slot.capacity ? 'error' : 'textSecondary'}>
                                    Availability: {slot.capacity - slot.bookedCount} / {slot.capacity} spots left
                                </Typography>
                                <Button
                                    variant="contained"
                                    color="primary"
                                    fullWidth
                                    sx={{ mt: 2 }}
                                    onClick={() => handleBook(slot.id)}
                                    disabled={(!!user && user.role !== 'ROLE_CUSTOMER') || slot.bookedCount >= slot.capacity}
                                >
                                    {slot.bookedCount >= slot.capacity ? 'Full' : (user && user.role === 'ROLE_PROVIDER' ? 'Provider View' : 'Book Now')}
                                </Button>
                            </CardContent>
                        </Card>
                    </Grid>
                ))}
                {(!slots || slots.length === 0) && <Typography>No slots available.</Typography>}
            </Grid>
        </Container >
    );
};

export default SlotList;
