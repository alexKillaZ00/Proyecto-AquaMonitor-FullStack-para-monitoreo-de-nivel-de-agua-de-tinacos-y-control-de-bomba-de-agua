// tinaco-edit.js
const API_BASE_URL = 'http://localhost:8080'

const userName = document.getElementById('userName')
const logoutBtn = document.getElementById('logoutBtn')

const editLoading = document.getElementById('editLoading')
const editError = document.getElementById('editError')
const formContainer = document.getElementById('formContainer')
const form = document.getElementById('editTinacoForm')

const fNombre = document.getElementById('nombre')
const fUbicacion = document.getElementById('ubicacion')
const fCapacidad = document.getElementById('capacidadLitros')
const fDestino = document.getElementById('destinoAgua')
const fAltura = document.getElementById('alturaMaximaCm')

const errNombre = document.getElementById('err-nombre')
const errUbicacion = document.getElementById('err-ubicacion')
const errCapacidad = document.getElementById('err-capacidad')
const errDestino = document.getElementById('err-destino')
const errAltura = document.getElementById('err-altura')

const formGlobalError = document.getElementById('formGlobalError')
const formGlobalSuccess = document.getElementById('formGlobalSuccess')

function getQueryId() {
    const params = new URLSearchParams(window.location.search)
    const id = Number(params.get('id'))
    return isNaN(id) ? null : id
}

async function authenticatedFetch(url, options = {}) {
    const defaultOptions = {
        credentials: 'include',
        headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
    }
    let response = await fetch(url, { ...options, ...defaultOptions })
    if (response.status === 401 || response.status === 403) {
        const refresh = await fetch(`${API_BASE_URL}/auth/refresh`, { method: 'POST', credentials: 'include' })
        if (refresh.ok) {
            response = await fetch(url, { ...options, ...defaultOptions })
        } else {
            throw new Error('No autorizado - Token inválido o expirado')
        }
    }
    return response
}

async function logoutUser() {
    try {
        const response = await authenticatedFetch(`${API_BASE_URL}/auth/logout`, { method: 'POST', credentials: 'include' })
        if (response.ok) {
            localStorage.clear()
            window.location.replace('login.html')
        } else {
            throw new Error("Error en el logout")
        }
    } catch (_) {
        window.location.replace('login.html')
    }
}

async function getUserData() {
    const cachedUser = localStorage.getItem('currentUser')
    if (cachedUser) {
        window.currentUser = JSON.parse(cachedUser)
        if (userName) userName.textContent = window.currentUser.nombre
        return
    }
    try {
        const response = await authenticatedFetch(`${API_BASE_URL}/usuario/me`, { method: 'GET' })
        if (response.ok) {
            const userData = await response.json()
            if (userName) userName.textContent = userData.nombre
            window.currentUser = userData
            localStorage.setItem('currentUser', JSON.stringify(userData))
        } else {
            throw new Error('No se pudo obtener el usuario')
        }
    } catch (e) {
        if (userName) userName.textContent = 'Usuario'
        await logoutUser()
    }
}

if (logoutBtn) {
    logoutBtn.addEventListener('click', () => { if (confirm('¿Estas seguro de que quieres salir?')) logoutUser() })
}

function clearFieldErrors() {
    errNombre.textContent = ''
    errUbicacion.textContent = ''
    errCapacidad.textContent = ''
    errDestino.textContent = ''
    errAltura.textContent = ''
    formGlobalError.style.display = 'none'
}

function validateForm() {
    clearFieldErrors()
    let ok = true
    const nombre = fNombre.value.trim()
    const ubicacion = fUbicacion.value.trim()
    const destino = fDestino.value.trim()
    const capRaw = fCapacidad.value
    const altRaw = fAltura.value

    // Validaciones equivalentes a validarActualizarTinaco
    if (!nombre || nombre.length < 2 || nombre.length > 100) {
        errNombre.textContent = 'El nombre del tinaco debe tener entre 2 y 100 caracteres'
        ok = false
    }

    if (ubicacion) {
        if (ubicacion.length < 2 || ubicacion.length > 100) {
            errUbicacion.textContent = 'La ubicacion del tinaco debe tener entre 2 y 100 caracteres'
            ok = false
        }
    }

    if (capRaw) {
        const cap = Number(capRaw)
        if (!(cap > 0 && cap <= 100000)) {
            errCapacidad.textContent = 'La capacidad debe ser un número positivo y menor a 100000L'
            ok = false
        }
    }

    if (destino) {
        if (destino.length < 2 || destino.length > 100) {
            errDestino.textContent = 'El destino del agua debe tener entre 2 y 100 caracteres'
            ok = false
        }
    }

    if (altRaw) {
        const alt = Number(altRaw)
        if (!(alt > 0 && alt <= 1000)) {
            errAltura.textContent = 'La altura del tinaco debe ser un número positivo y menor a 1000 cm'
            ok = false
        }
    }

    return ok
}

async function loadTinaco() {
    const id = getQueryId()
    if (!id) {
        editError.textContent = 'ID de tinaco no válido o no proporcionado'
        editError.style.display = 'block'
        editLoading.style.display = 'none'
        return
    }
    try {
        const res = await authenticatedFetch(`${API_BASE_URL}/tinacos/${id}`, { method: 'GET' })
        if (!res.ok) {
            const errorData = await res.text();
            throw new Error(`Error al obtener tinaco (${errorData})`)
        }
        const data = await res.json()

        // Precargar campos (coinciden con ActualizarTinacoRequest)
        fNombre.value = data.nombre || ''
        fUbicacion.value = data.ubicacion || ''
        fCapacidad.value = data.capacidadLitros != null ? data.capacidadLitros : ''
        fDestino.value = data.destinoAgua || ''
        fAltura.value = data.alturaMaximaCm != null ? data.alturaMaximaCm : ''

        formContainer.style.display = 'block'
        editLoading.style.display = 'none'
    } catch (e) {
        editError.textContent = e.message
        editError.style.display = 'block'
        editLoading.style.display = 'none'
    }
}

async function submitEdit(ev) {
    ev.preventDefault()
    if (!validateForm()) return

    const id = getQueryId()
    const payload = {
        nombre: fNombre.value.trim(),
        ubicacion: fUbicacion.value.trim() || null,
        capacidadLitros: fCapacidad.value ? Number(fCapacidad.value) : null,
        destinoAgua: fDestino.value.trim() || null,
        alturaMaximaCm: fAltura.value ? Number(fAltura.value) : null,
    }

    try {
        const res = await authenticatedFetch(`${API_BASE_URL}/tinacos/editar/${id}`, {
            method: 'PUT',
            body: JSON.stringify(payload),
        })
        if (!res.ok) throw new Error(`Error al actualizar tinaco (${res.status})`)

        formGlobalSuccess.textContent = 'Tinaco actualizado correctamente'
        formGlobalSuccess.style.display = 'block'
        setTimeout(() => { window.location.href = 'tinacos-list.html' }, 800)
    } catch (e) {
        formGlobalError.textContent = e.message
        formGlobalError.style.display = 'block'
    }
}

// Init
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => { getUserData(); loadTinaco(); form.addEventListener('submit', submitEdit) })
} else {
    getUserData(); loadTinaco(); form.addEventListener('submit', submitEdit)
}
