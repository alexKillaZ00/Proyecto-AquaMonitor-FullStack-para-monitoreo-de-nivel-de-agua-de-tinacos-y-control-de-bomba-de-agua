// Config
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

// Elements
const form = document.getElementById('requestEmailForm');
const emailInput = document.getElementById('email');
const emailError = document.getElementById('emailError');
const sendBtn = document.getElementById('sendBtn');
const root = document.getElementById('requestEmailRoot');

function isValidEmail(email) {
  const regexEmail = /^(?!.*\..*\..*)[a-zA-Z0-9][a-z0-9.+\-_%]*@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
  return regexEmail.test(email) && email.length <= 255;
}

function showLoading() {
  sendBtn.classList.add('loading');
  sendBtn.querySelector('.btn-text').textContent = 'Enviando';
  sendBtn.disabled = true;
}

function hideLoading() {
  sendBtn.classList.remove('loading');
  sendBtn.querySelector('.btn-text').textContent = 'Enviar enlace';
  sendBtn.disabled = false;
}

function showSuccess(email) {
  root.innerHTML = `
    <div class="header" style="text-align:center">
      <div style="font-size:56px">✅</div>
      <h1>Correo enviado</h1>
      <p>Si el correo existe, enviamos un enlace a <strong>${email}</strong>.</p>
      <p>Revisa tu bandeja de entrada o la carpeta de spam.</p>
    </div>
    <button class="login-btn" onclick="window.location.href='login.html'">
      <span class="btn-text">Ir a iniciar sesión</span>
    </button>
  `;
}

function showError(msg) {
  emailError.textContent = msg;
}

form.addEventListener('submit', async (e) => {
  e.preventDefault();
  emailError.textContent = '';
  const email = emailInput.value.trim();
  if (!isValidEmail(email)) {
    showError('Ingresa un email válido (máx. 255 caracteres)');
    return;
  }

  try {
    showLoading();
    // Backend expects @RequestParam String email, so send as form data
    const body = new URLSearchParams({ email });
    const res = await fetch(`${API_BASE_URL}/auth/password-reset/request`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body
    });

    if (res.ok) {
      showSuccess(email);
    } else {
      const text = await res.text();
      showError(text || 'No se pudo procesar la solicitud. Intenta de nuevo.');
    }
  } catch (err) {
    showError('Error de red. Intenta nuevamente.');
  } finally {
    hideLoading();
  }
});
