import {useMutation, useQuery} from '@tanstack/react-query';
import {useNavigate} from 'react-router-dom';
import {toast} from 'sonner';
import {authApi} from './client';
import {useAuthStore} from '@/stores/auth';
import type {AuthResponse, LoginRequest, PublicUser, RegisterRequest, UpdateUserRequest, User} from '@/types/api';

type JavaUser = {
    id: number;
    username: string;
    email: string;
    createdAt: string;
    updatedAt: string;
};

type JavaAuthResponse = {
    accessToken: string;
    tokenType: 'Bearer';
    expiresIn: number;
    user: JavaUser;
};

function mapUser(user: JavaUser): User {
    return {
        id: String(user.id),
        username: user.username,
        email: user.email,
        created_at: user.createdAt,
        updated_at: user.updatedAt,
    };
}

function mapAuthResponse(response: JavaAuthResponse): AuthResponse {
    return {
        access_token: response.accessToken,
        token_type: response.tokenType,
        expires_in: response.expiresIn,
        user: mapUser(response.user),
    };
}

export function useLogin() {
    const navigate = useNavigate();
    const setAuth = useAuthStore((state) => state.setAuth);

    return useMutation({
        mutationFn: async (data: LoginRequest) => {
            const response = await authApi.post<JavaAuthResponse>('/api/v1/auth/login', data);
            return mapAuthResponse(response.data);
        },
        onSuccess: (data) => {
            setAuth(data.access_token, data.user, data.expires_in);
            toast.success('Добро пожаловать!');
            navigate('/');
        },
        onError: () => {
            toast.error('Не удалось войти. Проверьте email и пароль.');
        },
    });
}

export function useRegister() {
    const navigate = useNavigate();
    const setAuth = useAuthStore((state) => state.setAuth);

    return useMutation({
        mutationFn: async (data: RegisterRequest) => {
            const response = await authApi.post<JavaAuthResponse>('/api/v1/auth/register', data);
            return mapAuthResponse(response.data);
        },
        onSuccess: (data) => {
            setAuth(data.access_token, data.user, data.expires_in);
            toast.success('Регистрация успешна!');
            navigate('/');
        },
        onError: (error: any) => {
            const status = error.response?.status;
            const message = status === 409
                ? 'Пользователь с таким email или именем уже существует'
                : error.response?.data?.message || 'Ошибка регистрации';
            toast.error(message);
        },
    });
}

// The Java auth service uses stateless JWT access tokens and has no logout endpoint.
// Logout is therefore performed locally by removing the token.
export function useLogout() {
    const navigate = useNavigate();
    const logout = useAuthStore((state) => state.logout);

    return useMutation({
        mutationFn: async () => undefined,
        onSuccess: () => {
            logout();
            navigate('/');
            toast.success('Вы вышли из аккаунта');
        },
    });
}

export function useCurrentUser() {
    const token = useAuthStore((state) => state.token);

    return useQuery({
        queryKey: ['currentUser'],
        queryFn: async () => {
            const response = await authApi.get<JavaUser>('/api/v1/users/me');
            return mapUser(response.data);
        },
        enabled: !!token,
    });
}

export function useUpdateProfile() {
    const setUser = useAuthStore((state) => state.setUser);

    return useMutation({
        mutationFn: async (data: UpdateUserRequest) => {
            const response = await authApi.put<JavaUser>('/api/v1/users/me', data);
            return mapUser(response.data);
        },
        onSuccess: (data) => {
            setUser(data);
            toast.success('Профиль обновлён');
        },
        onError: (error: any) => {
            const message = error.response?.status === 409
                ? 'Это имя пользователя уже занято'
                : 'Ошибка обновления профиля';
            toast.error(message);
        },
    });
}

// Public user lookup is not currently part of the Java API contract.
export function useUser(_userId: string) {
    return useQuery<PublicUser>({
        queryKey: ['users', _userId],
        queryFn: async () => {
            throw new Error('Публичный профиль пользователя пока не поддерживается Java API');
        },
        enabled: false,
    });
}
