// Configuración de la API
const API_BASE_URL = "http://localhost:8080"

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
        headers: {
            "Content-Type": "application/json",
            ...options.headers,
        },
    }

    let response = await fetch(url, { ...options, ...defaultOptions })

    if (response.status === 401 || response.status === 403) {
        console.warn("Token expirado. Intentando refresh...")

        const refreshResponse = await fetch(`${API_BASE_URL}/auth/refresh`, {
            method: "POST",
            credentials: "include",
        })

        if (refreshResponse.ok) {
            response = await fetch(url, { ...options, ...defaultOptions })
        } else {
            throw new Error("No autorizado - Token inválido o expirado")
        }
    }

    return response
}

// Función para hacer logout
async function logoutUser() {
    try {
        const response = await authenticatedFetch(`${API_BASE_URL}/auth/logout`, {
            method: "POST",
            credentials: "include",
        })

        if (response.ok) {
            localStorage.clear()
            window.location.replace("login.html")
        } else {
            throw new Error("Error en el logout")
        }
    } catch (error) {
        console.error("Error en logout:", error)
        window.location.replace("login.html")
    }
}

// Función para obtener datos del usuario
async function getUserData() {
    const cachedUser = localStorage.getItem("currentUser")
    if (cachedUser) {
        window.currentUser = JSON.parse(cachedUser)
        if (userName) userName.textContent = window.currentUser.nombre
        return
    }

    try {
        const response = await authenticatedFetch(`${API_BASE_URL}/usuario/me`, { method: "GET" })
        if (response && response.ok) {
            const userData = await response.json()
            if (userName) userName.textContent = userData.nombre
            window.currentUser = userData
            localStorage.setItem("currentUser", JSON.stringify(userData))
        } else {
            throw new Error(`Error al obtener datos del usuario: ${response.status}`)
        }
    } catch (error) {
        console.error("Error al obtener datos del usuario:", error)
        if (userName) userName.textContent = "Usuario"
        await logoutUser()
    }
}

// Event listeners
if (logoutBtn) {
    logoutBtn.addEventListener("click", () => {
        if (confirm("¿Estas seguro de que quieres salir?")) {
            logoutUser()
        }
    })
}

function clearStates() {
    if (bombasErrorEl) bombasErrorEl.style.display = 'none'
    if (bombasEmptyEl) bombasEmptyEl.style.display = 'none'
}

function showLoading(show) {
    if (bombasLoadingEl) bombasLoadingEl.style.display = show ? 'block' : 'none'
    if (bombasListEl) bombasListEl.setAttribute('aria-busy', show ? 'true' : 'false')
}

function showError(msg) {
    clearStates()
    if (bombasErrorEl) {
        bombasErrorEl.textContent = msg
        bombasErrorEl.style.display = 'block'
    }
}

// Obtener lista de bombas
async function fetchBombas() {
    if (!bombasListEl) return
    showLoading(true)
    try {
        const response = await authenticatedFetch(`${API_BASE_URL}/bombas`, { method: 'GET' })
        if (!response.ok) throw new Error(`Error ${response.status}`)
        const data = await response.json()
        renderBombas(data)
    } catch (err) {
        showError(err.message)
        console.error('Error fetchBombas:', err)
    } finally {
        showLoading(false)
    }
}

function truncate(text, max = 100) {
    if (!text) return ''
    return text.length > max ? text.slice(0, max - 1) + '…' : text
}

function escapeHtml(str) {
    return String(str).replace(/[&<>"];/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', ';': '&#59;' }[c] || c))
}
function escapeAttr(str) { return escapeHtml(str).replace(/'/g, '&#39;') }

function renderBombas(bombas) {
    clearStates()
    bombasListEl.innerHTML = ''
    if (!bombas || bombas.length === 0) {
        if (bombasEmptyEl) bombasEmptyEl.style.display = 'block'
        return
    }

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
                        <span id="badgeModo-${Number(b.id)}" class="badge ${modoClass}">${escapeHtml(b.modoBomba || '')}</span>
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
                    <button class="btn btn-edit" title="Editar" onclick="editarBomba(${Number(b.id)})">Editar</button>
                    <button id="btnDesactivar-${Number(b.id)}" class="btn btn-disable" title="Desactivar" onclick="desactivarBomba(${Number(b.id)})">Desactivar</button>
                </div>
                <label class="mode-switch" title="Cambiar modo">
                    <!--<span>Modo</span> -->
                    <select id="selectModo-${Number(b.id)}">
                        <option value="MANUAL" ${b.modoBomba === 'MANUAL' ? 'selected' : ''}>MANUAL</option>
                        <option value="AUTOMATICO" ${b.modoBomba === 'AUTOMATICO' ? 'selected' : ''}>AUTOMÁTICO</option>
                    </select>
                </label>
            </div>
        `

        // Wire change handler para cambio de modo
        const select = card.querySelector(`#selectModo-${Number(b.id)}`)
        if (select) {
            select.addEventListener('change', async (ev) => {
                const next = ev.target.value
                const prev = b.modoBomba || (next === 'MANUAL' ? 'AUTOMATICO' : 'MANUAL')
                if (next === prev) return
                ev.target.disabled = true
                try {
                    const resp = await authenticatedFetch(`${API_BASE_URL}/bombas/estado/${Number(b.id)}`, {
                        method: 'PUT',
                        body: JSON.stringify({ modoBomba: next })
                    })
                    if (!resp.ok) {
                        const txt = await resp.text().catch(() => '')
                        throw new Error(txt || `Error al actualizar modo (${resp.status})`)
                    }
                    const data = await resp.json().catch(() => ({}))
                    const returnedModo = data && data.modoBomba ? String(data.modoBomba) : next
                    b.modoBomba = returnedModo
                    /* Actualizar badge
                    const badge = card.querySelector(`#badgeModo-${Number(b.id)}`)
                    if (badge) {
                        badge.textContent = returnedModo
                        badge.classList.remove('modo-manual', 'modo-automatico')
                        badge.classList.add(returnedModo === 'AUTOMATICO' ? 'modo-automatico' : 'modo-manual')
                    }
                    // Asegurar que el select refleje el modo actual
                    ev.target.value = returnedModo */
                    window.location.reload()

                } catch (e) {
                    showError(e.message || 'No se pudo actualizar el modo de la bomba')
                    // Revertir selección
                    ev.target.value = prev
                } finally {
                    ev.target.disabled = false
                }
            })
        }

        frag.appendChild(card)
    })
    bombasListEl.appendChild(frag)
}

// Inicializar cuando el DOM esté listo
document.addEventListener("DOMContentLoaded", () => {
    getUserData()
    fetchBombas()
})

// Navegar a la página de edición con el id de la bomba
function editarBomba(id) {
    if (id == null || isNaN(Number(id))) return
    window.location.href = `bomba-edit.html?id=${id}`
}

// Desactivar bomba con confirmación y recarga
async function desactivarBomba(id) {
    if (id == null || isNaN(Number(id))) return
    const ok = confirm('¿Seguro que deseas desactivar esta bomba? Ya no se listará aquí y la verás en la página de bombas desactivadas, desde donde podrás volver a activarla.')
    if (!ok) return

    const btn = document.getElementById(`btnDesactivar-${Number(id)}`)
    try {
        if (btn) { btn.disabled = true; }
        const resp = await authenticatedFetch(`${API_BASE_URL}/bombas/desactivar/${Number(id)}`, { method: 'POST' })
        if (!resp.ok) {
            // Intentar leer detalle
            const txt = await resp.text().catch(() => '')
            throw new Error(txt || `Error al desactivar la bomba ${resp.status}`)
        }
        window.location.reload()
    } catch (e) {
        if (btn) { btn.disabled = false; }
        showError(e.message)
        console.error('desactivarBomba error:', e)
    }
}
