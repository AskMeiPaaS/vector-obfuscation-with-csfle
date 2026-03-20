import { useState, useEffect, useRef, useCallback } from 'react';

export interface LogEvent {
    timestamp: string;
    operation: string;
    latencyMs: number;
    message: string;
}

export function useLogStream() {
    const [logs, setLogs] = useState<LogEvent[]>([]);
    const [isConnected, setIsConnected] = useState(false);
    const wsRef = useRef<WebSocket | null>(null);
    const reconnectTimeoutRef = useRef<NodeJS.Timeout | null>(null);

    const connect = useCallback(() => {
        const wsUrl = process.env.NEXT_PUBLIC_WS_URL || 'ws://localhost:8080/ws/logs';
        console.log(`Connecting to WebSocket: ${wsUrl}`);
        
        const ws = new WebSocket(wsUrl);
        wsRef.current = ws;

        ws.onopen = () => {
            console.log('Connected to backend log stream');
            setIsConnected(true);
            if (reconnectTimeoutRef.current) {
                clearTimeout(reconnectTimeoutRef.current);
                reconnectTimeoutRef.current = null;
            }
        };

        ws.onmessage = (event) => {
            try {
                const newLog: LogEvent = JSON.parse(event.data);
                setLogs((prevLogs) => [...prevLogs.slice(-99), newLog]); // Keep last 100 logs
            } catch (err) {
                console.error('Failed to parse WebSocket message:', err);
            }
        };

        ws.onclose = () => {
            console.log('WebSocket connection closed. Attempting reconnect in 3s...');
            setIsConnected(false);
            reconnectTimeoutRef.current = setTimeout(connect, 3000);
        };

        ws.onerror = (error) => {
            console.error('WebSocket error:', error);
            ws.close();
        };
    }, []);

    useEffect(() => {
        connect();
        return () => {
            if (wsRef.current) wsRef.current.close();
            if (reconnectTimeoutRef.current) clearTimeout(reconnectTimeoutRef.current);
        };
    }, [connect]);

    return { logs, isConnected };
}