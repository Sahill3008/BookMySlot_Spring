import React, { useEffect, useRef } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import { toast } from 'react-toastify';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

const NotificationListener = () => {
    const { user } = useAuth();
    const stompClientRef = useRef(null);

    useEffect(() => {
        if (!user) return;

        // 1. Initial Fetch (for missed notifications while offline)
        const fetchNotifications = async () => {
            try {
                const response = await api.get('/notifications');
                response.data.forEach(notification => {
                    displayNotification(notification);
                });
            } catch (error) {
                console.error("Failed to fetch notifications", error);
            }
        };

        const displayNotification = (notification) => {
            toast.info(notification.message, {
                autoClose: false,
                closeOnClick: false
            });
            // Mark as read
            api.put(`/notifications/${notification.id}/read`).catch(console.error);
        };

        fetchNotifications();

        // 2. WebSocket Connection
        // Pass token in query param for authentication
        const token = localStorage.getItem('token');

        // Fix: Stomp.over takes a factory function in v5+
        const stompClient = Stomp.over(() => new SockJS('http://localhost:8080/ws?token=' + token));

        stompClient.debug = () => { }; // Disable debug logs

        stompClient.connect({
            // Pass JWT token if needed, usually passed in headers for handshake or connect
            // Spring Security 6 WebSocket might require token in query param or headers
            // For now, we are using implicit session or simplified flow.
            // If secure, we might need 'Authorization': `Bearer ${token}`
        }, (frame) => {
            // Subscribe to user-specific queue
            // Spring converts /user/queue/notifications -> /user/{username}/queue/notifications
            // We need to match what we send in backend: convertAndSendToUser(email, "/queue/notifications", ...)
            stompClient.subscribe('/user/queue/notifications', (message) => {
                const notification = JSON.parse(message.body);
                displayNotification(notification);
            });
        }, (error) => {
            console.error("WebSocket Error: ", error);
        });

        stompClientRef.current = stompClient;

        return () => {
            if (stompClientRef.current) {
                stompClientRef.current.deactivate(); // disconnect() is deprecated in v5
            }
        };

    }, [user]);

    return null;
};

export default NotificationListener;
