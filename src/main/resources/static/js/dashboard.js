// Configuración de la API
const API_BASE_URL = "http://localhost:8080"

// Elementos del DOM
const userName = document.getElementById("userName")
const logoutBtn = document.getElementById("logoutBtn")

// Función para hacer peticiones autenticadas a la API con manejo de refresh token
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
    const cached = localStorage.getItem('currentUser')
    if (cached) {
        const u = JSON.parse(cached)
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

if (logoutBtn) logoutBtn.addEventListener('click', () => { if (confirm('¿Estas seguro de que quieres salir?')) logoutUser() })

document.addEventListener('DOMContentLoaded', () => { getUserData() })

