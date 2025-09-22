// tinaco-add.js
const API_BASE_URL = 'http://localhost:8080'

// Referencias comunes
const userName = document.getElementById('userName')
const logoutBtn = document.getElementById('logoutBtn')

// Referencias del formulario
const form = document.getElementById('addTinacoForm')
const fNombre = document.getElementById('nombre')
const fUbicacion = document.getElementById('ubicacion')
const fCapacidad = document.getElementById('capacidadLitros')
const fDestino = document.getElementById('destinoAgua')
const fAltura = document.getElementById('alturaMaximaCm')
const fCodigo = document.getElementById('codigoIdentificador')

const errNombre = document.getElementById('err-nombre')
const errUbicacion = document.getElementById('err-ubicacion')
const errCapacidad = document.getElementById('err-capacidad')
const errDestino = document.getElementById('err-destino')
const errAltura = document.getElementById('err-altura')
const errCodigo = document.getElementById('err-codigo')

const formGlobalError = document.getElementById('formGlobalError')
const formGlobalSuccess = document.getElementById('formGlobalSuccess')

// Funciones comunes
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
    errCodigo.textContent = ''
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
    const codigo = fCodigo.value.trim()

    // Validaciones basada en validarRegistroTinaco (equivalente a update + requeridos)
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

    if (!capRaw) {
        errCapacidad.textContent = 'La capacidad es obligatoria'
        ok = false
    } else {
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

    if (!altRaw) {
        errAltura.textContent = 'La altura máxima es obligatoria'
        ok = false
    } else {
        const alt = Number(altRaw)
        if (!(alt > 0 && alt <= 1000)) {
            errAltura.textContent = 'La altura del tinaco debe ser un número positivo y menor a 1000 cm'
            ok = false
        }
    }

    if (!codigo) {
        errCodigo.textContent = 'El código identificador es obligatorio'
        ok = false
    } else {
        if (codigo.length < 11 || codigo.length > 15) {
            errCodigo.textContent = 'El código debe de contener de 11 a 15 caracteres'
            ok = false
        }
    }

    return ok
}

async function submitCreate(ev) {
    ev.preventDefault()
    if (!validateForm()) return

    const payload = {
        nombre: fNombre.value.trim(),
        ubicacion: fUbicacion.value.trim() || null,
        capacidadLitros: Number(fCapacidad.value),
        destinoAgua: fDestino.value.trim() || null,
        alturaMaximaCm: Number(fAltura.value),
        codigoIdentificador: fCodigo.value.trim(),
    }

    try {
        const res = await authenticatedFetch(`${API_BASE_URL}/tinacos`, { method: 'POST', body: JSON.stringify(payload) })
        if (!res.ok) {
            // Intentar leer detalle
            const txt = await res.text().catch(() => '')
            throw new Error(txt || `Error al registrar tinaco (${res.status})`)
        }

        formGlobalSuccess.textContent = 'Tinaco registrado correctamente'
        formGlobalSuccess.style.display = 'block'
        // Limpiar campos
        form.reset()
    } catch (e) {
        formGlobalError.textContent = e.message
        formGlobalError.style.display = 'block'
    }
}

// Init
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => { getUserData(); if (form) form.addEventListener('submit', submitCreate) })
} else {
    getUserData(); if (form) form.addEventListener('submit', submitCreate)
}

