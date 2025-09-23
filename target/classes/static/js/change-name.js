// change-name.js

// Configuración de la API
const API_BASE_URL = (() => {
    // Si estás en desarrollo local
    if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
        return 'http://localhost:8080/api';
    }

    // En producción, construir URL desde variables de entorno o usar el mismo dominio
    const protocol = window.location.protocol;
    const hostname = window.location.hostname;
    const port = window.location.port;

    // Si hay un puerto específico en producción
    if (port && port !== '80' && port !== '443') {
        return `${protocol}//${hostname}:${port}/api`;
    }

    // URL estándar de producción
    return `${protocol}//${hostname}/api`;
})();

// Elementos del DOM
const userName = document.getElementById('userName');
const logoutBtn = document.getElementById('logoutBtn');
const updateNameForm = document.getElementById('updateNameForm');
const updateNameError = document.getElementById('updateNameError');
const updateNameSuccess = document.getElementById('updateNameSuccess');

// Función para hacer peticiones autenticadas a la API con manejo de refresh token
async function authenticatedFetch(url, options = {}) {
    const defaultOptions = {
        credentials: 'include', // Siempre incluir cookies
        headers: {
            'Content-Type': 'application/json',
            ...options.headers
        }
    };

    // Intentar solicitud original
    let response = await fetch(url, { ...options, ...defaultOptions });

    if (response.status === 401 || response.status === 403) {
        console.warn('Token expirado. Intentando refresh...');

        // Intentar refresh
        const refreshResponse = await fetch(`${API_BASE_URL}/auth/refresh`, {
            method: 'POST',
            credentials: 'include'
        });

        if (refreshResponse.ok) {
            console.log('Refresh token exitoso. Reintentando la solicitud original...');
            // Reintentar solicitud original con nuevo token
            response = await fetch(url, { ...options, ...defaultOptions });
        } else {
            console.error('Refresh token falló. Redirigiendo a login...');
            throw new Error('No autorizado - Token inválido o expirado');
        }
    }

    return response;
}

// Función para hacer logout
async function logoutUser() {
    try {
        const response = await authenticatedFetch(`${API_BASE_URL}/auth/logout`, {
            method: 'POST',
            credentials: 'include'
        });

        if (response.ok) {
            localStorage.clear();
            window.location.replace('login.html');
        } else {
            throw new Error('Error en el logout');
        }
    } catch (error) {
        console.error('Error en logout:', error);
        window.location.replace('login.html');
    }
}

// Función para obtener datos del usuario usando localStorage o API
async function getUserData() {
    const cachedUser = localStorage.getItem('currentUser');
    if (cachedUser) {
        window.currentUser = JSON.parse(cachedUser);
        userName.textContent = window.currentUser.nombre;
        return;
    }

    try {
        const response = await authenticatedFetch(`${API_BASE_URL}/usuario/me`, {
            method: 'GET'
        });

        if (response && response.ok) {
            const userData = await response.json();
            userName.textContent = userData.nombre;
            window.currentUser = userData;
            localStorage.setItem('currentUser', JSON.stringify(userData));
        } else {
            throw new Error(`Error al obtener datos del usuario: ${response.status}`);
        }
    } catch (error) {
        console.error('Error al obtener datos del usuario:', error);
        userName.textContent = 'Usuario';
        await logoutUser();
    }
}

// Función para validar nombre
function isValidName(name) {
    const nameRegex = /^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]{2,255}$/;
    return nameRegex.test(name);
}

// Función para actualizar el nombre
async function updateUserName(event) {
    event.preventDefault();
    const newName = document.getElementById('newName').value;

    // Validar campos
    if (!newName) {
        updateNameError.textContent = 'El nombre no puede estar vacío';
        updateNameError.style.display = 'block';
        updateNameSuccess.style.display = 'none';
        return;
    }
    if (!isValidName(newName)) {
        updateNameError.textContent = 'Nombre inválido. Solo letras y espacios, 2-255 caracteres.';
        updateNameError.style.display = 'block';
        updateNameSuccess.style.display = 'none';
        return;
    }

    try {
        const response = await authenticatedFetch(`${API_BASE_URL}/usuario/me/actualizar-nombre`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'text/plain'
            },
            body: newName
        });

        if (response.ok) {
            const userData = await response.json();
            document.getElementById('userName').textContent = userData.nombre;
            window.currentUser = userData;
            localStorage.setItem('currentUser', JSON.stringify(userData));
            updateNameSuccess.textContent = 'Nombre actualizado correctamente';
            updateNameSuccess.style.display = 'block';
            updateNameError.style.display = 'none';
            updateNameForm.reset();
        } else {
            throw new Error('Error al actualizar el nombre');
        }
    } catch (error) {
        console.error('Error:', error);
        updateNameError.textContent = 'Error al actualizar el nombre';
        updateNameError.style.display = 'block';
        updateNameSuccess.style.display = 'none';
    }
}

// Event listeners
if (logoutBtn) {
    logoutBtn.addEventListener("click", () => {
        if (confirm("¿Estas seguro de que quieres salir?")) {
            logoutUser()
        }
    })
}

if (updateNameForm) {
    updateNameForm.addEventListener('submit', updateUserName);
}

// Inicializar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', () => {
    getUserData();
});
