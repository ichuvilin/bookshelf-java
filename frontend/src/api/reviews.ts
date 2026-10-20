import {useMutation, useQuery, useQueryClient} from '@tanstack/react-query';
import {toast} from 'sonner';
import {booksApi} from './client';
import type {CreateReviewRequest, Review, ReviewListResponse, ReviewsParams, UpdateReviewRequest} from '@/types/api';

type JavaReview = {
    id: number;
    bookId: number;
    userId: number;
    username: string;
    rating: number;
    title?: string | null;
    content: string;
    createdAt: string;
    updatedAt: string;
};

function mapReview(review: JavaReview): Review {
    return {
        id: String(review.id),
        book_id: String(review.bookId),
        user_id: String(review.userId),
        user: {id: String(review.userId), username: review.username},
        rating: review.rating,
        title: review.title ?? undefined,
        content: review.content,
        created_at: review.createdAt,
        updated_at: review.updatedAt,
    };
}

export function useBookReviews(bookId: string, _params: ReviewsParams = {}) {
    return useQuery({
        queryKey: ['reviews', bookId],
        queryFn: async () => {
            const response = await booksApi.get<JavaReview[]>(`/api/v1/books/${bookId}/reviews`);
            const data = response.data.map(mapReview);
            const result: ReviewListResponse = {
                data,
                pagination: {page: 1, limit: data.length, total: data.length, total_pages: data.length ? 1 : 0},
            };
            return result;
        },
        enabled: !!bookId,
        retry: false,
    });
}

// The current Java API does not provide GET /api/v1/reviews/{id}.
export function useReview(id: string) {
    return useQuery<Review>({
        queryKey: ['reviews', 'single', id],
        queryFn: async () => {
            throw new Error('Получение одной рецензии пока не поддерживается Java API');
        },
        enabled: false,
    });
}

export function useCreateReview(bookId: string) {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async (data: CreateReviewRequest) => {
            const response = await booksApi.post<JavaReview>(`/api/v1/books/${bookId}/reviews`, data);
            return mapReview(response.data);
        },
        onSuccess: () => {
            queryClient.invalidateQueries({queryKey: ['reviews', bookId]});
            queryClient.invalidateQueries({queryKey: ['books', bookId]});
            queryClient.invalidateQueries({queryKey: ['books']});
            toast.success('Рецензия добавлена!');
        },
        onError: (error: any) => {
            const message = error.response?.status === 409
                ? 'Вы уже оставили рецензию на эту книгу'
                : 'Ошибка добавления рецензии';
            toast.error(message);
        },
    });
}

export function useUpdateReview(reviewId: string, bookId: string) {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async (data: UpdateReviewRequest) => {
            const response = await booksApi.put<JavaReview>(`/api/v1/reviews/${reviewId}`, data);
            return mapReview(response.data);
        },
        onSuccess: () => {
            queryClient.invalidateQueries({queryKey: ['reviews', bookId]});
            queryClient.invalidateQueries({queryKey: ['reviews', 'single', reviewId]});
            queryClient.invalidateQueries({queryKey: ['books', bookId]});
            queryClient.invalidateQueries({queryKey: ['books']});
            toast.success('Рецензия обновлена');
        },
        onError: () => {
            toast.error('Ошибка обновления рецензии');
        },
    });
}

export function useDeleteReview(bookId: string) {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async (reviewId: string) => {
            await booksApi.delete(`/api/v1/reviews/${reviewId}`);
        },
        onSuccess: () => {
            queryClient.invalidateQueries({queryKey: ['reviews', bookId]});
            queryClient.invalidateQueries({queryKey: ['books', bookId]});
            queryClient.invalidateQueries({queryKey: ['books']});
            toast.success('Рецензия удалена');
        },
        onError: () => {
            toast.error('Ошибка удаления рецензии');
        },
    });
}

// Listing reviews by user is not part of the current Java API contract.
export function useUserReviews(userId: string, _params: ReviewsParams = {}) {
    return useQuery<ReviewListResponse>({
        queryKey: ['reviews', 'user', userId],
        queryFn: async () => {
            throw new Error('Список рецензий пользователя пока не поддерживается Java API');
        },
        enabled: false,
    });
}
