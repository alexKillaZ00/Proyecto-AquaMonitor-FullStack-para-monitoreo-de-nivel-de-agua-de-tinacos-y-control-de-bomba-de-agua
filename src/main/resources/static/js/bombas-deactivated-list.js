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
const userName = document.getElementById("userName")
const logoutBtn = document.getElementById("logoutBtn")
const bombasListEl = document.getElementById("bombasList")
const bombasLoadingEl = document.getElementById("bombasLoading")
const bombasErrorEl = document.getElementById("bombasError")
const bombasEmptyEl = document.getElementById("bombasEmpty")

// Función para hacer peticiones autenticadas a la API con manejo de refresh token
async function authenticatedFetch(url, options = {}) {
  const defaultOptions = {
    credentials: "include",
    headers: { "Content-Type": "application/json", ...options.headers },
  }
  let response = await fetch(url, { ...options, ...defaultOptions })
  if (response.status === 401 || response.status === 403) {
    const refresh = await fetch(`${API_BASE_URL}/auth/refresh`, { method: 'POST', credentials: 'include' })
    if (refresh.ok) response = await fetch(url, { ...options, ...defaultOptions })
    else throw new Error('No autorizado - Token inválido o expirado')
  }
  return response
}

async function logoutUser() {
  try {
    const r = await authenticatedFetch(`${API_BASE_URL}/auth/logout`, { method: 'POST', credentials: 'include' })
    if (r.ok) { localStorage.clear(); window.location.replace('login.html') } else { throw new Error('Error en el logout') }
  } catch (e) { console.error('logout', e); window.location.replace('login.html') }
}

async function getUserData() {
  const cached = localStorage.getItem('currentUser')
  if (cached) { window.currentUser = JSON.parse(cached); if (userName) userName.textContent = window.currentUser.nombre; return }
  try {
    const r = await authenticatedFetch(`${API_BASE_URL}/usuario/me`, { method: 'GET' })
    if (r && r.ok) { const data = await r.json(); if (userName) userName.textContent = data.nombre; window.currentUser = data; localStorage.setItem('currentUser', JSON.stringify(data)) }
    else { throw new Error(`Error al obtener datos del usuario: ${r.status}`) }
  } catch (e) { console.error('getUserData', e); if (userName) userName.textContent = 'Usuario'; await logoutUser() }
}

if (logoutBtn) {
  logoutBtn.addEventListener('click', () => { if (confirm('¿Estas seguro de que quieres salir?')) logoutUser() })
}

function clearStates() { if (bombasErrorEl) bombasErrorEl.style.display = 'none'; if (bombasEmptyEl) bombasEmptyEl.style.display = 'none' }
function showLoading(show) { if (bombasLoadingEl) bombasLoadingEl.style.display = show ? 'block' : 'none'; if (bombasListEl) bombasListEl.setAttribute('aria-busy', show ? 'true' : 'false') }
function showError(msg) { clearStates(); if (bombasErrorEl) { bombasErrorEl.textContent = msg; bombasErrorEl.style.display = 'block' } }

async function fetchBombasDesactivadas() {
  if (!bombasListEl) return
  showLoading(true)
  try {
    const resp = await authenticatedFetch(`${API_BASE_URL}/bombas/desactivadas`, { method: 'GET' })
    if (!resp.ok) throw new Error(`Error ${resp.status}`)
    const data = await resp.json()
    renderBombas(data)
  } catch (e) {
    console.error('fetchBombasDesactivadas', e)
    showError(e.message)
  } finally { showLoading(false) }
}

function truncate(text, max = 100) { if (!text) return ''; return text.length > max ? text.slice(0, max - 1) + '…' : text }
function escapeHtml(str) { return String(str).replace(/[&<>"];/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', ';': '&#59;' }[c] || c)) }
function escapeAttr(str) { return escapeHtml(str).replace(/'/g, '&#39;') }

function renderBombas(bombas) {
  clearStates()
  bombasListEl.innerHTML = ''
  if (!bombas || bombas.length === 0) { if (bombasEmptyEl) bombasEmptyEl.style.display = 'block'; return }

  const frag = document.createDocumentFragment()
  bombas.forEach(b => {
    const card = document.createElement('div')
    card.className = 'bomba-card'

    const estadoClass = (b.estado === 'ACTIVADA') ? 'estado-activada' : 'estado-desactivada'
    const modoClass = (b.modoBomba === 'AUTOMATICO') ? 'modo-automatico' : 'modo-manual'
    const asociacionClass = b.tieneTinaco ? 'con-tinaco' : 'sin-tinaco'

    const nombreTxt = truncate(escapeHtml(b.nombre || ''), 100)
    const ubicacionTxt = truncate(escapeHtml(b.ubicacion || ''), 100)
    const codigoTxt = truncate(escapeHtml(b.codigoIdentificador || ''), 15)

    const showPorcentajes = b.modoBomba === 'AUTOMATICO'
    const porcentajesHTML = showPorcentajes ? `
      <dt>Encender a</dt><dd>${b.porcentajeEncender ?? '-'}%</dd>
      <dt>Apagar a</dt><dd>${b.porcentajeApagar ?? '-'}%</dd>
    ` : ''

    card.innerHTML = `
      <header>
        <div class="bomba-icon">⚙️</div>
        <div class="bomba-title-wrap">
          <h3 class="bomba-name truncate" title="${escapeAttr(b.nombre || '')}">${nombreTxt}</h3>
          <div class="badges">
            <span class="badge ${estadoClass}">${escapeHtml(b.estado || '')}</span>
            <span class="badge ${modoClass}">${escapeHtml(b.modoBomba || '')}</span>
            <span class="badge ${asociacionClass}">${b.tieneTinaco ? 'CON TINACO' : 'SIN TINACO'}</span>
          </div>
        </div>
      </header>
      <div class="bomba-body">
        <dl>
          <dt>Ubicación</dt><dd class="truncate" title="${escapeAttr(b.ubicacion || '')}">${ubicacionTxt || '-'}</dd>
          <dt>Código</dt><dd class="truncate" title="${escapeAttr(b.codigoIdentificador || '')}">${codigoTxt || '-'}</dd>
          <dt>Encendida</dt><dd>${b.encendida ? 'Sí' : 'No'}</dd>
          ${porcentajesHTML}
        </dl>
      </div>
      <div class="bomba-actions">
        <div class="left-actions">
          <button id="btnActivar-${Number(b.id)}" class="btn btn-edit" title="Activar" onclick="activarBomba(${Number(b.id)})">Activar</button>
        </div>
      </div>
    `
    frag.appendChild(card)
  })
  bombasListEl.appendChild(frag)
}

// Activar bomba con confirmación y recarga
async function activarBomba(id) {
  if (id == null || isNaN(Number(id))) return
  const ok = confirm('¿Seguro que deseas activar esta bomba? Se mostrará nuevamente en la lista de bombas.')
  if (!ok) return
  const btn = document.getElementById(`btnActivar-${Number(id)}`)
  if (btn) btn.disabled = true
  try {
    const r = await authenticatedFetch(`${API_BASE_URL}/bombas/activar/${Number(id)}`, { method: 'POST' })
    if (!r.ok) throw new Error(`Error ${r.status}`)
    window.location.reload()
  } catch (e) {
    console.error('activarBomba', e)
    if (btn) btn.disabled = false
    showError('No se pudo activar la bomba. Intenta de nuevo.')
  }
}

document.addEventListener('DOMContentLoaded', () => {
  getUserData()
  fetchBombasDesactivadas()
})
