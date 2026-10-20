import {useQuery} from '@tanstack/react-query';
import {authApi, booksApi} from './client';
import type {HealthResponse} from '@/types/api';

const healthResponse: HealthResponse = {
    status: 'ok',
    version: 'unknown',
    timestamp: '',
};

export function useAuthHealth() {
    return useQuery({
        queryKey: ['health', 'auth'],
        queryFn: async () => {
            await authApi.get('/actuator/health');
            return healthResponse;
        },
        refetchInterval: 30000,
        retry: false,
    });
}

export function useBooksHealth() {
    return useQuery({
        queryKey: ['health', 'books'],
        queryFn: async () => {
            await booksApi.get('/actuator/health');
            return healthResponse;
        },
        refetchInterval: 30000,
        retry: false,
    });
}
