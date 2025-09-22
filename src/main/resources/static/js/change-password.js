// change-password.js

// Configuración de la API
const API_BASE_URL = 'http://localhost:8080';

// Elementos del DOM
const userName = document.getElementById('userName');
const logoutBtn = document.getElementById('logoutBtn');
const updatePasswordForm = document.getElementById('updatePasswordForm');
const updatePasswordError = document.getElementById('updatePasswordError');
const updatePasswordSuccess = document.getElementById('updatePasswordSuccess');

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

// Función para validar contraseña
function isValidPassword(password) {
    return password.length >= 8 && password.length <= 255;
}

// Función para actualizar la contraseña
async function updateUserPassword(event) {
    event.preventDefault();
    const passwordActual = document.getElementById('currentPassword').value;
    const nuevoPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    // Validar campos
    if (!passwordActual || !nuevoPassword || !confirmPassword) {
        updatePasswordError.textContent = 'Todos los campos son obligatorios';
        updatePasswordError.style.display = 'block';
        updatePasswordSuccess.style.display = 'none';
        return;
    } else if (!isValidPassword(nuevoPassword)) {
        updatePasswordError.textContent = 'La nueva contraseña debe tener entre 8 y 255 caracteres';
        updatePasswordError.style.display = 'block';
        updatePasswordSuccess.style.display = 'none';
        return;
    }

    // Validar que las contraseñas coincidan
    if (nuevoPassword !== confirmPassword) {
        updatePasswordError.textContent = 'Las contraseñas no coinciden';
        updatePasswordError.style.display = 'block';
        updatePasswordSuccess.style.display = 'none';
        return;
    }

    try {
        const response = await authenticatedFetch(`${API_BASE_URL}/usuario/me/actualizar-password`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                passwordActual,
                nuevoPassword
            })
        });

        if (response.ok) {
            updatePasswordSuccess.textContent = 'Contraseña actualizada correctamente';
            updatePasswordSuccess.style.display = 'block';
            updatePasswordError.style.display = 'none';
            updatePasswordForm.reset();
        } else {
            const errorData = await response.text();
            throw new Error('Error al actualizar la contraseña' || errorData);
        }
    } catch (error) {
        console.error('Error:', error);
        updatePasswordError.textContent = error.message;
        updatePasswordError.style.display = 'block';
        updatePasswordSuccess.style.display = 'none';
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

if (updatePasswordForm) {
    updatePasswordForm.addEventListener('submit', updateUserPassword);
}

// Inicializar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', () => {
    getUserData();
});
