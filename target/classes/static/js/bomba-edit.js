// Configuración de la API
const API_BASE_URL = "http://localhost:8080"

// Elementos del DOM
const userName = document.getElementById("userName")
const logoutBtn = document.getElementById("logoutBtn")

const editLoading = document.getElementById('editLoading')
const editError = document.getElementById('editError')
const formContainer = document.getElementById('formContainer')

const form = document.getElementById('bombaEditForm')
const nombreInput = document.getElementById('nombre')
const ubicacionInput = document.getElementById('ubicacion')
const encenderSelect = document.getElementById('porcentajeEncender')
const apagarSelect = document.getElementById('porcentajeApagar')

const formGlobalError = document.getElementById('formGlobalError')
const formGlobalSuccess = document.getElementById('formGlobalSuccess')

const nombreError = document.getElementById('nombreError')
const ubicacionError = document.getElementById('ubicacionError')
const encenderError = document.getElementById('porcentajeEncenderError')
const apagarError = document.getElementById('porcentajeApagarError')

// Función para hacer peticiones autenticadas con refresh token
async function authenticatedFetch(url, options = {}) {
  const defaultOptions = {
    credentials: "include",
    headers: { "Content-Type": "application/json", ...options.headers },
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
    const r = await authenticatedFetch(`${API_BASE_URL}/auth/logout`, { method: 'POST', credentials: 'include' })
    if (r.ok) { localStorage.clear(); window.location.replace('login.html') } else { throw new Error('Error logout') }
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

function getBombaIdFromUrl() {
  const params = new URLSearchParams(window.location.search)
  const id = Number(params.get('id'))
  return isNaN(id) ? null : id
}

function clearFieldErrors() {
  if (formGlobalError) { formGlobalError.style.display = 'none'; formGlobalError.textContent = '' }
  if (formGlobalSuccess) { formGlobalSuccess.style.display = 'none'; formGlobalSuccess.textContent = '' }
  if (nombreError) nombreError.textContent = ''
  if (ubicacionError) ubicacionError.textContent = ''
  if (encenderError) encenderError.textContent = ''
  if (apagarError) apagarError.textContent = ''
}

function validateForm() {
  clearFieldErrors()
  let ok = true
  const nombre = (nombreInput.value || '').trim()
  const ubicacion = (ubicacionInput.value || '').trim()
  const pEnc = encenderSelect.value ? Number(encenderSelect.value) : null
  const pApg = apagarSelect.value ? Number(apagarSelect.value) : null

  // ValidationsService.validarActualizarBomba
  if (!nombre) { nombreError.textContent = 'El nombre de la bomba es obligatorio'; ok = false }
  else if (nombre.length < 2 || nombre.length > 100) { nombreError.textContent = 'El nombre debe tener entre 2 y 100 caracteres'; ok = false }

  if (ubicacion) {
    if (ubicacion.length < 2 || ubicacion.length > 100) { ubicacionError.textContent = 'La ubicación debe tener entre 2 y 100 caracteres'; ok = false }
  }

  if (![15,20,25].includes(pEnc)) { encenderError.textContent = 'Selecciona 15, 20 o 25'; ok = false }
  if (![85,90,100].includes(pApg)) { apagarError.textContent = 'Selecciona 85, 90 o 100'; ok = false }

  return ok
}

async function loadBomba() {
  const id = getBombaIdFromUrl()
  if (!id) {
    if (editError) { editError.textContent = 'ID de bomba no válido o no proporcionado'; editError.style.display = 'block' }
    if (editLoading) editLoading.style.display = 'none'
    if (formContainer) formContainer.style.display = 'none'
    return
  }
  try {
    const r = await authenticatedFetch(`${API_BASE_URL}/bombas/${id}`, { method: 'GET' })
    if (!r.ok) {
      const errText = await r.text()
      throw new Error(`Error al obtener bomba (${errText || r.status})`)
    }
    const b = await r.json()
    // Precargar solo campos de ActualizarBombaRequest
    nombreInput.value = b.nombre ?? ''
    ubicacionInput.value = b.ubicacion ?? ''

    // Si vienen porcentajes en la respuesta (modo automatico), setearlos; sino dejar valores por defecto existentes del select
    if (b.porcentajeEncender && [15,20,25].includes(Number(b.porcentajeEncender))) encenderSelect.value = String(b.porcentajeEncender)
    if (b.porcentajeApagar && [85,90,100].includes(Number(b.porcentajeApagar))) apagarSelect.value = String(b.porcentajeApagar)

    if (formContainer) formContainer.style.display = 'block'
    if (editLoading) editLoading.style.display = 'none'
    if (editError) editError.style.display = 'none'
  } catch (e) {
    console.error('loadBomba', e)
    if (editError) { editError.textContent = e.message; editError.style.display = 'block' }
    if (editLoading) editLoading.style.display = 'none'
    if (formContainer) formContainer.style.display = 'none'
  }
}

async function submitEdit(e) {
  e.preventDefault()
  if (!validateForm()) return
  const id = getBombaIdFromUrl()
  if (!id) return
  const payload = {
    nombre: (nombreInput.value || '').trim(),
    ubicacion: (ubicacionInput.value || '').trim() || null,
    porcentajeEncender: Number(encenderSelect.value),
    porcentajeApagar: Number(apagarSelect.value)
  }
  try {
    const r = await authenticatedFetch(`${API_BASE_URL}/bombas/editar/${id}`, {
      method: 'PUT',
      body: JSON.stringify(payload)
    })
    if (!r.ok) throw new Error(`Error al actualizar bomba (${r.status})`)
    if (formGlobalSuccess) { formGlobalSuccess.textContent = 'Bomba actualizada correctamente'; formGlobalSuccess.style.display = 'block' }
    setTimeout(() => { window.location.href = 'bombas-list.html' }, 800)
  } catch (e) {
    console.error('submitEdit', e)
    if (formGlobalError) { formGlobalError.textContent = e.message || 'No se pudo actualizar. Verifica los datos e intenta de nuevo.'; formGlobalError.style.display = 'block' }
  }
}

document.addEventListener('DOMContentLoaded', () => {
  // Estados iniciales similares a tinaco-edit
  if (editLoading) editLoading.style.display = 'block'
  if (editError) editError.style.display = 'none'
  if (formContainer) formContainer.style.display = 'none'
  getUserData()
  loadBomba()
  if (form) form.addEventListener('submit', submitEdit)
})
