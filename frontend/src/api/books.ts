import {useMutation, useQuery, useQueryClient} from '@tanstack/react-query';
import {toast} from 'sonner';
import {booksApi} from './client';
import type {Book, BookListResponse, BooksParams, CreateBookRequest, UpdateBookRequest} from '@/types/api';

type JavaBook = {
    id: number;
    title: string;
    author: string;
    description?: string | null;
    isbn?: string | null;
    publishedYear?: string | null;
    createdBy: number | null;
    createdAt: string;
    updatedAt: string;
};

type JavaPagination = {
    page: number;
    limit: number;
    total: number;
    totalPages: number;
};

type JavaBookListResponse = {
    data: JavaBook[];
    pagination: JavaPagination;
};

function mapBook(book: JavaBook): Book {
    return {
        id: String(book.id),
        title: book.title,
        author: book.author,
        description: book.description ?? undefined,
        isbn: book.isbn ?? undefined,
        published_year: book.publishedYear ? Number(book.publishedYear.slice(0, 4)) : undefined,
        average_rating: null,
        reviews_count: 0,
        created_by: book.createdBy == null ? '' : String(book.createdBy),
        created_at: book.createdAt,
        updated_at: book.updatedAt,
    };
}

function mapBookList(response: JavaBookListResponse): BookListResponse {
    return {
        data: response.data.map(mapBook),
        pagination: {
            page: response.pagination.page,
            limit: response.pagination.limit,
            total: response.pagination.total,
            total_pages: response.pagination.totalPages,
        },
    };
}

export function useBooks(params: BooksParams = {}) {
    return useQuery({
        queryKey: ['books', params],
        queryFn: async () => {
            const javaParams = {
                ...params,
                sort: params.sort === 'created_at' ? 'createdAt' : params.sort,
            };
            const response = await booksApi.get<JavaBookListResponse>('/api/v1/books', {params: javaParams});
            return mapBookList(response.data);
        },
        retry: false,
    });
}

export function useBook(id: string) {
    return useQuery({
        queryKey: ['books', id],
        queryFn: async () => {
            const response = await booksApi.get<JavaBook>(`/api/v1/books/${id}`);
            return mapBook(response.data);
        },
        enabled: !!id,
        retry: false,
    });
}

export function useCreateBook() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async (data: CreateBookRequest) => {
            const payload = {
                title: data.title,
                author: data.author,
                description: data.description,
                isbn: data.isbn,
                // Java currently models publishedYear as LocalDate.
                publishedYear: data.published_year ? `${data.published_year}-01-01` : undefined,
            };
            const response = await booksApi.post<JavaBook>('/api/v1/books', payload);
            return mapBook(response.data);
        },
        onSuccess: () => {
            queryClient.invalidateQueries({queryKey: ['books']});
            toast.success('Книга добавлена!');
        },
        onError: () => {
            toast.error('Ошибка добавления книги');
        },
    });
}

export function useUpdateBook(id: string) {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async (data: UpdateBookRequest) => {
            // Java's update DTO currently supports only title, author and description.
            const response = await booksApi.put<JavaBook>(`/api/v1/books/${id}`, {
                title: data.title,
                author: data.author,
                description: data.description,
            });
            return mapBook(response.data);
        },
        onSuccess: () => {
            queryClient.invalidateQueries({queryKey: ['books']});
            queryClient.invalidateQueries({queryKey: ['books', id]});
            toast.success('Книга обновлена');
        },
        onError: () => {
            toast.error('Ошибка обновления книги');
        },
    });
}

export function useDeleteBook() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async (id: string) => {
            await booksApi.delete(`/api/v1/books/${id}`);
        },
        onSuccess: () => {
            queryClient.invalidateQueries({queryKey: ['books']});
            toast.success('Книга удалена');
        },
        onError: () => {
            toast.error('Ошибка удаления книги');
        },
    });
}
