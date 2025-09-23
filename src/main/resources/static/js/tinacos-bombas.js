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

// Reutiliza las utilidades de otras páginas si están definidas globalmente,
// de lo contrario define versiones locales mínimas.
async function authenticatedFetch(url, options = {}) {
    const defaultOptions = {
        credentials: "include",
        headers: { "Content-Type": "application/json", ...(options.headers || {}) },
    }
    let response = await fetch(url, { ...options, ...defaultOptions })
    if (response.status === 401 || response.status === 403) {
        const refreshResponse = await fetch(`${API_BASE_URL}/auth/refresh`, { method: 'POST', credentials: 'include' })
        if (refreshResponse.ok) {
            response = await fetch(url, { ...options, ...defaultOptions })
        } else {
            throw new Error('No autorizado - Token inválido o expirado')
        }
    }
    return response
}

async function logoutUser() {
    try {
        const response = await authenticatedFetch(`${API_BASE_URL}/auth/logout`, { method: 'POST' })
        if (response.ok) {
            localStorage.clear()
            window.location.replace('login.html')
        } else {
            throw new Error('Error en el logout')
        }
    } catch (_) {
        window.location.replace('login.html')
    }
}

async function getUserData() {
    const userName = document.getElementById('userName')
    const cachedUser = localStorage.getItem('currentUser')
    if (cachedUser) {
        const u = JSON.parse(cachedUser)
        if (userName) userName.textContent = u.nombre
        window.currentUser = u
        return
    }
    try {
        const res = await authenticatedFetch(`${API_BASE_URL}/usuario/me`, { method: 'GET' })
        if (!res.ok) throw new Error(`Error ${res.status}`)
        const u = await res.json()
        if (userName) userName.textContent = u.nombre
        window.currentUser = u
        localStorage.setItem('currentUser', JSON.stringify(u))
    } catch (e) {
        if (userName) userName.textContent = 'Usuario'
        await logoutUser()
    }
}

const logoutBtn = document.getElementById('logoutBtn')
if (logoutBtn) {
    logoutBtn.addEventListener('click', () => { if (confirm('¿Estas seguro de que quieres salir?')) logoutUser() })
}

document.addEventListener('DOMContentLoaded', () => {
    getUserData()
    loadTinacosConBomba()
})

// Estado de vista
let homeIntervals = []
function clearHomeIntervals() { homeIntervals.forEach(id => clearInterval(id)); homeIntervals = [] }

// Helpers UI
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

// Escapes
function escapeHtml(str) { return String(str || '').replace(/[&<>"]/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[c])) }
function escapeAttr(str) { return escapeHtml(str).replace(/'/g, '&#39;') }
function truncate(text, max = 25) { const t = String(text || ''); return t.length > max ? t.slice(0, max - 3) + '...' : t }

async function loadTinacosConBomba() {
    if (!homeGrid) return
    setHomeState({ loading: true, error: '', empty: false })
    homeGrid.innerHTML = ''
    clearHomeIntervals()
    try {
        const res = await authenticatedFetch(`${API_BASE_URL}/tinacos/con-bomba`, { method: 'GET' })
        if (!res.ok) throw new Error(`Error ${res.status}`)
        const data = await res.json()
        renderHome(data)
    } catch (e) {
        setHomeState({ loading: false, error: e.message, empty: false })
        return
    }
    setHomeState({ loading: false, error: '', empty: homeGrid.children.length === 0 })
}

function renderHome(items) {
    homeGrid.innerHTML = ''
    if (!items || items.length === 0) { setHomeState({ loading: false, error: '', empty: true }); return }
    const frag = document.createDocumentFragment()
    items.forEach(it => {
        const tin = it.tinacoResponse || it.tinaco || it.tinacoDto || {}
        const bom = it.bombaResponse || it.bomba || it.bombaDto || {}

        const card = document.createElement('div')
        card.className = 'tc-card'
        card.innerHTML = `
      <div class="tc-header">
        <div class="tc-title">
          <h3 title="${escapeAttr(tin.nombre || '')}">${escapeHtml(truncate(tin.nombre || 'Tinaco', 25))}</h3>
          <small title="${escapeAttr(tin.ubicacion || '')}">${escapeHtml(truncate(tin.ubicacion || '', 30))}</small>
        </div>
        <div class="tc-badges">
          <span class="badge-sm ${bom.modoBomba === 'AUTOMATICO' ? 'badge-auto' : 'badge-manual'}" id="badgeModo-${bom.id}">Bomba: ${escapeHtml((bom.modoBomba === 'AUTOMATICO' ? 'Automático' : 'Manual' || 'MANUAL'))}</span>
          <span class="badge-sm ${bom.encendida ? 'badge-pump-on' : ''}" id="badgePump-${bom.id}">Bomba: ${bom.encendida ? 'Encendida' : 'Apagada'}</span>
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
          <dt>Código Tinaco</dt><dd>${escapeHtml(tin.codigoIdentificador || '-')}</dd>
          <dt>Nombre Bomba</dt><dd title="${escapeAttr(bom.nombre || '')}">${escapeHtml(truncate(bom.nombre || '', 20)) || '-'}</dd>
          <dt>Código Bomba</dt><dd>${escapeHtml(bom.codigoIdentificador || '-')}</dd>
        </dl>
      </div>
      <div class="tc-footer">
        <div class="pump-controls" id="controls-${bom.id}">
          <span class="state-indicator ${bom.encendida ? 'state-on' : 'state-off'}" id="pstate-${bom.id}"></span>
          ${bom.modoBomba === 'MANUAL' ? `
            <label>
              <input type="checkbox" id="ptoggle-${bom.id}" ${bom.encendida ? 'checked' : ''} />
              Encender bomba
            </label>
          ` : `
            <span>Enciende ${bom.porcentajeEncender ?? '-'}% / apaga ${bom.porcentajeApagar ?? '-'}%</span>
          `}
        </div>
        <button class="btn-link" id="unlink-${tin.id}" title="Desvincular bomba">Desvincular bomba</button>
      </div>
    `

        frag.appendChild(card)

        const int1 = setInterval(() => updateNivel(tin.id), 2000)
        homeIntervals.push(int1)
        const int2 = setInterval(() => refreshPumpState(bom), 2000)
        homeIntervals.push(int2)

        if (bom.modoBomba === 'MANUAL') {
            queueMicrotask(() => {
                const toggle = document.getElementById(`ptoggle-${bom.id}`)
                if (toggle) {
                    toggle.addEventListener('change', async ev => {
                        const checked = ev.target.checked
                        const btn = ev.target
                        btn.disabled = true
                        try {
                            const url = `${API_BASE_URL}/bombas/${checked ? 'encender' : 'apagar'}/${bom.id}`
                            const r = await authenticatedFetch(url, { method: 'POST' })
                            if (!r.ok) { const t = await r.text().catch(() => ''); throw new Error(t || `Error ${r.status}`) }
                            const ind = document.getElementById(`pstate-${bom.id}`)
                            if (ind) { ind.classList.toggle('state-on', checked); ind.classList.toggle('state-off', !checked) }
                        } catch (e) {
                            ev.target.checked = !checked
                            showHomeError(e.message)
                        } finally { btn.disabled = false }
                    })
                }
            })
        }

        queueMicrotask(() => {
            const unlink = document.getElementById(`unlink-${tin.id}`)
            if (unlink) unlink.addEventListener('click', () => desvincularBomba(tin.id, unlink))
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
        const toggle = document.getElementById(`ptoggle-${bomba.id}`)
        if (toggle && bomba.modoBomba === 'MANUAL') toggle.checked = !!encendida
    } catch (_) { }
}

async function desvincularBomba(tinacoId, btn) {
    if (!(tinacoId > 0)) return
    const ok = confirm('¿Deseas desvincular la bomba de este tinaco?')
    if (!ok) return
    try {
        if (btn) btn.disabled = true
        const r = await authenticatedFetch(`${API_BASE_URL}/tinacos/desvincular-bomba/${tinacoId}`, { method: 'DELETE' })
        if (!r.ok) { const t = await r.text().catch(() => ''); throw new Error(t || `Error ${r.status}`) }
        window.location.reload()
    } catch (e) {
        console.error('desvincularBomba error:', e)
        showHomeError(e.message)
    } finally { if (btn) btn.disabled = false }
}
