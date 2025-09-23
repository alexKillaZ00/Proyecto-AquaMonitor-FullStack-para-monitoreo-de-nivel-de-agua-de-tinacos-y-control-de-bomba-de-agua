//login.js:
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
const loginForm = document.getElementById('loginForm');
const emailInput = document.getElementById('email');
const passwordInput = document.getElementById('password');
const loginBtn = document.getElementById('loginBtn');
const togglePassword = document.getElementById('togglePassword');
const forgotPassword = document.getElementById('forgotPassword');
const emailError = document.getElementById('emailError');
const passwordError = document.getElementById('passwordError');

// Mostrar/ocultar contraseña
togglePassword.addEventListener('click', function () {
    if (passwordInput.type === 'password') {
        passwordInput.type = 'text';
        togglePassword.textContent = '🙈';
    } else {
        passwordInput.type = 'password';
        togglePassword.textContent = '👁️';
    }
});

// Función para validar email básico
function isValidEmail(email) {
    return email.includes('@') && email.includes('.');
}

// Función para mostrar errores
function showError(errorElement, message) {
    errorElement.textContent = message;
}

// Función para limpiar errores
function clearError(errorElement) {
    errorElement.textContent = '';
}

// Función para mostrar estado de carga
function showLoading() {
    loginBtn.classList.add('loading');
    loginBtn.querySelector('.btn-text').textContent = 'Verificando';
    loginBtn.disabled = true;
}

// Función para ocultar estado de carga
function hideLoading() {
    loginBtn.classList.remove('loading');
    loginBtn.querySelector('.btn-text').textContent = 'Iniciar Sesión';
    loginBtn.disabled = false;
}

// Función para hacer login con la API
async function loginUser(email, password) {
    try {
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            credentials: 'include', // Importante: incluir cookies
            body: JSON.stringify({
                email: email,
                password: password
            })
        });

        const data = await response.json();

        if (response.ok) {
            // Login exitoso - La cookie JWT ya se estableció automáticamente

            //Si existe un usuario cacheado, eliminarlo
            if (localStorage.getItem('currentUser')) {
                localStorage.removeItem('currentUser');
            }

            // Cambiar apariencia del botón
            loginBtn.classList.remove('loading');
            loginBtn.style.background = '#27ae60';
            loginBtn.querySelector('.btn-text').textContent = '¡Acceso Concedido!';

            // Redirigir al dashboard
            setTimeout(() => {
                window.location.replace('dashboard.html');
            }, 1000);

        } else {
            /*loginBtn.classList.remove('loading');
            loginBtn.querySelector('.btn-text').textContent = '¡Acceso Denegado!';*/

            // Error del servidor
            hideLoading();

            // Manejar diferentes tipos de error según el status
            if (response.status === 401) {
                showError(passwordError, data.message || 'Credenciales inválidas');
            } else if (response.status === 422) {
                showError(emailError, data.message || 'Datos inválidos');
            } else {
                showError(passwordError, data.message || 'Error en el servidor');
            }
        }
    } catch (error) {
        hideLoading();
        showError(passwordError, 'Error de conexión. Verifica tu internet.');
        console.error('Error:', error);
    }
}

// Manejar el envío del formulario
loginForm.addEventListener('submit', function (e) {
    e.preventDefault();

    // Obtener valores
    const email = emailInput.value.trim();
    const password = passwordInput.value;

    // Limpiar errores anteriores
    clearError(emailError);
    clearError(passwordError);

    // Validar campos vacíos
    let hasErrors = false;

    if (!email) {
        showError(emailError, 'El email es requerido');
        hasErrors = true;
    } else if (!isValidEmail(email)) {
        showError(emailError, 'Ingresa un email válido');
        hasErrors = true;
    }

    if (!password) {
        showError(passwordError, 'La contraseña es requerida');
        hasErrors = true;
    }

    // Si hay errores, no continuar
    if (hasErrors) {
        return;
    }

    // Mostrar carga y hacer login
    showLoading();

    setTimeout(() => {
        loginUser(email, password);
    }, 700);
});

// Manejar "Olvidaste tu contraseña"
/*forgotPassword.addEventListener('click', function (e) {
    e.preventDefault();

    const email = emailInput.value.trim();

    if (!email) {
        alert('Ingresa tu email primero');
        emailInput.focus();
        return;
    }

    if (!isValidEmail(email)) {
        alert('Ingresa un email válido');
        emailInput.focus();
        return;
    }

    alert('Funcionalidad de recuperación de contraseña próximamente');
});*/