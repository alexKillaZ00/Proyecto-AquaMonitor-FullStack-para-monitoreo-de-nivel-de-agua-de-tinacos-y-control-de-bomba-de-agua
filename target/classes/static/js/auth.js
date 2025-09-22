// auth.js - Módulo compartido para autenticación
const AUTH = {
    API_BASE_URL: 'http://localhost:8080',

    // Listas configurables de páginas
    protectedPages: ['dashboard.html'],
    publicPages: ['login.html', 'register.html'],

    // Verifica si estamos en una página protegida
    isProtectedPage() {
        const path = window.location.pathname;
        return this.protectedPages.some(page => path.endsWith(page));
    },

    // Verifica si estamos en una página pública
    isPublicPage() {
        const path = window.location.pathname;
        return this.publicPages.some(page => path.endsWith(page));
    },

    // Verifica si el usuario está autenticado
    async isAuthenticated() {
        try {
            const response = await this.fetchWithRefresh(`${this.API_BASE_URL}/usuario/me`);
            return response.ok;
        } catch (error) {
            console.error('Error verificando autenticación:', error);
            return false;
        }
    },

    // Redirige según estado de autenticación
    async checkAuthAndRedirect() {
        const isAuth = await this.isAuthenticated();

        // Si está autenticado y está en una página pública de autenticación, redirigir a dashboard
        if (isAuth && this.isPublicPage()) {
            window.location.replace('dashboard.html');
            return;
        }

        // Si no está autenticado y está en una página protegida, redirigir a login
        if (!isAuth && this.isProtectedPage()) {
            window.location.replace('login.html');
            return;
        }
    },

    // Fetch con manejo automático de refresh token (manejando 403 también)
    async fetchWithRefresh(url, options = {}) {
        // Configuración por defecto para incluir cookies
        const defaultOptions = {
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json',
                ...options?.headers
            },
            ...options
        };

        try {
            // Primera petición
            let response = await fetch(url, defaultOptions);

            // Si es 401 o 403 (token expirado), intentar refresh
            if (response.status === 401 || response.status === 403) {
                const refreshResponse = await fetch(`${this.API_BASE_URL}/auth/refresh`, {
                    method: 'POST',
                    credentials: 'include'
                });

                // Si el refresh fue exitoso, reintentar la petición original
                if (refreshResponse.ok) {
                    response = await fetch(url, defaultOptions);
                } else {
                    throw new Error('Error al refrescar el token.');
                }
            }

            return response;

        } catch (error) {
            console.error('Error en fetchWithRefresh:', error);
            throw error;
        }
    },

    // Hacer logout (sin cambios)
    async logout() {
        try {
            await fetch(`${this.API_BASE_URL}/auth/logout`, {
                method: 'POST',
                credentials: 'include'
            });
            window.location.replace('login.html');
        } catch (error) {
            console.error('Error en logout:', error);
            window.location.replace('login.html');
        }
    }
};

// Al cargar cualquier página, verificar autenticación y redirigir si es necesario
document.addEventListener('DOMContentLoaded', () => {
    AUTH.checkAuthAndRedirect();
});