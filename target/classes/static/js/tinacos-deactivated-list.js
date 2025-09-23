// tinacos-deactivated-list.js
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

const userName = document.getElementById('userName')
const logoutBtn = document.getElementById('logoutBtn')
const tinacosListEl = document.getElementById('tinacosList')
const tinacosLoadingEl = document.getElementById('tinacosLoading')
const tinacosErrorEl = document.getElementById('tinacosError')
const tinacosEmptyEl = document.getElementById('tinacosEmpty')

async function authenticatedFetch(url, options = {}) {
  const defaultOptions = { credentials: 'include', headers: { 'Content-Type': 'application/json', ...(options.headers || {}) } }
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
    const res = await authenticatedFetch(`${API_BASE_URL}/auth/logout`, { method: 'POST' })
    if (res.ok) { localStorage.clear(); window.location.replace('login.html') } else {
      throw new Error("Error en el logout")
    }
  } catch { window.location.replace('login.html') }
}

async function getUserData() {
  const cached = localStorage.getItem('currentUser')
  if (cached) { window.currentUser = JSON.parse(cached); if (userName) userName.textContent = window.currentUser.nombre; return }
  try {
    const res = await authenticatedFetch(`${API_BASE_URL}/usuario/me`, { method: 'GET' })
    if (res.ok) { const data = await res.json(); if (userName) userName.textContent = data.nombre; window.currentUser = data; localStorage.setItem('currentUser', JSON.stringify(data)) }
    else throw new Error('No se pudo obtener el usuario')
  } catch { if (userName) userName.textContent = 'Usuario'; await logoutUser() }
}

if (logoutBtn) logoutBtn.addEventListener('click', () => { if (confirm('¿Estas seguro de que quieres salir?')) logoutUser() })

function truncate(text, max = 100) { if (!text) return ''; return text.length > max ? text.slice(0, max - 1) + '…' : text }
function clearStates() { if (tinacosErrorEl) tinacosErrorEl.style.display = 'none'; if (tinacosEmptyEl) tinacosEmptyEl.style.display = 'none' }
function showLoading(show) { if (tinacosLoadingEl) tinacosLoadingEl.style.display = show ? 'block' : 'none'; if (tinacosListEl) tinacosListEl.setAttribute('aria-busy', show ? 'true' : 'false') }
function showError(msg) { clearStates(); if (tinacosErrorEl) { tinacosErrorEl.textContent = msg; tinacosErrorEl.style.display = 'block' } }

async function fetchTinacosDesactivados() {
  if (!tinacosListEl) return
  showLoading(true)
  try {
    const res = await authenticatedFetch(`${API_BASE_URL}/tinacos/desactivados`, { method: 'GET' })
    if (!res.ok) throw new Error(`Error al obtener tinacos desactivados (${res.status})`)
    const data = await res.json()
    renderTinacos(data)
  } catch (e) { showError(e.message); console.error('fetchTinacosDesactivados error:', e) } finally { showLoading(false) }
}

function renderTinacos(tinacos) {
  clearStates()
  tinacosListEl.innerHTML = ''
  if (!tinacos || tinacos.length === 0) { if (tinacosEmptyEl) tinacosEmptyEl.style.display = 'block'; return }

  const frag = document.createDocumentFragment()
  tinacos.forEach(t => {
    const card = document.createElement('div')
    card.className = 'tinaco-card'
    const conBomba = !!t.tieneBomba
    const icon = '💧'
    const bombaBadgeClass = conBomba ? 'bomba-activa' : 'sin-bomba'
    const bombaIcon = conBomba ? '⚙️' : '⛔'
    const bombaTexto = conBomba ? 'Con bomba' : 'Sin bomba'

    card.innerHTML = `
      <header>
        <div class="tinaco-icon" aria-hidden="true">${icon}</div>
        <div class="tinaco-title-wrap">
          <h2 class="tinaco-name truncate" title="${escapeHtml(t.nombre || '')}">${truncate(t.nombre || '')}</h2>
          <div class="badges">
            <span class="badge estado-desactivado">DESACTIVADO</span>
            <span class="badge ${bombaBadgeClass}" title="${bombaTexto}">${bombaIcon} ${bombaTexto}</span>
          </div>
        </div>
      </header>
      <div class="tinaco-body">
        <dl>
          <dt>Código:</dt><dd>${escapeHtml(t.codigoIdentificador || '-')}</dd>
          <dt>Ubicación:</dt><dd class="truncate" title="${escapeHtml(t.ubicacion || '')}">${truncate(t.ubicacion || '')}</dd>
          <dt>Capacidad:</dt><dd>${t.capacidadLitros != null ? t.capacidadLitros + ' L' : '-'}</dd>
          <dt>Altura máx.:</dt><dd>${t.alturaMaximaCm != null ? t.alturaMaximaCm + ' cm' : '-'}</dd>
          <dt>Destino:</dt><dd class="truncate" title="${escapeHtml(t.destinoAgua || '')}">${truncate(t.destinoAgua || '')}</dd>
        </dl>
      </div>
      <div class="tinaco-actions">
        <button id="btnActivar-${Number(t.id)}" class="btn btn-edit" type="button" onclick="activarTinaco(${Number(t.id)})" aria-label="Activar tinaco ${escapeAttr(t.nombre || '')}">Activar</button>
      </div>
    `
    frag.appendChild(card)
  })
  tinacosListEl.appendChild(frag)
}

function escapeHtml(str) { return String(str).replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', '\'': '&#39;' }[c] || c)) }
function escapeAttr(str) { return escapeHtml(str).replace(/'/g, '&#39;') }

async function activarTinaco(id) {
  if (id == null || isNaN(Number(id))) return
  const ok = confirm('¿Seguro que deseas activar este tinaco? Volverá a mostrarse en la lista principal de tinacos.')
  if (!ok) return

  const btn = document.getElementById(`btnActivar-${Number(id)}`)
  try {
    if (btn) { btn.disabled = true; btn.textContent = 'Activando…' }
    const res = await authenticatedFetch(`${API_BASE_URL}/tinacos/activar/${Number(id)}`, { method: 'POST' })
    if (!res.ok) throw new Error(`Error al activar tinaco (${res.status})`)
    window.location.reload()
  } catch (e) {
    if (btn) { btn.disabled = false; btn.textContent = 'Activar' }
    showError(e.message)
    console.error('activarTinaco error:', e)
  }
}

// Init
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', () => { getUserData(); fetchTinacosDesactivados() })
} else { getUserData(); fetchTinacosDesactivados() }
