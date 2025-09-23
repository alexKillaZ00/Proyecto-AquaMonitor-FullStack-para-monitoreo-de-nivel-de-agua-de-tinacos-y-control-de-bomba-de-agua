// Configuración de la API y utilidades comunes
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
  } catch (e) { if (userName) userName.textContent = 'Usuario'; await logoutUser() }
}

const logoutBtn = document.getElementById('logoutBtn')
if (logoutBtn) logoutBtn.addEventListener('click', () => { if (confirm('¿Estas seguro de que quieres salir?')) logoutUser() })

document.addEventListener('DOMContentLoaded', () => { getUserData(); loadTinacosSinBomba() })

// Estado y helpers UI
let homeIntervals = []
function clearHomeIntervals() { homeIntervals.forEach(id => clearInterval(id)); homeIntervals = [] }
const homeGrid = document.getElementById('homeGrid')
const homeLoading = document.getElementById('homeLoading')
const homeError = document.getElementById('homeError')
const homeEmpty = document.getElementById('homeEmpty')
function setHomeState({ loading = false, error = '', empty = false }) {
  if (homeLoading) homeLoading.style.display = loading ? 'block' : 'none'
  if (homeError) { homeError.textContent = error || ''; homeError.style.display = error ? 'block' : 'none' }
  if (homeEmpty) homeEmpty.style.display = empty ? 'block' : 'none'
}
function showHomeError(msg) { if (homeError) { homeError.textContent = msg || 'Ocurrió un error'; homeError.style.display = 'block' } }

function escapeHtml(str) { return String(str || '').replace(/[&<>"]/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[c])) }
function escapeAttr(str) { return escapeHtml(str).replace(/'/g, '&#39;') }
function truncate(text, max = 25) { const t = String(text || ''); return t.length > max ? t.slice(0, max - 3) + '...' : t }

async function loadTinacosSinBomba() {
  if (!homeGrid) return
  setHomeState({ loading: true, error: '', empty: false })
  homeGrid.innerHTML = ''
  clearHomeIntervals()
  try {
    const res = await authenticatedFetch(`${API_BASE_URL}/tinacos/sin-bomba`, { method: 'GET' })
    if (!res.ok) throw new Error(`Error ${res.status}`)
    const tinacos = await res.json()
    renderTinacosSinBomba(tinacos)
  } catch (e) { setHomeState({ loading: false, error: e.message, empty: false }); return }
  setHomeState({ loading: false, error: '', empty: homeGrid.children.length === 0 })
}

function renderTinacosSinBomba(tinacos) {
  homeGrid.innerHTML = ''
  if (!tinacos || tinacos.length === 0) { setHomeState({ loading: false, error: '', empty: true }); return }
  const frag = document.createDocumentFragment()
  tinacos.forEach(tin => {
    const card = document.createElement('div')
    card.className = 'tc-card'
    card.innerHTML = `
      <div class="tc-header">
        <div class="tc-title">
          <h3 title="${escapeAttr(tin.nombre || '')}">${escapeHtml(truncate(tin.nombre || 'Tinaco', 30))}</h3>
          <small title="${escapeAttr(tin.ubicacion || '')}">${escapeHtml(truncate(tin.ubicacion || '', 40))}</small>
        </div>
        <div class="tc-badges">
          <span class="badge-sm badge-overflow" id="badgeOverflow-${tin.id}" style="display:none">Desbordado</span>
        </div>
      </div>
      <div class="tc-body">
        <div class="tank" id="tank-${tin.id}">
          <div class="level-label" id="label-${tin.id}">--%</div>
          <div class="water" id="water-${tin.id}" style="height:0%"></div>
        </div>
        <dl class="tc-info">
          <dt>Capacidad</dt><dd>${Number(tin.capacidadLitros ?? 0)} L</dd>
          <dt>Altura máx.</dt><dd>${Number(tin.alturaMaximaCm ?? 0)} cm</dd>
          <dt>Código Tinaco</dt><dd>${escapeHtml(tin.codigoIdentificador || '-')}
        </dl>
      </div>
      <div class="tc-footer">
        <div class="pump-controls"></div>
        <button class="btn-link" id="linkbtn-${tin.id}" title="Vincular bomba">Vincular bomba</button>
      </div>
    `
    frag.appendChild(card)

    const int1 = setInterval(() => updateNivel(tin.id), 2000)
    homeIntervals.push(int1)

    queueMicrotask(() => {
      const linkBtn = document.getElementById(`linkbtn-${tin.id}`)
      if (linkBtn) linkBtn.addEventListener('click', () => openVincularModal(tin.id))
    })
  })
  homeGrid.appendChild(frag)
}

async function updateNivel(tinacoId) {
  try {
    const r = await authenticatedFetch(`${API_BASE_URL}/nivel/actual/${tinacoId}`, { method: 'GET' })
    if (!r.ok) return
    const data = await r.json()
    const pct = Math.max(0, Math.min(100, Number(data.porcentajeLlenado ?? 0)))
    const water = document.getElementById(`water-${tinacoId}`)
    const label = document.getElementById(`label-${tinacoId}`)
    if (water) water.style.height = `${pct}%`
    if (label) label.textContent = `${pct.toFixed(1)}%`
    const badge = document.getElementById(`badgeOverflow-${tinacoId}`)
    if (badge) badge.style.display = data.desbordado ? 'inline-block' : 'none'
  } catch (_) { }
}

// Modal Vincular
function getLinkModalEls() {
  return {
    overlay: document.getElementById('linkModalOverlay'),
    closeBtn: document.getElementById('linkModalClose'),
    cancelBtn: document.getElementById('linkModalCancelBtn'),
    vincularBtn: document.getElementById('linkModalVincularBtn'),
    loading: document.getElementById('linkModalLoading'),
    error: document.getElementById('linkModalError'),
    success: document.getElementById('linkModalSuccess'),
    list: document.getElementById('linkModalList'),
  }
}

function resetLinkModalState(els) {
  if (!els) els = getLinkModalEls()
  if (els.loading) els.loading.style.display = 'none'
  if (els.error) { els.error.style.display = 'none'; els.error.textContent = '' }
  if (els.success) { els.success.style.display = 'none'; els.success.textContent = '' }
  if (els.list) els.list.innerHTML = ''
  if (els.vincularBtn) { els.vincularBtn.disabled = true; els.vincularBtn.textContent = 'Vincular' }
}

function openVincularModal(tinacoId) {
  const els = getLinkModalEls()
  if (!els.overlay) return
  resetLinkModalState(els)
  els.overlay.dataset.tinacoId = String(tinacoId)
  els.overlay.style.display = 'flex'
  if (els.closeBtn && !els.closeBtn._wired) { els.closeBtn._wired = true; els.closeBtn.addEventListener('click', closeVincularModal) }
  if (els.cancelBtn && !els.cancelBtn._wired) { els.cancelBtn._wired = true; els.cancelBtn.addEventListener('click', closeVincularModal) }
  if (els.vincularBtn && !els.vincularBtn._wired) { els.vincularBtn._wired = true; els.vincularBtn.addEventListener('click', onConfirmVincular) }
  loadBombasSinTinaco()
}

function closeVincularModal() {
  const els = getLinkModalEls()
  if (els.overlay) { els.overlay.style.display = 'none'; delete els.overlay.dataset.tinacoId }
  resetLinkModalState(els)
}

async function loadBombasSinTinaco() {
  const els = getLinkModalEls()
  if (els.loading) els.loading.style.display = 'block'
  if (els.error) { els.error.style.display = 'none'; els.error.textContent = '' }
  if (els.list) els.list.innerHTML = ''
  try {
    const res = await authenticatedFetch(`${API_BASE_URL}/bombas/sin-tinaco`, { method: 'GET' })
    if (!res.ok) throw new Error(`Error ${res.status}`)
    const bombas = await res.json()
    if (!bombas || bombas.length === 0) {
      if (els.list) els.list.innerHTML = '<div class="empty-message">No hay bombas disponibles para vincular.</div>'
      if (els.vincularBtn) els.vincularBtn.disabled = true
      return
    }
    const frag = document.createDocumentFragment()
    bombas.forEach(b => {
      const label = document.createElement('label')
      label.className = 'option-row'
      label.innerHTML = `
        <input type="radio" name="bombaOption" value="${String(b.id)}" />
        <div class="option-meta">
          <div class="option-title" title="${escapeAttr(b.nombre || '')}">${escapeHtml(truncate(b.nombre || 'Bomba', 40))}</div>
          <div class="option-subtitle" title="${escapeAttr(b.ubicacion || '')}">${escapeHtml(truncate(b.ubicacion || '-', 50))} · Código: ${escapeHtml(b.codigoIdentificador || '-')}</div>
        </div>
      `
      frag.appendChild(label)
    })
    if (els.list) els.list.appendChild(frag)
    const radios = els.list ? els.list.querySelectorAll('input[name="bombaOption"]') : []
    radios.forEach(r => r.addEventListener('change', () => { if (els.vincularBtn) els.vincularBtn.disabled = !getSelectedBombaId() }))
  } catch (e) {
    if (els.error) { els.error.textContent = e.message || 'Error al cargar las bombas'; els.error.style.display = 'block' }
  } finally {
    if (els.loading) els.loading.style.display = 'none'
  }
}

function getSelectedBombaId() {
  const list = document.getElementById('linkModalList')
  if (!list) return null
  const sel = list.querySelector('input[name="bombaOption"]:checked')
  return sel ? Number(sel.value) : null
}

async function onConfirmVincular() {
  const els = getLinkModalEls()
  const tinacoId = els.overlay && els.overlay.dataset ? Number(els.overlay.dataset.tinacoId) : null
  const bombaId = getSelectedBombaId()
  if (!(tinacoId > 0) || !(bombaId > 0)) return
  try {
    if (els.vincularBtn) { els.vincularBtn.disabled = true; els.vincularBtn.textContent = 'Vinculando...' }
    if (els.error) { els.error.style.display = 'none'; els.error.textContent = '' }
    const res = await authenticatedFetch(`${API_BASE_URL}/tinacos/vincular-bomba`, { method: 'POST', body: JSON.stringify({ tinacoId, bombaId }) })
    if (!res.ok) { const t = await res.text().catch(() => ''); throw new Error(t || `Error ${res.status}`) }
    if (els.success) { els.success.textContent = 'Bomba vinculada correctamente. Recargando...'; els.success.style.display = 'block' }
    setTimeout(() => window.location.reload(), 1000)
  } catch (e) {
    if (els.error) { els.error.textContent = e.message || 'No se pudo vincular la bomba.'; els.error.style.display = 'block' }
    if (els.vincularBtn) { els.vincularBtn.disabled = false; els.vincularBtn.textContent = 'Vincular' }
  }
}
