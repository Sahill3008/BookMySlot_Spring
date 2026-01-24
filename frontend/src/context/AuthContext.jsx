import React, { createContext, useState, useEffect, useContext } from 'react';
import { jwtDecode } from 'jwt-decode';
import api from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // Check for token on mount
        const token = localStorage.getItem('token');
        if (token) {
            try {
                const decoded = jwtDecode(token);
                // Expiration check
                if (decoded.exp * 1000 < Date.now()) {
                    throw new Error('Token expired');
                }

                setUser({
                    sub: decoded.sub,
                    role: decoded.role,
                    id: decoded.userId
                });
            } catch (e) {
                console.error("Invalid token", e);
                localStorage.removeItem('token');
            }
        }
        setLoading(false);
    }, []);

    const login = async (email, password) => {
        const response = await api.post('/auth/login', { email, password });
        const { token } = response.data;

        // Save to local storage
        localStorage.setItem('token', token);

        // Decode and set user
        const decoded = jwtDecode(token);
        setUser({
            sub: decoded.sub,
            role: decoded.role,
            id: decoded.userId
        });
        return response.data;
    };

    const register = async (userData) => {
        return await api.post('/auth/register', userData);
    };

    const logout = () => {
        localStorage.removeItem('token');
        setUser(null);
    };

    return (
        <AuthContext.Provider value={{ user, login, register, logout, loading }}>
            {!loading && children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);
