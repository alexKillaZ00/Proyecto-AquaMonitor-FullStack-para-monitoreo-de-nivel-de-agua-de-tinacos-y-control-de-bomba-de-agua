// Config
const API_BASE_URL = 'http://localhost:8080';

// Elements
const root = document.getElementById('resetRoot');
const form = document.getElementById('resetForm');
const newPasswordInput = document.getElementById('newPassword');
const confirmPasswordInput = document.getElementById('confirmPassword');
const toggleNew = document.getElementById('toggleNew');
const toggleConfirm = document.getElementById('toggleConfirm');
const newPasswordError = document.getElementById('newPasswordError');
const confirmPasswordError = document.getElementById('confirmPasswordError');
const resetBtn = document.getElementById('resetBtn');

// Read token from URL
const params = new URLSearchParams(window.location.search);
const token = params.get('token');

if (!token) {
    root.innerHTML = `
    <div class="header" style="text-align:center">
      <div style="font-size:56px">⚠️</div>
      <h1>Token faltante</h1>
      <p>El enlace es inválido. Solicita un nuevo restablecimiento.</p>
    </div>
    <button class="login-btn" onclick="window.location.href='request-email.html'">
      <span class="btn-text">Solicitar nuevo enlace</span>
    </button>
  `;
}

function isValidPassword(pwd) {
    return typeof pwd === 'string' && pwd.length >= 8 && pwd.length <= 255;
}

function showLoading() {
    resetBtn.classList.add('loading');
    resetBtn.querySelector('.btn-text').textContent = 'Guardando';
    resetBtn.disabled = true;
}

function hideLoading() {
    resetBtn.classList.remove('loading');
    resetBtn.querySelector('.btn-text').textContent = 'Guardar nueva contraseña';
    resetBtn.disabled = false;
}

function showSuccess() {
    root.innerHTML = `
    <div class="header" style="text-align:center">
      <div style="font-size:56px">✅</div>
      <h1>Contraseña actualizada</h1>
      <p>Ya puedes cerrar esta ventana e ir al inicio de sesión.</p>
    </div>
    <button class="login-btn" onclick="window.location.href='login.html'">
      <span class="btn-text">Ir a iniciar sesión</span>
    </button>
  `;
}

function setFieldError(el, msg) {
    el.textContent = msg;
}

function clearErrors() {
    newPasswordError.textContent = '';
    confirmPasswordError.textContent = '';
}

// Toggle visibility
function toggleVisibility(input, btn) {
    if (input.type === 'password') {
        input.type = 'text';
        btn.textContent = '🙈';
    } else {
        input.type = 'password';
        btn.textContent = '👁️';
    }
}

toggleNew.addEventListener('click', () => toggleVisibility(newPasswordInput, toggleNew));
toggleConfirm.addEventListener('click', () => toggleVisibility(confirmPasswordInput, toggleConfirm));

confirmPasswordInput.addEventListener('input', () => {
    if (confirmPasswordInput.value && confirmPasswordInput.value !== newPasswordInput.value) {
        setFieldError(confirmPasswordError, 'Las contraseñas no coinciden');
    } else {
        confirmPasswordError.textContent = '';
    }
});

form.addEventListener('submit', async (e) => {
    e.preventDefault();
    clearErrors();

    const newPassword = newPasswordInput.value;
    const confirmPassword = confirmPasswordInput.value;

    let hasError = false;
    if (!isValidPassword(newPassword)) {
        setFieldError(newPasswordError, 'La contraseña debe tener entre 8 y 255 caracteres');
        hasError = true;
    }
    if (newPassword !== confirmPassword) {
        setFieldError(confirmPasswordError, 'Las contraseñas no coinciden');
        hasError = true;
    }
    if (hasError) return;

    showLoading();

    setTimeout(async () => {
        try {
            const body = new URLSearchParams({ token, newPassword });
            const res = await fetch(`${API_BASE_URL}/auth/password-reset/confirm`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body
            });
            if (res.ok) {
                showSuccess();
            } else {
                const text = await res.text();
                setFieldError(confirmPasswordError, text || 'No se pudo restablecer la contraseña.');
            }
        } catch (err) {
            setFieldError(confirmPasswordError, 'Error de red. Intenta nuevamente.');
        } finally {
            hideLoading();
        }
    }, 2000);
});