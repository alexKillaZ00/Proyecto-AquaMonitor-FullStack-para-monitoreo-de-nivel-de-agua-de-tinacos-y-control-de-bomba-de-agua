// Configuración de la API
const API_BASE_URL = "http://localhost:8080"

// Elementos del DOM
const userName = document.getElementById("userName")
const logoutBtn = document.getElementById("logoutBtn")
const tinacosListEl = document.getElementById("tinacosList")
const tinacosLoadingEl = document.getElementById("tinacosLoading")
const tinacosErrorEl = document.getElementById("tinacosError")
const tinacosEmptyEl = document.getElementById("tinacosEmpty")

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
            console.log("Refresh token exitoso. Reintentando la solicitud original...")
            response = await fetch(url, { ...options, ...defaultOptions })
        } else {
            console.error("Refresh token falló. Redirigiendo a login...")
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
        userName.textContent = window.currentUser.nombre
        return
    }

    try {
        const response = await authenticatedFetch(`${API_BASE_URL}/usuario/me`, {
            method: "GET",
        })

        if (response && response.ok) {
            const userData = await response.json()
            userName.textContent = userData.nombre
            window.currentUser = userData
            localStorage.setItem("currentUser", JSON.stringify(userData))
        } else {
            throw new Error(`Error al obtener datos del usuario: ${response.status}`)
        }
    } catch (error) {
        console.error("Error al obtener datos del usuario:", error)
        userName.textContent = "Usuario"
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

// Obtener lista de tinacos
async function fetchTinacos() {
    if (!tinacosListEl) return
    showLoading(true)
    try {
        const response = await authenticatedFetch(`${API_BASE_URL}/tinacos`, { method: 'GET' })
        if (!response.ok) throw new Error(`Error al obtener tinacos (${response.status})`)
        const data = await response.json()
        renderTinacos(data)
    } catch (err) {
        showError(err.message)
        console.error('Error fetchTinacos:', err)
    } finally {
        showLoading(false)
    }
}

function truncate(text, max = 100) {
    if (!text) return ''
    return text.length > max ? text.slice(0, max - 1) + '…' : text
}

function clearStates() {
    if (tinacosErrorEl) tinacosErrorEl.style.display = 'none'
    if (tinacosEmptyEl) tinacosEmptyEl.style.display = 'none'
}

function showLoading(show) {
    if (tinacosLoadingEl) tinacosLoadingEl.style.display = show ? 'block' : 'none'
    if (tinacosListEl) tinacosListEl.setAttribute('aria-busy', show ? 'true' : 'false')
}

function showError(msg) {
    clearStates()
    if (tinacosErrorEl) {
        tinacosErrorEl.textContent = msg
        tinacosErrorEl.style.display = 'block'
    }
}

function renderTinacos(tinacos) {
    clearStates()
    tinacosListEl.innerHTML = ''
    if (!tinacos || tinacos.length === 0) {
        if (tinacosEmptyEl) tinacosEmptyEl.style.display = 'block'
        return
    }

    const frag = document.createDocumentFragment()
    tinacos.forEach(t => {
        const card = document.createElement('div')
        card.className = 'tinaco-card'
        const estado = (t.estado || '').toUpperCase() === 'ACTIVADO'
        const conBomba = !!t.tieneBomba
        const icon = '💧'
        const estadoBadgeClass = estado ? 'estado-activado' : 'estado-desactivado'
        const bombaBadgeClass = conBomba ? 'bomba-activa' : 'sin-bomba'
        const bombaIcon = conBomba ? '⚙️' : '⛔'
        const bombaTexto = conBomba ? 'Con bomba' : 'Sin bomba'

        card.innerHTML = `
                    <header>
                        <div class="tinaco-icon" aria-hidden="true">${icon}</div>
                        <div class="tinaco-title-wrap">
                            <h2 class="tinaco-name truncate" title="${escapeHtml(t.nombre || '')}">${truncate(t.nombre || '')}</h2>
                            <div class="badges">
                                <span class="badge ${estadoBadgeClass}">${estado ? 'ACTIVADO' : 'DESACTIVADO'}</span>
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
                        <button class="btn btn-edit" type="button" onclick="editarTinaco(${Number(t.id)})" aria-label="Editar tinaco ${escapeAttr(t.nombre || '')}">Editar</button>
                        <button id="btnDesactivar-${Number(t.id)}" class="btn btn-disable" type="button" onclick="desactivarTinaco(${Number(t.id)})" aria-label="Desactivar tinaco ${escapeAttr(t.nombre || '')}">Desactivar</button>
                    </div>
                `
        frag.appendChild(card)
    })
    tinacosListEl.appendChild(frag)
}

function escapeHtml(str) {
    return String(str).replace(/[&<>"];/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', ';': '&#59;' }[c] || c))
}
function escapeAttr(str) { return escapeHtml(str).replace(/'/g, '&#39;') }

// Inicializar cuando el DOM esté listo
document.addEventListener("DOMContentLoaded", () => {
    getUserData()
    fetchTinacos()
})

// Navegar a la página de edición con el id del tinaco
function editarTinaco(id) {
    if (id == null || isNaN(Number(id))) return
    window.location.href = `tinaco-edit.html?id=${id}`
}

// Desactivar tinaco con confirmación y recarga
async function desactivarTinaco(id) {
    if (id == null || isNaN(Number(id))) return
    const ok = confirm('¿Seguro que deseas desactivar este tinaco? Ya no se listará aquí y lo verás en la página de tinacos desactivados, desde donde podrás volver a activarlo.')
    if (!ok) return

    const btn = document.getElementById(`btnDesactivar-${Number(id)}`)
    try {
        if (btn) { btn.disabled = true; btn.textContent = 'Desactivando…' }
        const res = await authenticatedFetch(`${API_BASE_URL}/tinacos/desactivar/${Number(id)}`, { method: 'POST' })
        if (!res.ok) {
            // Intentar leer detalle
            const txt = await res.text().catch(() => '')
            throw new Error(txt || `Error al desactivar tinaco (${res.status})`)
        }
        // Recargar para reflejar cambios
        window.location.reload()
    } catch (e) {
        if (btn) { btn.disabled = false; btn.textContent = 'Desactivar' }
        showError(e.message)
        console.error('desactivarTinaco error:', e)
    }
}
