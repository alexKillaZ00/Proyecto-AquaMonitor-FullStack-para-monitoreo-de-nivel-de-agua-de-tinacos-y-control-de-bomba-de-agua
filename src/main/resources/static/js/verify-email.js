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
const root = document.getElementById('verifyRoot');
const verifyBtn = document.getElementById('verifyBtn');
const verifyError = document.getElementById('verifyError');

// Read token from URL
const params = new URLSearchParams(window.location.search);
const token = params.get('token');

if (!token) {
  root.innerHTML = `
    <div class="header" style="text-align:center">
      <div style="font-size:56px">⚠️</div>
      <h1>Token faltante</h1>
      <p>El enlace de verificación es inválido o ha expirado. Solicita un nuevo correo de verificación.</p>
    </div>
    <button class="login-btn" onclick="window.location.href='login.html'">
      <span class="btn-text">Ir a iniciar sesión</span>
    </button>
  `;
}

function showLoading() {
  verifyBtn.classList.add('loading');
  verifyBtn.querySelector('.btn-text').textContent = 'Verificando';
  verifyBtn.disabled = true;
}

function hideLoading() {
  verifyBtn.classList.remove('loading');
  verifyBtn.querySelector('.btn-text').textContent = 'Verificar correo';
  verifyBtn.disabled = false;
}

function showSuccess() {
  root.innerHTML = `
    <div class="header" style="text-align:center">
      <div style="font-size:56px">✅</div>
      <h1>Correo verificado</h1>
      <p>Tu correo fue verificado con éxito. Ya puedes iniciar sesión.</p>
    </div>
    <button class="login-btn" onclick="window.location.href='login.html'">
      <span class="btn-text">Ir a iniciar sesión</span>
    </button>
  `;
}

function setError(msg) {
  verifyError.textContent = msg || '';
}

async function verifyEmail() {
  if (!token) return; // ya se maneja arriba la UI
  setError('');
  showLoading();
  setTimeout(async () => {
    try {
      const body = new URLSearchParams({ token });
      const res = await fetch(`${API_BASE_URL}/auth/verify-email`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body
      });
      if (res.ok) {
        showSuccess();
      } else {
        const text = await res.json();
        setError(text.message || 'No se pudo verificar el correo. Inténtalo nuevamente.');
      }
    } catch (err) {
      setError('Error de red. Inténtalo nuevamente.');
    } finally {
      hideLoading();
    }
  }, 1500); // Simula retardo mínimo para UX
}

// Auto-verify on load for better UX (optional). User can also press button manually.
verifyBtn?.addEventListener('click', () => verifyEmail());

// Intento automático si hay token
/*if (token) {
  // Pequeño delay para permitir ver la página antes de auto-verificar
  setTimeout(() => verifyEmail(), 300);
}*/
