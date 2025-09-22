// Configuración de la API
const API_BASE_URL = 'http://localhost:8080'; // Cambia por tu URL de API

// Elementos del DOM
const registerForm = document.getElementById('registerForm');
const nombreInput = document.getElementById('nombre');
const emailInput = document.getElementById('email');
const passwordInput = document.getElementById('password');
const confirmPasswordInput = document.getElementById('confirmPassword');
const registerBtn = document.getElementById('registerBtn');
const togglePassword = document.getElementById('togglePassword');
const toggleConfirmPassword = document.getElementById('toggleConfirmPassword');
const loginLink = document.getElementById('loginLink');
const root = document.getElementById('confirmEmailRoot');

// Elementos de error
const nombreError = document.getElementById('nombreError');
const emailError = document.getElementById('emailError');
const passwordError = document.getElementById('passwordError');
const confirmPasswordError = document.getElementById('confirmPasswordError');

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

// Mostrar/ocultar confirmar contraseña
toggleConfirmPassword.addEventListener('click', function () {
    if (confirmPasswordInput.type === 'password') {
        confirmPasswordInput.type = 'text';
        toggleConfirmPassword.textContent = '🙈';
    } else {
        confirmPasswordInput.type = 'password';
        toggleConfirmPassword.textContent = '👁️';
    }
});

function showSuccess(email) {
    root.innerHTML = `
    <div class="header" style="text-align:center">
      <div style="font-size:56px">✅</div>
      <h1>Confirma tu correo electrónico</h1>
      <p>Enviamos un enlace a <strong>${email}</strong>.</p>
      <p>Revisa tu bandeja de entrada o la carpeta de spam para confirmar el correo electrónico.</p>
    </div>
    <button class="register-btn" onclick="window.location.href='login.html'">
      <span class="btn-text">Ir a iniciar sesión</span>
    </button>
  `;
}

// Función para validar email básico
function isValidEmail(email) {
    const regexEmail = /^(?!.*\.\.)[a-zA-Z0-9][a-z0-9.+-_%]*@[a-z.-]+\.[a-z]{2,}$/;
    return regexEmail.test(email) && email.length <= 255;
}

// Función para validar nombre (básica para frontend)
function isValidName(name) {
    const nameRegex = /^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]{2,255}$/;
    return nameRegex.test(name);
}

// Función para validar contraseña (básica para frontend)
function isValidPassword(password) {
    return password.length >= 8 && password.length <= 255;
}

// Función para mostrar errores
function showError(errorElement, message) {
    errorElement.textContent = message;
}

// Función para limpiar errores
function clearError(errorElement) {
    errorElement.textContent = '';
}

// Función para limpiar todos los errores
function clearAllErrors() {
    clearError(nombreError);
    clearError(emailError);
    clearError(passwordError);
    clearError(confirmPasswordError);
}

// Función para mostrar estado de carga
function showLoading() {
    registerBtn.classList.add('loading');
    registerBtn.querySelector('.btn-text').textContent = 'Creando cuenta';
    registerBtn.disabled = true;
}

// Función para ocultar estado de carga
function hideLoading() {
    registerBtn.classList.remove('loading');
    registerBtn.querySelector('.btn-text').textContent = 'Crear Cuenta';
    registerBtn.disabled = false;
}

// Función para registrar usuario con la API
async function registerUser(nombre, email, password) {
    try {
        const response = await fetch(`${API_BASE_URL}/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                nombre: nombre,
                email: email,
                password: password
            })
        });

        const data = await response.json();

        if (response.ok) {

            //Si existe un usuario cacheado, eliminarlo
            if (localStorage.getItem('currentUser')) {
                localStorage.removeItem('currentUser');
            }

            // Cambiar apariencia del botón
            registerBtn.classList.remove('loading');
            registerBtn.style.background = '#27ae60';
            registerBtn.querySelector('.btn-text').textContent = '¡Cuenta Creada!';

            // Redirigir al dashboard
            setTimeout(() => {
                showSuccess(email);
            }, 1000);

        } else {
            throw new Error(data.message || 'Error de conexión. Verifica tu internet.');
        }
    } catch (error) {
        hideLoading();
        showError(confirmPasswordError, error.message);
    }
}

// Manejar el envío del formulario
registerForm.addEventListener('submit', function (e) {
    e.preventDefault();

    // Obtener valores
    const nombre = nombreInput.value.trim();
    const email = emailInput.value.trim();
    const password = passwordInput.value;
    const confirmPassword = confirmPasswordInput.value;

    // Limpiar errores anteriores
    clearError(nombreError);
    clearError(emailError);
    clearError(passwordError);
    clearError(confirmPasswordError);

    // Validar campos
    let hasErrors = false;

    // Validar nombre
    if (!nombre) {
        showError(nombreError, 'El nombre es requerido');
        hasErrors = true;
    } else if (!isValidName(nombre)) {
        showError(nombreError, 'El nombre debe tener entre 2 y 255 caracteres y sin simbolos');
        hasErrors = true;
    }

    // Validar email
    if (!email) {
        showError(emailError, 'El email es requerido');
        hasErrors = true;
    } else if (!isValidEmail(email)) {
        showError(emailError, 'Ingresa un email válido');
        hasErrors = true;
    }

    // Validar contraseña
    if (!password) {
        showError(passwordError, 'La contraseña es requerida');
        hasErrors = true;
    } else if (!isValidPassword(password)) {
        showError(passwordError, 'La contraseña debe tener entre 8 y 255 caracteres');
        hasErrors = true;
    }

    // Validar confirmación de contraseña
    if (!confirmPassword) {
        showError(confirmPasswordError, 'Confirma tu contraseña');
        hasErrors = true;
    } else if (password !== confirmPassword) {
        showError(confirmPasswordError, 'Las contraseñas no coinciden');
        hasErrors = true;
    }

    // Si hay errores, no continuar
    if (hasErrors) {
        return;
    }

    // Mostrar carga y registrar usuario
    showLoading();
    setTimeout(() => {
        registerUser(nombre, email, password);
    }, 1000);
});

// Validación en tiempo real para confirmar contraseña
confirmPasswordInput.addEventListener('input', function () {
    const password = passwordInput.value;
    const confirmPassword = this.value;

    if (confirmPassword && password !== confirmPassword) {
        showError(confirmPasswordError, 'Las contraseñas no coinciden');
    } else {
        clearError(confirmPasswordError);
    }
});