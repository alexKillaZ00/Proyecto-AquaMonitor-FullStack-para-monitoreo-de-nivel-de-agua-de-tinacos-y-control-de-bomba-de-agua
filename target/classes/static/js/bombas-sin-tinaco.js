// Configuración y utilidades comunes
const API_BASE_URL = 'http://localhost:8080'

async function authenticatedFetch(url, options = {}) {
  const defaultOptions = { credentials: 'include', headers: { 'Content-Type': 'application/json', ...(options.headers || {}) } }
  let response = await fetch(url, { ...options, ...defaultOptions })
  if (response.status === 401 || response.status === 403) {
    const rr = await fetch(`${API_BASE_URL}/auth/refresh`, { method: 'POST', credentials: 'include' })
    if (rr.ok) response = await fetch(url, { ...options, ...defaultOptions })
    else throw new Error('No autorizado - Token inválido o expirado')
  }
  return response
}

async function logoutUser() {
  try {
    const r = await authenticatedFetch(`${API_BASE_URL}/auth/logout`, { method: 'POST' })
    if (r.ok) { localStorage.clear(); window.location.replace('login.html') } else { throw new Error('Error en el logout') }
  } catch (_) { window.location.replace('login.html') }
}

async function getUserData() {
  const userName = document.getElementById('userName')
  const cached = localStorage.getItem('currentUser')
  if (cached) { const u = JSON.parse(cached); if (userName) userName.textContent = u.nombre; window.currentUser = u; return }
  try {
    const res = await authenticatedFetch(`${API_BASE_URL}/usuario/me`, { method: 'GET' })
    if (!res.ok) throw new Error(`Error ${res.status}`)
    const u = await res.json(); if (userName) userName.textContent = u.nombre; window.currentUser = u; localStorage.setItem('currentUser', JSON.stringify(u))
  } catch (_) { if (userName) userName.textContent = 'Usuario'; await logoutUser() }
}

const logoutBtn = document.getElementById('logoutBtn')
if (logoutBtn) logoutBtn.addEventListener('click', () => { if (confirm('¿Estas seguro de que quieres salir?')) logoutUser() })

document.addEventListener('DOMContentLoaded', () => { getUserData(); loadBombasSinTinaco() })

// Estado de vista (sin polling)

// Helpers UI
const homeGrid = document.getElementById('homeGrid')
const homeLoading = document.getElementById('homeLoading')
const homeError = document.getElementById('homeError')
const homeEmpty = document.getElementById('homeEmpty')

function setState({ loading = false, error = '', empty = false }) {
  if (homeLoading) homeLoading.style.display = loading ? 'block' : 'none'
  if (homeError) { homeError.textContent = error || ''; homeError.style.display = error ? 'block' : 'none' }
  if (homeEmpty) homeEmpty.style.display = empty ? 'block' : 'none'
}

// Mostrar errores en la página (como en tinacos-bombas)
function showError(msg) {
  if (!homeError) return
  homeError.textContent = msg || 'Ocurrió un error'
  homeError.style.display = 'block'
}

function escapeHtml(str) { return String(str || '').replace(/[&<>"]/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;'}[c])) }
function escapeAttr(str) { return escapeHtml(str).replace(/'/g, '&#39;') }
function truncate(text, max = 25) { const t = String(text || ''); return t.length > max ? t.slice(0, max - 3) + '...' : t }

async function loadBombasSinTinaco() {
  if (!homeGrid) return
  setState({ loading: true, error: '', empty: false })
  homeGrid.innerHTML = ''
  try {
    const res = await authenticatedFetch(`${API_BASE_URL}/bombas/sin-tinaco`, { method: 'GET' })
    if (!res.ok) throw new Error(`Error ${res.status}`)
    const bombas = await res.json()
    renderBombas(bombas)
  } catch (e) { setState({ loading: false, error: e.message, empty: false }); return }
  setState({ loading: false, error: '', empty: homeGrid.children.length === 0 })
}

function renderBombas(items) {
  homeGrid.innerHTML = ''
  if (!items || items.length === 0) { setState({ loading: false, error: '', empty: true }); return }
  const frag = document.createDocumentFragment()
  items.forEach(bom => {
    const card = document.createElement('div')
    card.className = 'tc-card'
    card.innerHTML = `
      <div class="tc-header">
        <div class="tc-title">
          <h3 title="${escapeAttr(bom.nombre || '')}">${escapeHtml(truncate(bom.nombre || 'Bomba', 25))}</h3>
          <small title="${escapeAttr(bom.ubicacion || '')}">${escapeHtml(truncate(bom.ubicacion || '-', 30))}</small>
        </div>
        <div class="tc-badges">
          <span class="badge-sm badge-manual">Modo: Manual</span>
          <span class="badge-sm ${bom.encendida ? 'badge-pump-on' : ''}" id="badgePump-${bom.id}">Bomba: ${bom.encendida ? 'Encendida' : 'Apagada'}</span>
        </div>
      </div>
      <div class="tc-body">
        <div class="pump-icon">
          <span class="pump-gear ${bom.encendida ? 'spin' : ''}" id="pumpIcon-${bom.id}">⚙️</span>
        </div>
        <dl class="tc-info">
          <dt>Nombre</dt><dd title="${escapeAttr(bom.nombre || '')}">${escapeHtml(truncate(bom.nombre || '-', 20))}</dd>
          <dt>Código</dt><dd>${escapeHtml(bom.codigoIdentificador || '-')}</dd>
          <dt>Ubicación</dt><dd title="${escapeAttr(bom.ubicacion || '')}">${escapeHtml(truncate(bom.ubicacion || '-', 20))}</dd>
          <!-- <dt>Modo</dt><dd>MANUAL</dd> -->
        </dl>
      </div>
      <div class="tc-footer">
        <div class="pump-controls" id="controls-${bom.id}">
          <span class="state-indicator ${bom.encendida ? 'state-on' : 'state-off'}" id="pstate-${bom.id}"></span>
          <label>
            <input type="checkbox" id="ptoggle-${bom.id}" ${bom.encendida ? 'checked' : ''} />
            Encender bomba
          </label>
        </div>
      </div>
    `
    frag.appendChild(card)

  // Checar estado una sola vez al cargar
  queueMicrotask(() => { refreshPumpState(bom) })

    // Wire manual toggle
    queueMicrotask(() => {
      const toggle = document.getElementById(`ptoggle-${bom.id}`)
      if (toggle) {
        toggle.addEventListener('change', async (ev) => {
          const checked = ev.target.checked
          const btn = ev.target
          btn.disabled = true
          try {
            const url = `${API_BASE_URL}/bombas/${checked ? 'encender' : 'apagar'}/${bom.id}`
            const r = await authenticatedFetch(url, { method: 'POST' })
            if (!r.ok) { const t = await r.text().catch(() => ''); throw new Error(t || `Error ${r.status}`) }
            // reflect state
            const ind = document.getElementById(`pstate-${bom.id}`)
            if (ind) { ind.classList.toggle('state-on', checked); ind.classList.toggle('state-off', !checked) }
            const badge = document.getElementById(`badgePump-${bom.id}`)
            if (badge) { badge.textContent = `Bomba: ${checked ? 'Encendida' : 'Apagada'}`; badge.classList.toggle('badge-pump-on', checked) }
            const icon = document.getElementById(`pumpIcon-${bom.id}`)
            if (icon) icon.classList.toggle('spin', checked)
          } catch (e) {
            ev.target.checked = !checked
            showError(e.message)
          } finally { btn.disabled = false }
        })
      }
    })
  })
  homeGrid.appendChild(frag)
}

async function refreshPumpState(bomba) {
  if (!bomba || !bomba.codigoIdentificador || bomba.id == null) return
  try {
    const r = await authenticatedFetch(`${API_BASE_URL}/bombas/estado/${encodeURIComponent(bomba.codigoIdentificador)}`, { method: 'GET' })
    if (!r.ok) return
    const encendida = await r.json()
    const ind = document.getElementById(`pstate-${bomba.id}`)
    if (ind) { ind.classList.toggle('state-on', !!encendida); ind.classList.toggle('state-off', !encendida) }
    const badge = document.getElementById(`badgePump-${bomba.id}`)
    if (badge) { badge.textContent = `Bomba: ${encendida ? 'Encendida' : 'Apagada'}`; badge.classList.toggle('badge-pump-on', !!encendida) }
    const icon = document.getElementById(`pumpIcon-${bomba.id}`)
    if (icon) icon.classList.toggle('spin', !!encendida)
    const toggle = document.getElementById(`ptoggle-${bomba.id}`)
    if (toggle) toggle.checked = !!encendida
  } catch (_) {}
}
