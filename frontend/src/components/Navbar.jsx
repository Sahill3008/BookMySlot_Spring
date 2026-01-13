import React from 'react';
import { AppBar, Toolbar, Typography, Button, Box } from '@mui/material';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Navbar = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <AppBar position="static">
            <Toolbar>
                <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
                    <Link to="/" style={{ color: 'white', textDecoration: 'none' }}>
                        BookMySlot
                    </Link>
                </Typography>

                <Box>
                    {!user ? (
                        <>
                            <Button color="inherit" component={Link} to="/login">Login</Button>
                            <Button color="inherit" component={Link} to="/register">Register</Button>
                        </>
                    ) : (
                        <>
                            <Button color="inherit" component={Link} to="/">Home</Button>

                            {user.role === 'ROLE_CUSTOMER' && (
                                <Button color="inherit" component={Link} to="/my-appointments">My Appointments</Button>
                            )}

                            {user.role === 'ROLE_PROVIDER' && (
                                <Button color="inherit" component={Link} to="/provider/dashboard">Manage Slots</Button>
                            )}

                            {user.role === 'ROLE_ADMIN' && (
                                <Button color="inherit" component={Link} to="/admin/dashboard">Admin</Button>
                            )}

                            <Button color="inherit" onClick={handleLogout}>Logout</Button>
                        </>
                    )}
                </Box>
            </Toolbar>
        </AppBar>
    );
};

export default Navbar;
